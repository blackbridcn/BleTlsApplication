package org.tls.utils;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

/**
 * Author: yuzzha
 * Date: 2021/12/22 15:40
 * Description:
 * Remark:
 */
public class SignersUtils {

    private static final byte[] SM2_ID = {
            (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38,
            (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38
    };
    public static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] RSASign(byte[] inData, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256WITHRSA", BC);
        signer.initSign(privateKey);
        signer.update(inData);
        return signer.sign();
    }

    public static boolean RSAVerifySign(byte[] inData, byte[] signature, PublicKey publicKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256WITHRSA", BC);
        signer.initVerify(publicKey);
        signer.update(inData);
        return signer.verify(signature);
    }

    public static byte[] SM2Sign(byte[] inData, PrivateKey privateKey) throws Exception {
        AsymmetricKeyParameter ecParam = PrivateKeyFactory.createKey(privateKey.getEncoded());
        SM2Signer sm2Signer = new SM2Signer();
        sm2Signer.init(true, new ParametersWithID(ecParam, SM2_ID));
        sm2Signer.update(inData, 0, inData.length);
        return sm2Signer.generateSignature();
    }


    public static boolean SM2VerifySign(byte[] inData, byte[] signature, PublicKey publicKey) throws Exception {
        AsymmetricKeyParameter ecParam = ECUtil.generatePublicKeyParameter(publicKey);
        SM2Signer sm2Signer = new SM2Signer();
        sm2Signer.init(false, new ParametersWithID(ecParam, SM2_ID));
        sm2Signer.update(inData, 0, inData.length);
        return sm2Signer.verifySignature(signature);
    }

    public static byte[] ECDSASign(byte[] inData, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256WITHECDSA", BC);
        signer.initSign(privateKey);
        signer.update(inData);
        return signer.sign();
    }

    public static boolean ECDSAVerifySign(byte[] inData, byte[] signature, PublicKey publicKey, String sigAlgName) throws Exception {
        Signature signer = Signature.getInstance(sigAlgName, "EC");
        //Signature signer = Signature.getInstance(sigAlgName, BC);
        signer.initVerify(publicKey);
        signer.update(inData);
        return signer.verify(signature);
    }


    public static boolean ECDSAVerifySign(byte[] inData, byte[] signature, Certificate certificate, String sigAlgName) throws Exception {
        Signature signer = Signature.getInstance("SHA256WITHECDSA", "EC");
        //Signature signer = Signature.getInstance(sigAlgName, BC);
        signer.initVerify(certificate);
        signer.update(inData);
        return signer.verify(signature);
    }

    /**
     * 验证签名
     *
     * @param inData     证书encoder 数据
     * @param signature  签名数据
     * @param publicKey  公钥
     * @param sigAlgName 签名算法
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static boolean verify(byte[] inData, byte[] signature,
                                 PublicKey publicKey, String sigAlgName)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signer = Signature.getInstance(sigAlgName, "EC");
        //Signature signer = Signature.getInstance(sigAlgName, BC);
        signer.initVerify(publicKey);
        signer.update(inData);
        return signer.verify(signature);
    }


    /**
     * 验证签名
     *
     * @param inData      需要验签证书encoder
     * @param signature   签名数据
     * @param certificate 根证书
     * @param sigAlgName  签名算法
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static boolean verify(byte[] inData, byte[] signature,
                                 Certificate certificate,
                                 String sigAlgName)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signer = Signature.getInstance(sigAlgName, "EC");
        //Signature signer = Signature.getInstance(sigAlgName, BC);
        signer.initVerify(certificate);
        signer.update(inData);
        return signer.verify(signature);
    }
}

