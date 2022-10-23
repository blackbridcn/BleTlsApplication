package org.tls.v13;

import org.tls.selector.TlsCryptoSelector;
import org.tls.v13.server.TlsV3ServerImpl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.TlsServerProtocol;

import java.io.IOException;
import java.security.Security;

public class TlsV3ServerUtils {

    static TlsServerProtocol tlsServerProtocol;
    static TlsV3ServerImpl tlsV3ServerImpl;

    public static byte[] input(byte[] input) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            tlsServerProtocol = new TlsServerProtocol();
            tlsV3ServerImpl = new TlsV3ServerImpl(TlsCryptoSelector.getCrypto());
            tlsServerProtocol.accept(tlsV3ServerImpl);
            tlsServerProtocol.offerInput(input);
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
}
