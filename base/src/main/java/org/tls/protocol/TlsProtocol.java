package org.tls.protocol;

public class TlsProtocol {

    public static final int FRAGMENT_OFFSET = 5;

    public static boolean isTlsByteStream(byte[] bytes) {
        if(bytes==null || bytes.length<FRAGMENT_OFFSET){
            return false;
        }
        return ContentType.isTlsData(bytes[0]);
    }

    public static TlsProtocolData inputTlsBytes(byte[] bytes){
       return TlsProtocolData.initData(bytes);
    }

}
