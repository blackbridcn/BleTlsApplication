package org.tls12.client;


import com.hidglobal.duality.relay.application.BaseApplication;
import com.hidglobal.duality.testapplication.R;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsClientContext;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.TlsServerCertificate;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.tls12.selector.TlsCryptoSelector;
import org.tls12.utils.CertificateUtils;
import org.tls12.utils.KeyUtils;
import org.tls12.utils.SignersUtils;
import org.tls12.utils.TlsV2Utils;
import org.utils.LogUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Vector;

/**
 * Author: yuzzha
 * Date: 2021-12-02 16:55
 * Description:
 * Remark:
 */
public class ClientTlsAuthentication implements TlsAuthentication {

    private static final String TAG = ClientTlsAuthentication.class.getSimpleName();


    private TlsV2Client tlsV2Client;
    protected TlsClientContext context;

    public ClientTlsAuthentication(TlsV2Client tlsV2Client, TlsClientContext context) {
        this.tlsV2Client = tlsV2Client;
        this.context = context;
    }

    //Server证书的校验
    //this method is responsible for certificate verification and validation
    @Override
    public void notifyServerCertificate(TlsServerCertificate serverCertificate) throws IOException {
        LogUtils.i("TAG", "notifyServerCertificate 收到服务端证书 ");
        TlsCertificate[] chain = serverCertificate.getCertificate().getCertificateList();

        boolean isEmpty = serverCertificate == null || serverCertificate.getCertificate() == null
                || serverCertificate.getCertificate().isEmpty();
        if (isEmpty) {
            LogUtils.e("TAG", "notifyServerCertificate 收到服务端证书 TlsFatalAlert : empty certificate ");
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }

        Certificate mServerCertificate = Certificate.getInstance(chain[0].getEncoded());
        //有效期验证
        if (CertificateUtils.checkValidity(mServerCertificate)) {
            LogUtils.e("TAG", "notifyServerCertificate 收到服务端证书 TlsFatalAlert: certificate_expired ");
            throw new TlsFatalAlert(AlertDescription.certificate_expired);
        }
        TBSCertificate mServerTbsCertificate = mServerCertificate.getTBSCertificate();

        //加载根证书校验 服务端传Tls传输过来的证书
        TlsCertificate rootTlsCertificate = TlsV2Utils.loadCertificateResource(TlsCryptoSelector.getCrypto(), BaseApplication.getContext(), R.raw.ecc_root_cert);
        Certificate rootCertificate = Certificate.getInstance(rootTlsCertificate.getEncoded());

        X500Name issuer = mServerTbsCertificate.getIssuer();

        if (issuer.equals(rootCertificate.getSubject())) {
            try {
                byte[] signature = mServerCertificate.getSignature().getOctets();
                PublicKey publicKey = KeyUtils.getPublicKey(rootCertificate.getSubjectPublicKeyInfo());
                boolean serverCaSigner = SignersUtils.verify(
                        mServerTbsCertificate.getEncoded(),
                        signature,
                        publicKey, "SHA256WITHECDSA");

                LogUtils.i(TAG, "TlsServerCertificate serverCaSigner :" + serverCaSigner);

                if (!serverCaSigner) {
                    LogUtils.e("TAG", "notifyServerCertificate 收到服务端证书 TlsFatalAlert: verify incorrectly ");
                    //签名校验失败
                    throw new TlsFatalAlert(AlertDescription.bad_certificate);
                }
            } catch (IOException | NoSuchProviderException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                LogUtils.e("TAG", "notifyServerCertificate 收到服务端证书 TlsFatalAlert: verify Exception ");
                //签名校验失败
                throw new TlsFatalAlert(AlertDescription.bad_certificate);
            }
        } else {
            LogUtils.e("TAG", "notifyServerCertificate 收到服务端证书 TlsFatalAlert: bad_certificate , Server CA 不是 根证书签发，不受信任CA ");
            // Server CA 不是 根证书签发，不受信任CA
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }

    }

    @Override
    public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {

        Vector supportedSignatureAlgorithms = certificateRequest.getSupportedSignatureAlgorithms();

        SignatureAndHashAlgorithm signatureAndHashAlgorithm = null;
        if (supportedSignatureAlgorithms == null) {
            supportedSignatureAlgorithms = TlsUtils.getDefaultSignatureAlgorithms(SignatureAlgorithm.ecdsa);
        }

        for (int i = 0; i < supportedSignatureAlgorithms.size(); ++i) {
            SignatureAndHashAlgorithm alg = (SignatureAndHashAlgorithm)
                    supportedSignatureAlgorithms.elementAt(i);

            if (alg.getSignature() == SignatureAlgorithm.ecdsa) {
                // Just grab the first one we find
                signatureAndHashAlgorithm = alg;
                break;
            }
        }

        if (signatureAndHashAlgorithm == null) {
            LogUtils.e("TAG", "加载本地客户端证书失败,(signatureAndHashAlgorithm == null) ");
            return null;
        }
        TlsCredentialedSigner signerCredentials = null;
        try {
            //加载客户端CA和私钥
            signerCredentials = TlsV2Utils.loadSignerCredentials(
                    context,
                    BaseApplication.Companion.getContext(),
                    new int[]{R.raw.ecc_phone_cert},
                    R.raw.phone_private, signatureAndHashAlgorithm
            );
        } catch (Exception e) {
            LogUtils.e("TAG", "加载本地客户端证书失败, Exception ：" + e.getMessage());
        }

        return signerCredentials;
    }


}
