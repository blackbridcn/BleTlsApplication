package org.tls.v13.client;

import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsClientContext;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsServerCertificate;

import java.io.IOException;

public class TlsClientAuthentication implements TlsAuthentication {


    private static final String TAG = TlsClientAuthentication.class.getSimpleName();


    private TlsV3Client tlsV2Client;
    protected TlsClientContext context;


    TlsClientAuthentication(TlsClientContext context, TlsV3Client tlsV2Client) {
        this.context = context;
        this.tlsV2Client = tlsV2Client;
    }

    @Override
    public void notifyServerCertificate(TlsServerCertificate serverCertificate) throws IOException {

    }

    @Override
    public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
        return null;
    }


}
