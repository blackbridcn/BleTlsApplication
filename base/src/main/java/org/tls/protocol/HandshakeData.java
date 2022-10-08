package org.tls.protocol;

import android.annotation.SuppressLint;

import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.ExtensionType;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.util.io.Streams;
import org.e.ble.utils.HexStrUtils;
import org.utlis.ByteHexUtils;
import org.utlis.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * Author: yuzzha
 * Date: 2021/12/13 11:45
 * Description:
 * Remark:
 */
public class HandshakeData implements TlsProtoBodyData {


    private static final int HANDSHAKE_OFFSET = 5;

    private static final int HANDSHAKE_TYPE_OFFSET = 1;

    public static final int VERSION_OFFSET = 9;


    public static final int VERSION_AFTER_OFFSET = 11;

    // 16 表示 ContentType Handshake
    // 03 03 表示 Version： TLS v1.2
    // 00 67 表示 长度 103


    //5

    // 01  表示 Handshake Type : 0x01: Client Hello;  0x02 Server Hello

    // 00 00 63 表示 Length 99

    //9

    // 03 03  Version: TLS v1.2

    // 11

    // B2 B3 88 34  GMT Unix Time

    //Random Bytes 28
    // DD FD C9 57 A5 7D FA 52 61 85 4F 25 12 90 A7 9A
    // 8E 9F EE 35 4B 1F 31 49 9F 0A 3E 66

    //43

    // 00  Session ID  length
    // 00 04 Cipher Suite length 表示 2个密钥

    // C0 AC Cipher Suite : TLS_ECDHE_ECDSA_WITH_AES_128_CCM
    // 00 FF Cipher Suite : TLS_EMPTY_RENEGOTIATION_INFO_SCSV （0xFF）密码套件（缩写为SCSV） “安全重协商”机制
    // 01  Compression Methods length
    // 00  Compression Methods :null
    // 00 36 Extension length    T
    // LS扩展于2003年以一个独立的规范（RFC3456)被提出，之后被加入到TLS1.1和TLS1.2的版本中。
    // 它主要用于声明协议对某些新功能的支持，或者携带在握手进行中需要的额外数据。
    // TLS扩展机制使得协议在不改变本身基本行为的基础上增加了额外的功能，因此从出现以来，它就成为协议功能不断发展的主要载体
    //而每个扩展都以两个字节的表明扩展类型的字段开始，后面接着扩展的数据。
    //  全部的扩展类型可以在IANA的网站看到 http://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml
    //
    // 00 0A  Extension Name : supported_groups
    // 00 06 length
    // 00 04
    // 00 17 00 18

    // 00 0D  Extension Name : signature_algorithms
    // 00 20  length 32
    // 00 1E  signature  algorithms length  30
    // 02 01 03 01 04 01 05 01 06 01 02 02 03 02 04 02 05 02 06 02 02 03 03 03 04 03 05 03 06 03

    // 00 0B
    // 00 04
    // 03 00 01 02

    //16 03 04
    // 00 D3  //211
    // 01
    // 00 00 CF  //207
    // 03 03
    //
    // BA 41 05 40
    // EF 02 C5 D4 BE A0 54
    // 7B 8D 6B C7 CC FA F1
    // 19 94 D0 81 BB DC CE
    // D6 EF E6 9F 3B 4F 7B
    //
    // 2024E76356C90FAC9FEED93E06B75AFCE300BF554A91B4F3AEA28F2FE1B79BBA1E0004130100FF01000082002B0003020304000A0010000E001D001E00170018010001010102003300260024001D0020CF81AF0259560DF6B34E950CE2BEAEB90B60D0848B7D4AEB451BC426BBE8AC43000500050100000000000D0030002E080708080403050306030804080508060809080A080B040105010601040205020602030303010302020302010202

    private static final int PROTOCOL_TYPE_CLIENT_HELLO = 0x1;


    //private String handshakeType;

    private byte handshakeType;

    private String contentTypeString;

    private int handshakeLength;

    private ProtocolVersion protocolVersion;


    public static HandshakeData initPackage(byte[] packageValue) {
        if (packageValue != null && packageValue.length > 6) {
            HandshakeData packageData = new HandshakeData(packageValue);
            return packageData;
        }
        return null;
    }

    public HandshakeData(byte[] values) {
        switch (/*getHandshakeType()*/values[HANDSHAKE_OFFSET]) {
            case HandshakeType.client_hello:

                processHandshakeHello(values);

                processClientHello(values);

                break;
            case HandshakeType.server_hello:

                processHandshakeHello(values);
                break;

            case HandshakeType.server_key_exchange:
                protoBodyData = EccExchangeKey.init(values);

                break;
        }

    }


    TlsProtoBodyData protoBodyData;

    private void processHandshakeHello(byte[] values) {
        setHandshakeType(values[HANDSHAKE_OFFSET]);
        setContentLength(readUint24(values, HANDSHAKE_OFFSET + 1));
        setProtocolVersion(values);
    }


