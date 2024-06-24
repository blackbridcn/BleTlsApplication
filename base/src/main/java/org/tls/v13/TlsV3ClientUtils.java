package org.tls.v13;

import org.tls.v13.client.TlsV3Client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.TlsClientProtocol;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCryptoProvider;
import org.utlis.LogUtils;

import java.io.IOException;
import java.security.Security;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class TlsV3ClientUtils {

    private static String TAG = TlsV3ClientUtils.class.getSimpleName();

    private static TlsClientProtocol tlsClientProtocol;

    private static TlsV3Client tlsV3Client;

    public static byte[] startHello() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            // TlsClientProtocol 进行tls client 端handshake 协议组装 封装类
            tlsClientProtocol = new TlsClientProtocol();
           // tlsV3Client = new TlsV3Client(TlsCryptoSelector.getCrypto());
            tlsV3Client = new TlsV3Client(new JcaTlsCryptoProvider().setProvider(new BouncyCastleProvider()).create(new SecureRandom()));
            //new JcaTlsCryptoProvider().setProvider(new BouncyCastleProvider()).create(new SecureRandom())

            tlsClientProtocol.connect(tlsV3Client);

            int dataAvailable = tlsClientProtocol.getAvailableOutputBytes();
            LogUtils.e(TAG, "----------> dataAvailable :" + dataAvailable);
            byte[] data = new byte[dataAvailable];

            tlsClientProtocol.readOutput(data, 0, dataAvailable);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "----------> IOException :" + e.getMessage());
        }

        return null;
    }
}
