package org.tls12;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.TlsServerProtocol;
import org.tls12.selector.TlsCryptoSelector;
import org.tls12.server.TlsV2Server;
import org.utils.LogUtils;

import java.io.IOException;
import java.security.Security;

/**
 * Author: yuzzha
 * Date: 2021/12/9 17:04
 * Description:
 * Remark:
 * https://blog.csdn.net/qq_38975553/article/details/112987893
 */
public class TlsServerUtils {

    static TlsV2Server tlsV2Server;
    static TlsServerProtocol tlsServerProtocol;

    public static byte[] receiverClientHello(byte[] hello) {
        try {
            //握手随机数

            Security.addProvider(new BouncyCastleProvider());
            //var rngProvider = new System.Security.Cryptography.RNGCryptoServiceProvider();
            //rngProvider.GetBytes(random);
            //SHA256PRNG 表示 采用SHA256算法 + PRNG生成伪随机数
            //SecureRandom secureRandom = SecureRandom.getInstance("SHA256PRNG");

            tlsServerProtocol = new TlsServerProtocol();
            tlsV2Server = new TlsV2Server(TlsCryptoSelector.getCrypto());
            tlsServerProtocol.accept(tlsV2Server);
            tlsServerProtocol.offerInput(hello);
            int availableOutputBytes = tlsServerProtocol.getAvailableOutputBytes();
            if (availableOutputBytes > 0) {
                byte[] data = new byte[availableOutputBytes];
                tlsServerProtocol.readOutput(data, 0, availableOutputBytes);
              //  LogUtils.e("TAG", "-------------------------> availableOutputBytes value: " + ByteHexUtils.INSTANCE.byteArrayToHexString(data));
                return data;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] reciver(byte[] clientMsg) throws IOException {
        /*try {*/
            tlsServerProtocol.offerInput(clientMsg);
            int availableOutputBytes = tlsServerProtocol.getAvailableOutputBytes();
            if (availableOutputBytes > 0) {
                byte[] data = new byte[availableOutputBytes];
                tlsServerProtocol.readOutput(data, 0, availableOutputBytes);
               // LogUtils.e("TAG", "-------------------------> availableOutputBytes value: " + ByteHexUtils.INSTANCE.byteArrayToHexString(data));
                return data;
            }
       /* } catch (IOException e) {
            LogUtils.e("TAG", "--------> " + e.getMessage());
        }*/
        return null;

    }


    public static byte[] wrapData(byte[] input) {
        // tlsClientProtocol.offerOutput(input, 0, input.length);
        int dataAvailable = tlsServerProtocol.getAvailableOutputBytes();
        byte[] wrappedData = new byte[dataAvailable];
        tlsServerProtocol.readOutput(wrappedData, 0, wrappedData.length);
        return wrappedData;
        //  } catch (IOException e) {
        //    e.printStackTrace();
        // }
        // return null;
    }

    public static byte[] unWrapData(byte[] input) {
        try {
            tlsServerProtocol.offerInput(input);
            int dataAvailable = tlsServerProtocol.getAvailableInputBytes();
            if (dataAvailable != 0) {
                byte[] unwrappedData = new byte[dataAvailable];
                tlsServerProtocol.readInput(unwrappedData, 0, unwrappedData.length);
                return unwrappedData;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("TAG"," IOException : "+e.getMessage());
        }
        return null;
    }

    public static boolean handshakeFinised(){
        return tlsV2Server.handshakeFinished();
    }



//1603030067010000630303CA2AC10FE92D144208F31DD507C4A8AABF57A9C2AC2BC538685A7E3ADC3AB52C000004C0AC00FF01000036000A0006000400170018000D0020001E020103010401050106010202030204020502060202030303040305030603000B000403000102
}