    private int publicKeyLength = 0;


    private void setHandshakeType(byte handshakeType) {
        //this.handshakeType = HandshakeType.getName(contentLength);
        this.handshakeType = handshakeType;
    }

    public byte getHandshakeType() {
        return handshakeType;
    }

    public void setContentLength(int contentLength) {
        this.handshakeLength = contentLength;
    }


    public int getContentLength() {
        return handshakeLength;
    }

    public void setProtocolVersion(byte[] values) {
        try {

            this.protocolVersion = readVersion(values, VERSION_OFFSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProtocolVersion readVersion(byte[] buf, int offset) throws IOException {
        return ProtocolVersion.get(buf[offset] & 0xFF, buf[offset + 1] & 0xFF);
    }

    private long helloTimes;
    private byte[] random;

    private short sessionIdLength;
    private byte[] sessionId;

    private int cipherSuiteLength;
    private byte[] cipherSuite;

    private short compressionMethodsLength;
    private byte[] compressionMethods;

    private int extensionLength;
    private byte[] extension;
    private Hashtable<Integer, ExtData> extensions;

    private void processClientHello(byte[] values) {
        this.helloTimes = readUint32(values, VERSION_AFTER_OFFSET);

        random = new byte[32];
        System.arraycopy(values, VERSION_AFTER_OFFSET, random, 0, 32);

        int offset = setSessionId(values, 43);

        int cipherSuiteOffset = setCipherSuite(values, offset);

        int compressionMethodsOffset = setCompressionMethods(values, cipherSuiteOffset);

        setExtension(values, compressionMethodsOffset);

    }

    private void processServerHello(byte[] values) {
        this.helloTimes = readUint32(values, VERSION_AFTER_OFFSET);
        random = new byte[32];
        System.arraycopy(values, VERSION_AFTER_OFFSET, random, 0, 32);
        int offset = setSessionId(values, 43);

        //cipherSuite[0] = readUint16(values, offset);
        //int compressionMethodsOffset = setCompressionMethods(values, offset + 2);

    }

    private int setSessionId(byte[] values, int offset) {
        this.sessionIdLength = readUint8(values, offset);
        if (sessionIdLength != 0) {
            sessionId = new byte[sessionIdLength];
            System.arraycopy(values, offset + 1, sessionId, 0, sessionIdLength);
            return offset + 1 + sessionIdLength;
        }
        return offset + 1;
    }

    private int setCipherSuite(byte[] values, int offset) {
        this.cipherSuiteLength = readUint16(values, offset);
        if (this.cipherSuiteLength != 0) {
            cipherSuite = new byte[cipherSuiteLength];
            System.arraycopy(values, offset + 2,
                    cipherSuite, 0, cipherSuiteLength);
            return offset + 2 + cipherSuiteLength;
        }

        return offset + 2;
    }


    private int setCompressionMethods(byte[] values, int offset) {
        this.compressionMethodsLength = readUint8(values, offset);
        if (compressionMethodsLength > 0) {
            compressionMethods = new byte[compressionMethodsLength];
            System.arraycopy(values, offset + 1, compressionMethods, 0, compressionMethodsLength);
            return offset + 1 + compressionMethodsLength;
        }
        return offset + 1;
    }

    private void setExtension(byte[] values, int offset) {
        this.extensionLength = readUint16(values, offset);
        if (extensionLength > 0) {
            this.extension = new byte[extensionLength];
            System.arraycopy(values, offset + 2, extension, 0, extensionLength);

            LogUtils.e("private void setExtension(byte[] values, int offset)");

            ByteArrayInputStream buf = new ByteArrayInputStream(extension);
            int extension_type = -1;
            boolean pre_shared_key_found = false;
            extensions = new Hashtable<Integer, ExtData>();
            try {
                do {

                    extension_type = readUint16(buf);

                    int length = readUint16(buf);
                    byte[] extension_data = readFully(length, buf);

                   // byte[] extension_data = readOpaque16(buf);

                    /*
                     * RFC 3546 2.3 There MUST NOT be more than one extension of the same type.
                     */
                    if (null != extensions.put(extension_type, new ExtData(length, extension_data))) {

                        throw new TlsFatalAlert(AlertDescription.illegal_parameter,
                                "Repeated extension: " + org.bouncycastle.tls.ExtensionType.getText(extension_type));
                    }

                    pre_shared_key_found |= (org.bouncycastle.tls.ExtensionType.pre_shared_key == extension_type);
                }
                while (buf.available() > 0);

            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e("setExtension IOException:" + e.getMessage());
            }
        }


    }

    @Override
    public String bodyToString() {
        StringBuilder sb = new StringBuilder();
        String s0 = "handshakeType=" + HandshakeType.getName(this.handshakeType) +
                ", handshakeLength=" + handshakeLength;
        //", protocolVersion=" + protocolVersion.toString()
        //;

        sb.append(s0);

        if (handshakeType == HandshakeType.client_hello) {
            sb.append(clientHelloString());
        } else if (handshakeType == HandshakeType.server_hello) {
            sb.append(serverHelloString());
        }
        String b = sb.toString();
        LogUtils.e("TAG", "----------> " + b);
        return b;//sb.toString();
    }


    public String clientHelloString() {
        StringBuffer buffer = new StringBuffer();
        String s0 = ", UnixTime=" + getSimpleDate(helloTimes) +
                ", ClientRandom=" + HexStrUtils.Companion.byteArrayToHexString(random) +
                ", sessionIdLength=" + sessionIdLength;
        buffer.append(s0);
        if (sessionIdLength > 0) {
            buffer.append(", sessionId=" + HexStrUtils.Companion.byteArrayToHexString(sessionId));
        }
        buffer.append(", cipherSuiteLength=" + cipherSuiteLength + "(" + (cipherSuiteLength / 2) + "个密码套件)");
        if (cipherSuiteLength > 0) {
            buffer.append(", cipherSuite=" + HexStrUtils.Companion.byteArrayToHexString(cipherSuite));
        }
        buffer.append(", compressionMethodsLength=" + compressionMethodsLength);
        if (compressionMethodsLength > 0) {
            buffer.append(", compressionMethods=" + HexStrUtils.Companion.byteArrayToHexString(compressionMethods));
        }
        buffer.append(", extensionLength=" + extensionLength);
        if (extensionLength > 0) {
            buffer.append(", extension=" + handshakeExtensionString());/*
                        HexStrUtils.Companion.byteArrayToHexString(extension));*/

        }


        buffer.append("}");
        return buffer.toString();
    }

    public String serverHelloString() {
        StringBuffer buffer = new StringBuffer();
        String s0 = ", UnixTime=" + getSimpleDate(helloTimes) +
                ", ServerRandom=" + HexStrUtils.Companion.byteArrayToHexString(random) +
                ", sessionIdLength=" + sessionIdLength;
        buffer.append(s0);
        if (sessionIdLength > 0) {
            buffer.append(", sessionId=" + HexStrUtils.Companion.byteArrayToHexString(sessionId));
        }
        buffer.append(", cipherSuite=" + HexStrUtils.Companion.byteArrayToHexString(cipherSuite));
        buffer.append(", compressionMethodsLength=" + compressionMethodsLength);
        if (compressionMethodsLength > 0) {
            buffer.append(", compressionMethods=" + HexStrUtils.Companion.byteArrayToHexString(compressionMethods));
        }
        buffer.append(", extensionLength=" + extensionLength);
        if (extensionLength > 0) {
            buffer.append(", extension=" + handshakeExtensionString());/*
                        HexStrUtils.Companion.byteArrayToHexString(extension));*/

        }

        buffer.append("}");
        return buffer.toString();
    }


    public String handshakeExtensionString() {
        if (extensions != null) {
            StringBuffer buffer = new StringBuffer();
            for (Map.Entry<Integer, ExtData> integerEntry : extensions.entrySet()) {
                buffer.append("\n").append(ExtensionType.getName(integerEntry.getKey())).append(':').append("length= ").append(integerEntry.getValue().getLen()).append(" value= ").append(ByteHexUtils.INSTANCE.byteArrayToHexString(integerEntry.getValue().getData()));
            }
            String extS = buffer.toString();
            LogUtils.e("Extensions : " + extS);
            return extS;
        }
        return "";
    }

    public static byte[] readOpaque16(InputStream input)
            throws IOException {
        int length = readUint16(input);
        return readFully(length, input);
    }

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static byte[] readFully(int length, InputStream input)
            throws IOException {
        if (length < 1) {
            return EMPTY_BYTES;
        }
        byte[] buf = new byte[length];
        if (length != Streams.readFully(input, buf)) {
            throw new EOFException();
        }
        return buf;
    }


    public static short readUint8(byte[] buf, int offset) {
        return (short) (buf[offset] & 0xff);
    }

    /*public static int readUint8(byte[] buf, int offset) {
        return (buf[offset] & 0xff);
    }*/

    public static int readUint16(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public static int readUint16(InputStream input)
            throws IOException {
        int i1 = input.read();
        int i2 = input.read();
        if (i2 < 0) {
            throw new EOFException();
        }
        return (i1 << 8) | i2;
    }

    public static int readUint24(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 16;
        n |= (buf[++offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public static long readUint32(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 24;
        n |= (buf[++offset] & 0xff) << 16;
        n |= (buf[++offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n & 0xFFFFFFFFL;
    }

    public static String getSimpleDate(long longDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(longDate);
        return df.format(date);
    }

    class ExtData{
        int len;
        byte[] data;

        public ExtData(int len, byte[] data) {
            this.len = len;
            this.data = data;
        }

        public int getLen() {
            return len;
        }

        public void setLen(int len) {
            this.len = len;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
