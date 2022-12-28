package org.tls.peer.server;


import com.train.base.R;
import com.train.base.application.BaseApplication;

import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.ClientCertificateType;
import org.bouncycastle.tls.DefaultTlsServer;
import org.bouncycastle.tls.HashAlgorithm;
import org.bouncycastle.tls.NamedGroup;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsServerContext;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.tls.utils.TlsNativeUtils;
import org.utlis.LogUtils;


import java.io.IOException;
import java.util.Vector;

/**
 * Author: yuzzha
 * Date: 2021/12/9 17:03
 * Description:
 * Remark:
 */
public class TlsServer extends DefaultTlsServer {


    private static final String TAG = TlsServer.class.getSimpleName();

    private static final int[] DEFAULT_CIPHER_SUITES = new int[]
            {
                    //tls 1.2
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM,
                    //tls1.3
                   // CipherSuite.TLS_AES_128_GCM_SHA256
            };

    public Boolean handshakeFinished = false;

    public TlsServer(TlsCrypto crypto) {
        super(crypto);
    }

    @Override
    public void init(TlsServerContext tlsServerContext) {
        super.init(tlsServerContext);
        handshakeFinished = false;
    }

    @Override
    public int[] getCipherSuites() {

        return DEFAULT_CIPHER_SUITES;
    }

    @Override
    protected int[] getSupportedCipherSuites() {
        //return super.getSupportedCipherSuites();
        return DEFAULT_CIPHER_SUITES;
    }


    @Override
    public int[] getSupportedGroups() throws IOException {
        //return super.getSupportedGroups();
        return new int[]{ NamedGroup.secp256k1 };
        //secp256k1
    }

    //配置tls Server 支持tls版本
    protected ProtocolVersion[] getSupportedVersions() {

        return ProtocolVersion.TLSv13.downTo(ProtocolVersion.TLSv12);
    }

    @Override
    public void notifyHandshakeComplete() throws IOException {
        super.notifyHandshakeComplete();
        handshakeFinished = true;
        LogUtils.e(TAG, "TlsServer notifyHandshakeComplete ----------------> ");
    }

    @Override
    public void notifyClientCertificate(Certificate certificate) throws IOException {
       // super.notifyClientCertificate(certificate);
        LogUtils.e(TAG, "TlsServer notifyClientCertificate ----------------> ");
    }

    @Override
    public void notifyOfferedCipherSuites(int[] ints) throws IOException {
        super.notifyOfferedCipherSuites(ints);
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < ints.length; i++) {
            sb.append(Integer.toHexString(ints[i]));
        }
        LogUtils.e(TAG, "TlsServer notifyOfferedCipherSuites  客户端提供 CipherSuites: " + sb.toString());
    }

    @Override
    public void notifyClientVersion(ProtocolVersion protocolVersion) throws IOException {
        super.notifyClientVersion(protocolVersion);
        LogUtils.e(TAG, "TlsServer notifyClientVersion  客户段支持对tls 版本" + protocolVersion.toString());
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
        return TlsNativeUtils.loadSignerCredentialsServer(
                context,
                BaseApplication.getContext(),
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
