package org.tls.protocol;

/**
 * Author: yuzzha
 * Date: 2021/12/17 12:15
 * Description:
 * Remark:
 */
public class HandshakeType {
    /*
     * RFC 2246 7.4
     */
    public static final byte hello_request = 0;
    public static final byte client_hello = 1;
    public static final byte server_hello = 2;
    public static final byte certificate = 11;
    public static final byte server_key_exchange = 12;
    public static final byte certificate_request = 13;
    public static final byte server_hello_done = 14;
    public static final byte certificate_verify = 15;
    public static final byte client_key_exchange = 16;
    public static final byte finished = 20;

    /*
     * RFC 3546 2.4
     */
    public static final byte certificate_url = 21;
    public static final byte certificate_status = 22;

    /*
     * (DTLS) RFC 4347 4.3.2
     */
    public static final byte hello_verify_request = 3;

    /*
     * RFC 4680
     */
    public static final byte supplemental_data = 23;

    /*
     * RFC 8446
     */
    public static final byte new_session_ticket = 4;
    public static final byte end_of_early_data = 5;
    public static final byte hello_retry_request = 6;
    public static final byte encrypted_extensions = 8;
    public static final byte key_update = 24;
    public static final byte message_hash = (byte) 0xFE;

    public static String getName(byte handshakeType)
    {
        switch (handshakeType)
        {
            case hello_request:
                return "hello_request";
            case client_hello:
                return "client_hello";
            case server_hello:
                return "server_hello";
            case certificate:
                return "certificate";
            case server_key_exchange:
                return "server_key_exchange";
            case certificate_request:
                return "certificate_request";
            case server_hello_done:
                return "server_hello_done";
            case certificate_verify:
                return "certificate_verify";
            case client_key_exchange:
                return "client_key_exchange";
            case finished:
                return "finished";
            case certificate_url:
                return "certificate_url";
            case certificate_status:
                return "certificate_status";
            case hello_verify_request:
                return "hello_verify_request";
            case supplemental_data:
                return "supplemental_data";
            case new_session_ticket:
                return "new_session_ticket";
            case end_of_early_data:
                return "end_of_early_data";
            case hello_retry_request:
                return "hello_retry_request";
            case encrypted_extensions:
                return "encrypted_extensions";
            case key_update:
                return "key_update";
            case message_hash:
                return "message_hash";
            default:
                return "UNKNOWN";
        }
    }

    public static String getText(byte handshakeType)
    {
        return getName(handshakeType) + "(" + handshakeType + ")";
    }

    public static boolean isRecognized(byte handshakeType)
    {
        switch (handshakeType)
        {
            case hello_request:
            case client_hello:
            case server_hello:
            case certificate:
            case server_key_exchange:
            case certificate_request:
            case server_hello_done:
            case certificate_verify:
            case client_key_exchange:
            case finished:
            case certificate_url:
            case certificate_status:
            case hello_verify_request:
            case supplemental_data:
            case new_session_ticket:
            case end_of_early_data:
            case hello_retry_request:
            case encrypted_extensions:
            case key_update:
            case message_hash:
                return true;
            default:
                return false;
        }

    }
}
