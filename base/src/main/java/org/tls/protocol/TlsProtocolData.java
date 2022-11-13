package org.tls.protocol;


import org.bouncycastle.tls.ProtocolVersion;

import java.io.IOException;

/**
 * Author: yuzzha
 * Date: 2021/12/13 11:45
 * Description:
 * Remark:
 */
public class TlsProtocolData {

    public static final int TYPE_OFFSET = 0;
    public static final int VERSION_OFFSET = 1;
    public static final int LENGTH_OFFSET = 3;
    public static final int FRAGMENT_OFFSET = 5;


    private byte contentType;

    private ProtocolVersion protocolVersion;

    private int contentLength;

    private TlsProtoBodyData protoBody;


    public static TlsProtocolData initData(byte[] packageValue) {
        if (packageValue != null && packageValue.length > 0) {
            TlsProtocolData packageData = new TlsProtocolData(packageValue);
            return packageData;
        }
        throw new RuntimeException("");
    }


    public TlsProtocolData(byte[] values) {
        setContentType(values[0]);
        if (values.length >= 3) {
            setProtocolVersion(values);
        }
        if (values.length >= 5) {
            setContentLength(values);
        }
        switch (this.contentType){

            case  ContentType.handshake:
                processHandShake(values);
                break;
            case  ContentType.alert:
                break;

            case  ContentType.change_cipher_spec:

                break;
            case  ContentType.application_data:

                break;
            case  ContentType.heartbeat:

                break;
        }

    }

    private void processHandShake(byte[] values) {
        int len = values.length;
        if (len > 6) {
            protoBody = HandshakeData.initPackage(values);
        }
    }

    private void processAlert(byte[] values){

    }


    public ProtocolVersion readVersion(byte[] buf, int offset) throws IOException {
        return ProtocolVersion.get(buf[offset] & 0xFF, buf[offset + 1] & 0xFF);
    }

    public static short readUint8(byte[] buf, int offset) {
        return (short) (buf[offset] & 0xff);
    }

    public void setProtocolVersion(byte[] values) {
        try {
            this.protocolVersion = readVersion(values, VERSION_OFFSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContentType(byte contentLength) {
        this.contentType = contentLength;
    }


    private void setContentLength(byte[] values) {
        this.contentLength = readUint16(values, LENGTH_OFFSET);
        //低位在前 高位在后
    }

    public static int readUint16(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public byte getContentType() {
        return contentType;
    }


    @Override
    public String toString() {
        return "TlsPackageData{" +
                "protocolVersion=" + protocolVersion.toString() +
                ", contentType=" + ContentType.getName(contentType) +
                ", contentLength=" + contentLength +
                ","+protoBody.bodyToString()+
                '}';
    }


}
