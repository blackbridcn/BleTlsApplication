package org.tls12.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.RawRes;

import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.ble.utils.HexStrUtils;
import org.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;


/**
 * Author: yuzzha
 * Date: 2021-12-07 14:41
 * Description:
 * Remark:
 */
public class X509CertificateUtils {

    public static InputStream readRawFile(Context mContext, @RawRes int id){
        Resources resources=mContext.getResources();
        InputStream inputStream=resources.openRawResource(id);
        return inputStream;
    }

    public static void x509Test(Context mContext,@RawRes int id)  {
        //创建X509工厂类
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //创建证书对象
            //rsaEntityCert.cer
            InputStream inputStream=readRawFile(mContext, id);
            // inStream证书的传入数据
            X509Certificate oCert = (X509Certificate) cf.generateCertificate(inputStream);
            inputStream.close();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String info = null;
            //获得证书版本
            info = String.valueOf(oCert.getVersion());
            System.out.println("证书版本:" + info);
            //获得证书序列号
            info = oCert.getSerialNumber().toString(16);
            System.out.println("证书序列号:" + info);
            //获得证书有效期
            Date beforedate = oCert.getNotBefore();
            info = dateformat.format(beforedate);
            System.out.println("证书生效日期:" + info);
            Date afterdate = oCert.getNotAfter();
            info = dateformat.format(afterdate);
            System.out.println("证书失效日期:" + info);
            //获得证书主体信息
            info = oCert.getSubjectDN().getName();
            System.out.println("证书拥有者:" + info);
            //获得证书颁发者信息
            info = oCert.getIssuerDN().getName();
            System.out.println("证书颁发者:" + info);
            //获得证书签名算法名称
            info = oCert.getSigAlgName();
            System.out.println("证书签名算法:" + info);

            byte[] signature = oCert.getSignature();
            if(signature!=null){
                System.out.println("signature:" +  HexStrUtils.INSTANCE.byteArrayToHexString(signature));
            }

            PublicKey publicKey=oCert.getPublicKey();

            if(publicKey!=null &&publicKey.getEncoded()!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    System.out.println("public key :"+new String(Base64.getEncoder().encode(publicKey.getEncoded())));
                }

            }
            byte[] byt = oCert.getExtensionValue("1.2.86.11.7.9");
            if(byt!=null){
                String strExt = new String(byt);
                System.out.println("证书扩展域:" + strExt);
                byt = oCert.getExtensionValue("1.2.86.11.7.1.8");
                String strExt2 = new String(byt);
                System.out.println("证书扩展域2:" + strExt2);

            }
        } catch (CertificateException e) {
            e.printStackTrace();
            LogUtils.e("TAG","------------------>CertificateException  "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("TAG","------------------>IOException  "+e.getMessage());
        }
    }

    public static void logX509(X509Certificate oCert){
        try {
           // CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //创建证书对象
            //rsaEntityCert.cer

            // inStream证书的传入数据
            //X509Certificate oCert = (X509Certificate) cf.generateCertificate(inputStream);
            //inputStream.close();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String info = null;
            //获得证书版本
            info = String.valueOf(oCert.getVersion());
            System.out.println("证书版本:" + info);
            //获得证书序列号
            info = oCert.getSerialNumber().toString(16);
            System.out.println("证书序列号:" + info);
            //获得证书有效期
            Date beforedate = oCert.getNotBefore();
            info = dateformat.format(beforedate);
            System.out.println("证书生效日期:" + info);
            Date afterdate = oCert.getNotAfter();
            info = dateformat.format(afterdate);
            System.out.println("证书失效日期:" + info);
            //获得证书主体信息
            info = oCert.getSubjectDN().getName();
            System.out.println("证书拥有者:" + info);
            //获得证书颁发者信息
            info = oCert.getIssuerDN().getName();
            System.out.println("证书颁发者:" + info);
            //获得证书签名算法名称
            info = oCert.getSigAlgName();
            System.out.println("证书签名算法:" + info);

            byte[] signature = oCert.getSignature();
            if(signature!=null){
                System.out.println("signature:" +  HexStrUtils.INSTANCE.byteArrayToHexString(signature));
            }

            PublicKey publicKey=oCert.getPublicKey();

            if(publicKey!=null &&publicKey.getEncoded()!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    System.out.println("public key :"+new String(Base64.getEncoder().encode(publicKey.getEncoded())));
                }

            }
            byte[] byt = oCert.getExtensionValue("1.2.86.11.7.9");
            if(byt!=null){
                String strExt = new String(byt);
                System.out.println("证书扩展域:" + strExt);
                byt = oCert.getExtensionValue("1.2.86.11.7.1.8");
                String strExt2 = new String(byt);
                System.out.println("证书扩展域2:" + strExt2);

            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("TAG","------------------>IOException  "+e.getMessage());
        }
    }


    public static Certificate getX509Certificate(Context mContext, @RawRes int id){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //创建证书对象
            //rsaEntityCert.cer
            Resources resources= mContext.getResources();
            InputStream inputStream=resources.openRawResource(id);
            Certificate certificate= cf.generateCertificate(inputStream);
            return certificate;
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void rawX509CertificateInfo(Context mContext, @RawRes int id){
        //创建X509工厂类
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //创建证书对象
            //rsaEntityCert.cer
            Resources resources=mContext.getResources();
            InputStream inputStream=resources.openRawResource(id);
            // inStream证书的传入数据
            X509Certificate oCert = (X509Certificate) cf.generateCertificate(inputStream);

            inputStream.close();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String info = null;
            //获得证书版本
            info = String.valueOf(oCert.getVersion());
            System.out.println("证书版本:" + info);
            //获得证书序列号
            info = oCert.getSerialNumber().toString(16);
            System.out.println("证书序列号:" + info);
            //获得证书有效期
            Date beforedate = (Date) oCert.getNotBefore();
            info = dateformat.format(beforedate);
            System.out.println("证书生效日期:" + info);
            Date afterdate = oCert.getNotAfter();
            info = dateformat.format(afterdate);
            System.out.println("证书失效日期:" + info);
            //获得证书主体信息
            info = oCert.getSubjectDN().getName();
            System.out.println("证书拥有者:" + info);
            //获得证书颁发者信息
            info = oCert.getIssuerDN().getName();
            System.out.println("证书颁发者:" + info);
            //获得证书签名算法名称
            info = oCert.getSigAlgName();
            System.out.println("证书签名算法:" + info);

            byte[] signature = oCert.getSignature();
            if(signature!=null){
                System.out.println("signature:" +  HexStrUtils.INSTANCE.byteArrayToHexString(signature));
            }

            PublicKey publicKey=oCert.getPublicKey();

            if(publicKey!=null &&publicKey.getEncoded()!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    System.out.println("public key :"+new String(Base64.getEncoder().encode(publicKey.getEncoded())));
                }
            }
            byte[] byt = oCert.getExtensionValue("1.2.86.11.7.9");
            if(byt!=null){
                String strExt = new String(byt);
                System.out.println("证书扩展域:" + strExt);
                byt = oCert.getExtensionValue("1.2.86.11.7.1.8");
                String strExt2 = new String(byt);
                System.out.println("证书扩展域2:" + strExt2);
            }
        } catch (CertificateException e) {
            e.printStackTrace();
            LogUtils.e("TAG","------------------>CertificateException  "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("TAG","------------------>IOException  "+e.getMessage());
        }
    }

    public static int getKeyLength(final PublicKey pk) {
        int len = -1;
        if (pk instanceof RSAPublicKey) {
            final RSAPublicKey rsapub = (RSAPublicKey) pk;
            len = rsapub.getModulus().bitLength();
        } else if (pk instanceof JCEECPublicKey) {
            final JCEECPublicKey ecpriv = (JCEECPublicKey) pk;
            final org.bouncycastle.jce.spec.ECParameterSpec spec = ecpriv.getParameters();
            if (spec != null) {
                len = spec.getN().bitLength();
            } else {
                // We support the key, but we don't know the key length
                len = 0;
            }
        } else if (pk instanceof ECPublicKey) {
            final ECPublicKey ecpriv = (ECPublicKey) pk;
            final java.security.spec.ECParameterSpec spec = ecpriv.getParams();
            if (spec != null) {
                len = spec.getOrder().bitLength(); // does this really return something we expect?
            } else {
                // We support the key, but we don't know the key length
                len = 0;
            }
        } else if (pk instanceof DSAPublicKey) {
            final DSAPublicKey dsapub = (DSAPublicKey) pk;
            if ( dsapub.getParams() != null ) {
                len = dsapub.getParams().getP().bitLength();
            } else {
                len = dsapub.getY().bitLength();
            }
        }
        return len;
    }



}
