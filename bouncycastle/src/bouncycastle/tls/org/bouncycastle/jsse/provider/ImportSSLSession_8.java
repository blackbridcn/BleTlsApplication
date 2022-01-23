package org.bouncycastle.jsse.provider;

import org.bouncycastle.jsse.BCSNIServerName;

import java.util.List;

import javax.net.ssl.ExtendedSSLSession;

class ImportSSLSession_8
    extends ImportSSLSession_7
{
    ImportSSLSession_8(ExtendedSSLSession sslSession)
    {
        super(sslSession);
    }

    @Override
    public List<BCSNIServerName> getRequestedServerNames()
    {
        return JsseUtils_8.importSNIServerNames(sslSession.getRequestedServerNames());
    }
}
