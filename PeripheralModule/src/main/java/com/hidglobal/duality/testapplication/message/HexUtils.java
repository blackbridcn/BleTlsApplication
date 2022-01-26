package com.hidglobal.duality.testapplication.message;

/**
 * Author: yuzzha
 * Date: 2021-06-16 9:18
 * Description:
 * Remark:
 */
public class HexUtils {

    public static String intToHex(int value) {
        return buildComplLen(Integer.toHexString(value), 4);
    }

    public static String byteToHex(byte value) {
        if (value <= 0x0F) {
            return "0".concat(Integer.toHexString(value));
        }
        return Integer.toHexString(value);
    }

    public static String intTwoHex(int value) {
        if (value > 0xFF) {
            throw new RuntimeException(" ");
        }
        if (value <= 0x0F) {
            return "0".concat(Integer.toHexString(value));
        }
        return Integer.toHexString(value);
    }

    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    /**
     * 字节数组转十六进制字符串
     *
     * @param b: byte[] bytes_1=new byte[]{(byte) 0xA0,(byte) 0xB1,2}
     * @return "A0B102"
     */
    public static String bytesToHexString(byte[] b) {
        if (b == null) {
            return null;
        }
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[i] & 0x0f];
        }
        return new String(buff);
    }

    /**
     * 高低位 组成int，
     *
     * @param hi
     * @param lo
     * @return
     */
    public static int buildUint16(byte hi, byte lo) {
        return (int) ((hi << 8) + (lo & 0xff));
    }

    public static byte[] HexString2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static String buildComplLen(String src, int len) {
        if (src == null) {
            src = "";
        }
        if (len == 0) {
            return src;
        }
        if (src.length() == len) {
            return src;
        } else if (src.length() > len) {
            throw new RuntimeException(src + " len :" + src.length() + " ,长度已经超出限制: " + len);
        } else {
            return buildComplLen("0".concat(src), len);
        }
    }

    //7E040500000000000014F8
    public static String intToHexLh(int intVal) {
        StringBuffer var = new StringBuffer(8);
        var.append(intTwoHex(intVal & 0xFF))
                .append(intTwoHex(intVal >> 8 & 0xff))
                .append(intTwoHex(intVal >> 16 & 0xff))
                .append(intTwoHex(intVal >> 24 & 0xff));
        return var.toString();
    }


}
