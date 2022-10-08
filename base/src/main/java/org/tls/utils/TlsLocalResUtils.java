package org.tls.utils;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.RawRes;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.CertificateEntry;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SignatureAlgorithm;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.TlsContext;
import org.bouncycastle.tls.TlsCredentialedSigner;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.tls.crypto.TlsCryptoParameters;
import org.bouncycastle.tls.crypto.impl.bc.BcDefaultTlsCredentialedSigner;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaDefaultTlsCredentialedSigner;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCrypto;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Hashtable;

public class TlsLocalResUtils {


    public static String getResourceName(short signatureAlgorithm) throws IOException {
        switch (signatureAlgorithm) {
            case SignatureAlgorithm.rsa:
            case SignatureAlgorithm.rsa_pss_rsae_sha256:
            case SignatureAlgorithm.rsa_pss_rsae_sha384:
            case SignatureAlgorithm.rsa_pss_rsae_sha512:
                return "rsa";
            case SignatureAlgorithm.dsa:
                return "dsa";
            case SignatureAlgorithm.ecdsa:
                return "ecdsa";
            case SignatureAlgorithm.ed25519:
                return "ed25519";
            case SignatureAlgorithm.ed448:
                return "ed448";
            case SignatureAlgorithm.rsa_pss_pss_sha256:
                return "rsa_pss_256";
            case SignatureAlgorithm.rsa_pss_pss_sha384:
                return "rsa_pss_384";
            case SignatureAlgorithm.rsa_pss_pss_sha512:
                return "rsa_pss_512";
            default:
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }


    /**
     * 加载证书签名的对象
     *
     * 签名是指：非对称中的私钥加密
     *
     * @param context
     * @param mContext
     * @param certResources
     * @param keyResource
     * @param signatureAndHashAlgorithm
     * @return
     * @throws IOException
     */
    public static TlsCredentialedSigner buildSignerCredentials(
            TlsContext context,
            Context mContext,
            @RawRes int[] certResources,
            @RawRes int keyResource,
            SignatureAndHashAlgorithm signatureAndHashAlgorithm
    )
            throws IOException {

        TlsCryptoParameters cryptoParams = new TlsCryptoParameters(context);

        TlsCrypto crypto = context.getCrypto();

        Certificate certificate = loadCertificateChain(cryptoParams.getServerVersion(), crypto, mContext, certResources);
        // [tls-ops] Need to have TlsCrypto construct the credentials from the certs/key (as raw data)
        if (crypto instanceof BcTlsCrypto) {
            //基于BC 库的私钥
            AsymmetricKeyParameter privateKey = buildBcPrivateKeyResource(mContext, keyResource);

            return new BcDefaultTlsCredentialedSigner(cryptoParams, (BcTlsCrypto) crypto, privateKey, certificate, signatureAndHashAlgorithm);
        } else {
            JcaTlsCrypto jcaCrypto = (JcaTlsCrypto) crypto;
            //基于 JCA（ Java加密体系结构 Java Cryptography Architecture，JDK内部实现），加载私钥
            PrivateKey privateKey = buildJCAPrivateKeyResource(jcaCrypto, mContext, keyResource);

            return new JcaDefaultTlsCredentialedSigner(cryptoParams, jcaCrypto, privateKey, certificate, signatureAndHashAlgorithm);
        }
    }


    /**
     *
     * 采用BC库加载私钥参数
     *
     *
     * @param mContext
     * @param resource
     * @return
     * @throws IOException
     */
    private static AsymmetricKeyParameter buildBcPrivateKeyResource(Context mContext, @RawRes int resource)
            throws IOException {
        PemObject pem = loadPemResource(mContext, resource);
        if (pem.getType().equals("PRIVATE KEY")) {
            return PrivateKeyFactory.createKey(pem.getContent());
        }
        if (pem.getType().equals("ENCRYPTED PRIVATE KEY")) {
            throw new UnsupportedOperationException("Encrypted PKCS#8 keys not supported");
        }
        if (pem.getType().equals("RSA PRIVATE KEY")) {
            RSAPrivateKey rsa = RSAPrivateKey.getInstance(pem.getContent());
            return new RSAPrivateCrtKeyParameters(rsa.getModulus(), rsa.getPublicExponent(),
                    rsa.getPrivateExponent(), rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(),
                    rsa.getExponent2(), rsa.getCoefficient());
        }
        if (pem.getType().equals("EC PRIVATE KEY")) {
            ECPrivateKey pKey = ECPrivateKey.getInstance(pem.getContent());

            AlgorithmIdentifier algId = new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, pKey.getParameters());

            PrivateKeyInfo privInfo = new PrivateKeyInfo(algId, pKey);
            return PrivateKeyFactory.createKey(privInfo);
        }
        throw new IllegalArgumentException("'resource' doesn't specify a valid private key");
    }

