package org.bouncycastle.tls.crypto.impl.bc;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.agreement.srp.SRP6Client;
import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.crypto.TlsSRP6Client;

import java.math.BigInteger;

final class BcTlsSRP6Client
    implements TlsSRP6Client
{
    private final SRP6Client srp6Client;

    BcTlsSRP6Client(SRP6Client srpClient)
    {
        this.srp6Client = srpClient;
    }

    public BigInteger calculateSecret(BigInteger serverB)
        throws TlsFatalAlert
    {
        try
        {
            return srp6Client.calculateSecret(serverB);
        }
        catch (CryptoException e)
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter, e);
        }
    }

    public BigInteger generateClientCredentials(byte[] srpSalt, byte[] identity, byte[] password)
    {
        return srp6Client.generateClientCredentials(srpSalt, identity, password);
    }
}
