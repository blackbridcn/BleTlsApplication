package org.tls.v13.client;


import org.bouncycastle.tls.AbstractTlsClient;
import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.PskKeyExchangeMode;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsClientContext;
import org.bouncycastle.tls.TlsExtensionsUtils;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCrypto;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class TlsV3Client extends AbstractTlsClient {

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

    public TlsV3Client(TlsCrypto crypto) {
        super(crypto);
    }

    @Override
    public void init(TlsClientContext context) {
        super.init(context);

    }

    @Override
    protected ProtocolVersion[] getSupportedVersions() {
        // .downTo(ProtocolVersion.TLSv10);
        ProtocolVersion[] versions = new ProtocolVersion[1];
        versions[0] = ProtocolVersion.TLSv13;
        return ProtocolVersion.TLSv13.downTo(ProtocolVersion.TLSv12);
    }

    @Override
    protected int[] getSupportedCipherSuites() {
        return TlsUtils.getSupportedCipherSuites(getCrypto(), DEFAULT_CIPHER_SUITES);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {

        return new TlsClientAuthentication(this.context, this);
    }


    public Hashtable getClientExtensions()
            throws IOException {

        Hashtable clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(super.getClientExtensions());
        short[] pskKeyExchangeModes =new short[1];
        pskKeyExchangeModes[0]= PskKeyExchangeMode.psk_dhe_ke;

        TlsExtensionsUtils.addPSKKeyExchangeModesExtension(clientExtensions, pskKeyExchangeModes);

        Vector<SignatureAndHashAlgorithm> sSignatureAlgorithms =new Vector<SignatureAndHashAlgorithm>();

        sSignatureAlgorithms.add(SignatureAndHashAlgorithm.ecdsa_brainpoolP256r1tls13_sha256);// ecdsa_secp256r1_sha256

        // SignatureAlgorithms 签名算法，在tls1.2可以根据 CipherSuite 规则，
        // 但是tls1.3则必须专门指定（是由CA证书和私钥签名算法决定的）。
        TlsExtensionsUtils.addSignatureAlgorithmsExtension(clientExtensions,sSignatureAlgorithms);

        return clientExtensions;
    }

}

