package org.tls.selector;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCryptoProvider;

import java.security.SecureRandom;



public class TlsCryptoSelector {

    private static TlsCrypto BC_CRYPTO = new BcTlsCrypto(new SecureRandom());

    private static TlsCrypto JCA_CRYPTO =
            new JcaTlsCryptoProvider().setProvider(new BouncyCastleProvider()).create(new SecureRandom());

    private static boolean isJac = false;

    public static TlsCrypto getCrypto() {
        if (isJac) {
            return JCA_CRYPTO;
        }
        return BC_CRYPTO;
    }
}
