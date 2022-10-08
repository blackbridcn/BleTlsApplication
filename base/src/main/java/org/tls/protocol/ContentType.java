package org.tls.protocol;

/**
 * Author: yuzzha
 * Date: 2021/12/17 12:13
 * Description:
 * Remark:
 */
public class ContentType {

    public static final byte change_cipher_spec = 20;

    public static final byte alert = 21;
    //0x16
    public static final byte handshake = 22;

    public static final byte application_data = 23;

    public static final byte heartbeat = 24;

    public static String getName(byte contentType) {
        switch (contentType) {
            case alert:
                return "alert";
            case application_data:
                return "application_data";
            case change_cipher_spec:
                return "change_cipher_spec";
            case handshake:
                return "handshake";
            case heartbeat:
                return "heartbeat";
            default:
                return "UNKNOWN";
        }
    }

    public static String getText(byte contentType) {
        return getName(contentType) + "(" + contentType + ")";
    }

    public static boolean isTlsData(byte contentType) {
        if (contentType < change_cipher_spec) {
            return false;
        }
        if (contentType > heartbeat) {
            return false;
        }
        return true;
    }



}
