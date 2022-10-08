package org.tls.v13.server;

import org.bouncycastle.tls.AbstractTlsServer;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.utlis.LogUtils;

import java.io.IOException;

public class TlsV3ServerImpl extends AbstractTlsServer {


    /**
     * RFC 8446
     * <p>
     * public static final int TLS_AES_128_GCM_SHA256 = 0x1301;
     * public static final int TLS_AES_256_GCM_SHA384 = 0x1302;
     * public static final int TLS_CHACHA20_POLY1305_SHA256 = 0x1303;
     * public static final int TLS_AES_128_CCM_SHA256 = 0x1304;
     * public static final int TLS_AES_128_CCM_8_SHA256 = 0x1305;
     * /*
     * RFC 8998
     * <p>
     * public static final int TLS_SM4_GCM_SM3 = 0x00C6;
     * public static final int TLS_SM4_CCM_SM3 = 0x00C7;
     */
    private static final int[] DEFAULT_CIPHER_SUITES = new int[]
            {
                    CipherSuite.TLS_AES_128_GCM_SHA256
            };

    public TlsV3ServerImpl(TlsCrypto crypto) {
        super(crypto);
    }

    @Override
    protected ProtocolVersion[] getSupportedVersions() {
        // .downTo(ProtocolVersion.TLSv10);
        ProtocolVersion[] versions = new ProtocolVersion[1];
        versions[0] = ProtocolVersion.TLSv13;
        return ProtocolVersion.TLSv13.downTo(ProtocolVersion.TLSv13);
    }


    protected int[] getSupportedCipherSuites() {
        return TlsUtils.getSupportedCipherSuites(getCrypto(), DEFAULT_CIPHER_SUITES);
    }


    @Override
    public TlsCredentials getCredentials() throws IOException {
        int keyExchangeAlgorithm = context.getSecurityParametersHandshake().getKeyExchangeAlgorithm();
        LogUtils.e("TAG","----------> keyExchangeAlgorithm:"+keyExchangeAlgorithm);
        /*
         * TODO[tls13] Should really be finding the first client-supported signature scheme that the
         * server also supports and has credentials for.
         */
        if (TlsUtils.isTLSv13(context)) {
            return getECDSASignerCredentials();
        }
        return getECDSASignerCredentials();
    }

    protected TlsCredentialedSigner getECDSASignerCredentials() throws IOException {
        // [RFC 8422] Code should choose based on client's  supported sig algs?
        /*TlsLocalResUtils.buildSignerCredentials(
                context,
                BaseApplication.getContext(),
                new int[]{R.raw.ecc_relay_cert},
                R.raw.relay_private,
                getSupportedSignatureAlgorithms(),
                signatureAlgorithm);
        return TlsLocalResUtils.buildSignerCredentials(SignatureAlgorithm.ecdsa);*/
        return  null;
    }
}
