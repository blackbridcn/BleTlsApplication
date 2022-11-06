package org.tls.peer.client;

import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.DefaultTlsClient;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsClientContext;
import org.bouncycastle.tls.TlsExtensionsUtils;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.utlis.LogUtils;


import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Author: yuzzha
 * Date: 2021-12-02 16:42
 * Description: 公钥基础设施 Public Key Infrastructure 简称PKI,
 * Remark:
 * <p>
 * 　TLS　协议中对　加密算法、公钥、私钥　处理的封装类
 */
public class TlsClient extends DefaultTlsClient {

    public Boolean handshakeFinished = false;

    public X509Certificate serverCertificate;

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
                    //tls 1.2
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM,
                    //tls1.3
                    CipherSuite.TLS_AES_128_GCM_SHA256
            };


    public TlsClient(TlsCrypto crypto) {
        super(crypto);
    }

    @Override
    public void init(TlsClientContext tlsClientContext) {
        super.init(tlsClientContext);
        handshakeFinished=false;
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
        //证书鉴定
        return new ClientTlsAuthentication(this,this.context);
    }

    protected ProtocolVersion[] getSupportedVersions() {
        // [tls13] Enable TLSv13 by default in due course
        return ProtocolVersion.TLSv12.downTo(ProtocolVersion.TLSv12);
    }


    //
    @Override
    public int[] getCipherSuites() {
        //这里是指　ＴＬＳ中Client端支持加密算法
        //https://datatracker.ietf.org/doc/html/rfc4492#section-2.2
        TlsUtils.getSupportedCipherSuites(getCrypto(), DEFAULT_CIPHER_SUITES);
        return DEFAULT_CIPHER_SUITES;
    }

    public Hashtable getClientExtensions()
            throws IOException {

        Hashtable clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(super.getClientExtensions());
        /**
         * ECC 需要传参椭圆曲线参数
         *
         * https://datatracker.ietf.org/doc/html/rfc4492#section-5.1.1
         *
         * Supported Elliptic Curves Extension
         *
         *         enum {
         *             sect163k1 (1), sect163r1 (2), sect163r2 (3),
         *             sect193r1 (4), sect193r2 (5), sect233k1 (6),
         *             sect233r1 (7), sect239k1 (8), sect283k1 (9),
         *             sect283r1 (10), sect409k1 (11), sect409r1 (12),
         *             sect571k1 (13), sect571r1 (14), secp160k1 (15),
         *             secp160r1 (16), secp160r2 (17), secp192k1 (18),
         *             secp192r1 (19), secp224k1 (20), secp224r1 (21),
         *             secp256k1 (22), secp256r1 (23), secp384r1 (24),
         *             secp521r1 (25),
         *             reserved (0xFE00..0xFEFF),
         *             arbitrary_explicit_prime_curves(0xFF01),
         *             arbitrary_explicit_char2_curves(0xFF02),
         *             (0xFFFF)
         *         } NamedCurve;
         *
         *         secp256k1
         */


        Vector<Integer> vector=  new Vector<Integer>();
        // secp256k1
        vector.add(22);

        TlsExtensionsUtils.addSupportedGroupsExtension(clientExtensions,vector);

        return clientExtensions;
    }


    @Override
    public void notifyHandshakeComplete() throws IOException {
        super.notifyHandshakeComplete();
        LogUtils.e("TAG", "----------------------> notifyHandshakeComplete  handshakeFinished = true");
        handshakeFinished = true;
    }

    public boolean handshakeFinished(){
        return handshakeFinished;
    }

}