    /**
     * 采用JCA加载构建私钥PrivateKey 函数
     *
     *  JCA：Java Cryptography Architecture，Java加密体系结构 ，就是指 JDK内部实现
     *
     * @param crypto
     * @param mContext
     * @param resource
     * @return
     * @throws IOException
     */
    private static PrivateKey buildJCAPrivateKeyResource(JcaTlsCrypto crypto, Context mContext, @RawRes int resource)
            throws IOException {
        Throwable cause = null;
        try {
            PemObject pem = loadPemResource(mContext, resource);
            if (pem.getType().equals("PRIVATE KEY")) {
                return buildJcaPKCS8PrivateKey(crypto, pem.getContent());
            }
            if (pem.getType().equals("ENCRYPTED PRIVATE KEY")) {
                throw new UnsupportedOperationException("Encrypted PKCS#8 keys not supported");
            }
            if (pem.getType().equals("RSA PRIVATE KEY")) {
                RSAPrivateKey rsa = RSAPrivateKey.getInstance(pem.getContent());

                KeyFactory keyFact = crypto.getHelper().createKeyFactory("RSA");
                return keyFact.generatePrivate(new RSAPrivateCrtKeySpec(rsa.getModulus(), rsa.getPublicExponent(),
                        rsa.getPrivateExponent(), rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(), rsa.getExponent2(),
                        rsa.getCoefficient()));
            }
        } catch (GeneralSecurityException e) {
            cause = e;
        }
        throw new IllegalArgumentException("'resource' doesn't specify a valid private key", cause);
    }


    /**
     *
     * 基于JCA 加载私钥
     *
     * PKCS#8：私钥信息语法标准
     *
     *
     * @param crypto
     * @param encoded
     * @return
     * @throws GeneralSecurityException
     */
    public static PrivateKey buildJcaPKCS8PrivateKey(JcaTlsCrypto crypto, byte[] encoded)
            throws GeneralSecurityException {

        PrivateKeyInfo pki = PrivateKeyInfo.getInstance(encoded);
        AlgorithmIdentifier algID = pki.getPrivateKeyAlgorithm();
        ASN1ObjectIdentifier oid = algID.getAlgorithm();

        String name;
        if (X9ObjectIdentifiers.id_dsa.equals(oid)) {
            name = "DSA";
        } else if (X9ObjectIdentifiers.id_ecPublicKey.equals(oid)) {
            // TODO Try ECDH/ECDSA according to intended use?
            name = "EC";
        } else if (PKCSObjectIdentifiers.rsaEncryption.equals(oid)
                || PKCSObjectIdentifiers.id_RSASSA_PSS.equals(oid)) {
            name = "RSA";
        } else if (EdECObjectIdentifiers.id_Ed25519.equals(oid)) {
            name = "Ed25519";
        } else if (EdECObjectIdentifiers.id_Ed448.equals(oid)) {
            name = "Ed448";
        } else {
            name = oid.getId();
        }

        KeyFactory kf = crypto.getHelper().createKeyFactory(name);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }


    /**
     * 构建Tls证书对象 Certificate
     * <p>
     * X.509证书链
     * <p>
     * <p>
     * <p>
     * <p>
     * 加载Tls (X.509证书)链
     *
     * @param protocolVersion tls版本
     * @param crypto          tls 加密
     * @param mContext        Android Context 对象
     * @param resources       Android  raw tls(X509证书)资源对象
     * @return
     * @throws IOException
     */
    public static Certificate loadCertificateChain(
            ProtocolVersion protocolVersion,
            TlsCrypto crypto,
            Context mContext,
            @RawRes int[] resources)
            throws IOException {
        //
        if (org.bouncycastle.tls.TlsUtils.isTLSv13(protocolVersion)) {
            CertificateEntry[] certificateEntryList = new CertificateEntry[resources.length];
            for (int i = 0; i < resources.length; ++i) {

                TlsCertificate certificate = loadCertificateResource(crypto, mContext, resources[i]);
                // [tls13] Add possibility of specifying e.g. CertificateStatus
                Hashtable extensions = null;
                certificateEntryList[i] = new CertificateEntry(certificate, extensions);
            }
            // [tls13] Support for non-empty request context
            byte[] certificateRequestContext = org.bouncycastle.tls.TlsUtils.EMPTY_BYTES;
            return new Certificate(certificateRequestContext, certificateEntryList);
        } else {
            //
            TlsCertificate[] chain = new TlsCertificate[resources.length];
            for (int i = 0; i < resources.length; ++i) {
                chain[i] = loadCertificateResource(crypto, mContext, resources[i]);
            }
            return new Certificate(chain);
        }
    }

    /**
     * 加载X509证书 ，并转化为TlsCertificate 对象
     *
     * @param crypto
     * @param mContext Android Context 对象
     * @param raw      Android  raw tls(X509证书)资源对象id
     * @return
     * @throws IOException
     */
    public static TlsCertificate loadCertificateResource(TlsCrypto crypto, Context mContext, @RawRes int raw)
            throws IOException {
        PemObject pem = loadPemResource(mContext, raw);
        if (pem.getType().endsWith("CERTIFICATE")) {
            return crypto.createCertificate(pem.getContent());
        }
        throw new IllegalArgumentException("'resource' doesn't specify a valid certificate");
    }


    public static PemObject loadPemResource(Context mContext, @RawRes int raw) throws IOException {
        Resources resources = mContext.getResources();
        InputStream inputStream = resources.openRawResource(raw);
        PemReader p = new PemReader(new InputStreamReader(inputStream));
        PemObject o = p.readPemObject();
        p.close();
        return o;
    }

}
