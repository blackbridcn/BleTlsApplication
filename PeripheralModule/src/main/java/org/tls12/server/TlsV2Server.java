package org.tls12.server;


import com.hidglobal.duality.testapplication.application.PeripheralApplication;
import com.hidglobal.duality.testapplication.R;

import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.ClientCertificateType;
import org.bouncycastle.tls.DefaultTlsServer;
import org.bouncycastle.tls.HashAlgorithm;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsServerContext;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.tls12.utils.TlsV2Utils;
import org.utils.LogUtils;

import java.io.IOException;
import java.util.Vector;

/**
 * Author: yuzzha
 * Date: 2021/12/9 17:03
 * Description:
 * Remark:
 */
public class TlsV2Server extends DefaultTlsServer {

    public Boolean handshakeFinished = false;

    public TlsV2Server(TlsCrypto crypto) {
        super(crypto);
    }

    @Override
    public void init(TlsServerContext tlsServerContext) {
        super.init(tlsServerContext);
        handshakeFinished = false;
        //LogUtils.e("TAG", "TlsV2Server init ----------------> ");
    }

    @Override
    public int[] getCipherSuites() {
        //LogUtils.e("TAG", "TlsV2Server getCipherSuites ----------------> ");
        //return super.getCipherSuites();
        return new int[]{
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM,
        };
    }

    @Override
    public void notifyHandshakeComplete() throws IOException {
        super.notifyHandshakeComplete();
        handshakeFinished = true;
        LogUtils.e("TAG", "TlsV2Server notifyHandshakeComplete ----------------> ");

    }

    @Override
    public void notifyClientCertificate(Certificate certificate) throws IOException {
        super.notifyClientCertificate(certificate);

    }

    @Override
    public void notifyOfferedCipherSuites(int[] ints) throws IOException {
        super.notifyOfferedCipherSuites(ints);
        //LogUtils.e("TAG", "TlsV2Server notifyOfferedCipherSuites ----------------> "+ints[0]);
    }

    @Override
    public void notifyClientVersion(ProtocolVersion protocolVersion) throws IOException {
        super.notifyClientVersion(protocolVersion);
        // LogUtils.e("TAG", "TlsV2Server notifyClientVersion ----------------> " + protocolVersion.toString());
    }

    protected TlsCredentialedSigner getECDSASignerCredentials() throws IOException {
        // [RFC 8422] Code should choose based on client's  supported sig algs?

        return loadSignerCredentials(SignatureAlgorithm.ecdsa);
    }

    @Override
    public CertificateRequest getCertificateRequest() throws IOException {
        //证书类型
        short[] certificateTypes = new short[]{ClientCertificateType.ecdsa_sign};
        //证书签名和Hash算法
        Vector sigAlgVector = new Vector();
        SignatureAndHashAlgorithm signatureAndHashAlgorithm = SignatureAndHashAlgorithm.getInstance(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa);
        sigAlgVector.add(signatureAndHashAlgorithm);
        //这里是指和服务端证书一致的签名和Hash算法
        //Vector sigAlgVector = context.getSecurityParameters().getServerSigAlgsCert();
        //证书签发
        Vector certificateAuthorities = new Vector();

        TlsCertificate serverCer = context.getSecurityParameters().getLocalCertificate().getCertificateAt(0);
        //这里是指需要和服务端证书为同一签发 证书
        org.bouncycastle.asn1.x509.Certificate mServerCertificate = org.bouncycastle.asn1.x509.Certificate.getInstance(serverCer.getEncoded());

        //
        certificateAuthorities.add(mServerCertificate.getIssuer());

        return new CertificateRequest(certificateTypes, sigAlgVector, certificateAuthorities);
        //return super.getCertificateRequest();
    }

    private TlsCredentialedSigner loadSignerCredentials(short signatureAlgorithm) throws IOException {
        return TlsV2Utils.loadSignerCredentialsServer(
                context,
                PeripheralApplication.getContext(),
                new int[]{R.raw.ecc_relay_cert},
                R.raw.relay_private,
                getSupportedSignatureAlgorithms(),
                signatureAlgorithm);
    }


    protected Vector getSupportedSignatureAlgorithms() {
        if (TlsUtils.isTLSv12(context) && false/*config.serverAuthSigAlg != null*/) {
            Vector signatureAlgorithms = new Vector(1);
            //   signatureAlgorithms.addElement(config.serverAuthSigAlg);
            return signatureAlgorithms;
        }

        return context.getSecurityParametersHandshake().getClientSigAlgs();
    }

    public boolean handshakeFinished() {
        return handshakeFinished;
    }


}
