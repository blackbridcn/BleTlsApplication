package org.tls.peer.server;


import org.ble.utils.HexStrUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.TlsServerProtocol;
import org.tls.selector.TlsCryptoSelector;
import org.utlis.LogUtils;


import java.io.IOException;
import java.security.Security;

/**
 * Author: yuzzha
 * Date: 2021/12/9 17:04
 * Description:
 * Remark:
 * (TLS1.2连接过程解析 - 基于ECDHE密钥交换算法)
 * https://blog.csdn.net/qq_38975553/article/details/112987893
 */
public class TlsServerUtils {

    static String TAG = TlsServerUtils.class.getSimpleName();
    static TlsServer tlsV2Server;
    static TlsServerProtocol tlsServerProtocol;

    static {
        //握手随机数
        //var rngProvider = new System.Security.Cryptography.RNGCryptoServiceProvider();
        //rngProvider.GetBytes(random);
        //SHA256PRNG 表示 采用SHA256算法 + PRNG生成伪随机数
        //SecureRandom secureRandom = SecureRandom.getInstance("SHA256PRNG");
        try {
            Security.addProvider(new BouncyCastleProvider());
            tlsServerProtocol = new TlsServerProtocol();
            tlsV2Server = new TlsServer(TlsCryptoSelector.getCrypto());
            tlsServerProtocol.accept(tlsV2Server);
            LogUtils.e(TAG, " TlsServerUtils static {}  ");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, " static {}  IOException :" + e.getMessage());
        }
    }


    public static byte[] offerInput(byte[] clientMsg) {
        try {
            tlsServerProtocol.offerInput(clientMsg);
            int availableOutputBytes = tlsServerProtocol.getAvailableOutputBytes();
            LogUtils.e("TAG", "----------> availableOutputBytes :" + availableOutputBytes);
            if (availableOutputBytes > 0) {
                byte[] data = new byte[availableOutputBytes];
                tlsServerProtocol.readOutput(data, 0, availableOutputBytes);
                return data;
            }
        } catch (IOException e) {
            LogUtils.e("TAG", "-------->offerInput IOException: " + e.getMessage());
        }
        return null;

    }


    public static byte[] wrapData(byte[] input) {
        try {
            tlsServerProtocol.writeApplicationData(input, 0, input.length);
            // tlsClientProtocol.offerOutput(input, 0, input.length);
            int dataAvailable = tlsServerProtocol.getAvailableOutputBytes();
            byte[] wrappedData = new byte[dataAvailable];
            tlsServerProtocol.readOutput(wrappedData, 0, wrappedData.length);
            return wrappedData;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("TAG", "-------->wrapData IOException: " + e.getMessage());
        }
        return null;
    }

    public static byte[] unWrapData(byte[] input) {
        try {
            tlsServerProtocol.offerInput(input, 0, input.length);
            //                                    applicationDataAvailable
            int dataAvailable = tlsServerProtocol.applicationDataAvailable();
            if (dataAvailable != 0) {
                byte[] unwrappedData = new byte[dataAvailable];
                tlsServerProtocol.readApplicationData(unwrappedData, 0, unwrappedData.length);
                return unwrappedData;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("TAG", " IOException : " + e.getMessage());
        }
        return null;
    }

    public static boolean handshakeFinished() {
        return tlsV2Server.handshakeFinished();
    }


}
