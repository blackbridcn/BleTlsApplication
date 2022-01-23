package org.tls12;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.ByteQueue;
import org.bouncycastle.tls.TlsClientProtocol;
import org.e.ble.utils.HexStrUtils;
import org.tls12.client.TlsV2Client;
import org.tls12.selector.TlsCryptoSelector;
import org.utils.LogUtils;

import java.io.IOException;
import java.security.Security;


/**
 * Author: yuzzha
 * Date: 2021-12-02 15:32
 * Description:
 * https://www.cnblogs.com/askta0/p/11237822.html#/c/subject/p/11237822.html
 * <p>
 * https://arith.blog.csdn.net/article/details/105322582?spm=1001.2101.3001.6650.9&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-9.highlightwordscore&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-9.highlightwordscore
 * <p>
 * https://github.com/RUB-NDS/TlsPraktikumServerOld2018
 * <p>
 * https://github.com/openjsse/openjsse
 * <p>
 * https://www.jianshu.com/p/7c9506612858
 * <p>
 * Remark:
 */
public class TlsClientUtils {

    private static final String TAG = TlsClientUtils.class.getSimpleName();

    private static TlsClientProtocol tlsClientProtocol;
    //
    private static TlsV2Client tlsV2Client;

    //
    public static byte[] startHandshake() {
        try {

            Security.addProvider(new BouncyCastleProvider());
            // TlsClientProtocol 进行tls client 端handshake 协议组装 封装类
            tlsClientProtocol = new TlsClientProtocol();
            // 公钥基础设施 Public Key Infrastructure 简称PKI,
            // PkiTlsClient 对象是对 TLS  handshake协议中的密钥PKI生成与处理类　
            tlsV2Client = new TlsV2Client(TlsCryptoSelector.getCrypto());
            tlsClientProtocol.connect(tlsV2Client);


            int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();
            byte[] data = new byte[dataAvailable];
            tlsClientProtocol.readOutput(data, 0, dataAvailable);
            return data;

            // 49324 COAC

            // 16 表示 ContentType Handshake
            // 03 03 表示 Version： TLS v1.2
            // 00 67 表示 长度 103

            // 01  表示 Handshake Type : 0x01: Client Hello;  0x02 Server Hello
            // 00 00 63 表示 Length 99
            // 03 03  Version: TLS v1.2
            // B2 B3 88 34  GMT Unix Time

            //Random Bytes 28
            // DD FD C9 57 A5 7D FA 52 61 85 4F 25 12 90 A7 9A
            // 8E 9F EE 35 4B 1F 31 49 9F 0A 3E 66

            // 00  Session ID  length
            // 00 04 Cipher Suite length 表示 2个密钥

            // C0 AC Cipher Suite : TLS_ECDHE_ECDSA_WITH_AES_128_CCM
            // 00 FF Cipher Suite : TLS_EMPTY_RENEGOTIATION_INFO_SCSV （0xFF）密码套件（缩写为SCSV） “安全重协商”机制
            // 01  Compression Methods length
            // 00  Compression Methods :null
            // 00 36 Extension length    T
            // LS扩展于2003年以一个独立的规范（RFC3456)被提出，之后被加入到TLS1.1和TLS1.2的版本中。
            // 它主要用于声明协议对某些新功能的支持，或者携带在握手进行中需要的额外数据。
            // TLS扩展机制使得协议在不改变本身基本行为的基础上增加了额外的功能，因此从出现以来，它就成为协议功能不断发展的主要载体
            //而每个扩展都以两个字节的表明扩展类型的字段开始，后面接着扩展的数据。
            //  全部的扩展类型可以在IANA的网站看到 http://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml
            //
            // 00 0A  Extension Name : supported_groups
            // 00 06 length
            // 00 04
            // 00 17 00 18

            // 00 0D  Extension Name : signature_algorithms
            // 00 20  length 32
            // 00 1E  signature  algorithms length  30
            // 02 01 03 01 04 01 05 01 06 01 02 02 03 02 04 02 05 02 06 02 02 03 03 03 04 03 05 03 06 03

            // 00 0B
            // 00 04
            // 03 00 01 02
        } catch (/*NoSuchAlgorithmException |*/ IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "IOException :" + e.getMessage());
        }
        return null;

    }


    public static byte[] continueHandshake(ByteQueue input) throws IOException {
        boolean handshakeFinished = false;
        byte[] data = null;
        /*try {*/
            int size = input.available();
            byte[] serverHello = new byte[size];
            input.read(serverHello, 0, size, 0);
            tlsClientProtocol.offerInput(serverHello);
            int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();

            if (dataAvailable != 0) {
                data = new byte[dataAvailable];
                tlsClientProtocol.readOutput(data, 0, dataAvailable);
                LogUtils.e(TAG, "----------------------> : " + HexStrUtils.Companion.byteArrayToHexString(data));
            }
            handshakeFinished = tlsV2Client.handshakeFinished;

            if(data!=null){
                return data;
            }

            LogUtils.e(TAG, "-----------------> handshakeFinished :" + handshakeFinished);
            if (handshakeFinished) {
                return data;
            }
            LogUtils.e(TAG, "handshakeFinished false ");

      /*  } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "Exception :" + e.getMessage());
        }*/
        return null;
    }


    public static byte[] continueHandshake(byte[] serverHello) {
        boolean handshakeFinished = false;
        byte[] data = null;
        try {


            tlsClientProtocol.offerInput(serverHello);

            int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();

            if (dataAvailable != 0) {
                data = new byte[dataAvailable];
                tlsClientProtocol.readOutput(data, 0, dataAvailable);
            }
            handshakeFinished = tlsV2Client.handshakeFinished;
            LogUtils.e(TAG, "-----------------> handshakeFinished :" + handshakeFinished);
            if (data!=null) {
                return data;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "Exception :" + e.getMessage());
        }
        return null;

    }

    public static byte[] offerInput(byte[] input){

        byte[] data = null;
        try {

            tlsClientProtocol.offerInput(input);

            int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();

            if (dataAvailable != 0) {
                data = new byte[dataAvailable];
                tlsClientProtocol.readOutput(data, 0, dataAvailable);
            }

            LogUtils.e(TAG, "-----------------> handshakeFinished :" + tlsV2Client.handshakeFinished);
            if (data!=null) {
                return data;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "Exception :" + e.getMessage());
        }
        return null;
    }


    public static void getAuth(){

    }


    public static boolean handshakeFinised(){
        return tlsV2Client.handshakeFinished();
    }

    public static byte[] wrapData(byte[] input) {
        try {
        tlsClientProtocol.offerInput(input, 0, input.length);

        int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();
        byte[] wrappedData = new byte[dataAvailable];
        tlsClientProtocol.readOutput(wrappedData, 0, wrappedData.length);
        return wrappedData;
          } catch (IOException e) {
            e.printStackTrace();
         }
         return null;
    }

    public static byte[] unWrapData(byte[] input) {
        try {
            tlsClientProtocol.offerInput(input);
            int dataAvailable = tlsClientProtocol.getAvailableInputBytes();
            if (dataAvailable != 0) {
                byte[] unwrappedData = new byte[dataAvailable];
                tlsClientProtocol.readInput(unwrappedData, 0, unwrappedData.length);
                return unwrappedData;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getPeerRelayId() {
        if (!tlsV2Client.handshakeFinished) {
            throw new RuntimeException("");
        }

        String cn = tlsV2Client.serverCertificate.getSerialNumber().toString();
        int id = Integer.parseInt(cn);
        return id;
    }


}
