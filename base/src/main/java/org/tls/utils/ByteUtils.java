package org.tls.utils;

import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.util.io.Streams;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ByteUtils {

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static int readInt32(byte[] buf, int offset) {
        int n = buf[offset] << 24;
        n |= (buf[++offset] & 0xff) << 16;
        n |= (buf[++offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public static short readUint8(InputStream input)
            throws IOException {
        int i = input.read();
        if (i < 0) {
            throw new EOFException();
        }
        return (short) i;
    }

    public static short readUint8(byte[] buf, int offset) {
        return (short) (buf[offset] & 0xff);
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

    public static int readUint16(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public static int readUint24(InputStream input)
            throws IOException {
        int i1 = input.read();
        int i2 = input.read();
        int i3 = input.read();
        if (i3 < 0) {
            throw new EOFException();
        }
        return (i1 << 16) | (i2 << 8) | i3;
    }

    public static int readUint24(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 16;
        n |= (buf[++offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }

    public static long readUint32(InputStream input)
            throws IOException {
        int i1 = input.read();
        int i2 = input.read();
        int i3 = input.read();
        int i4 = input.read();
        if (i4 < 0) {
            throw new EOFException();
        }
        return ((i1 << 24) | (i2 << 16) | (i3 << 8) | i4) & 0xFFFFFFFFL;
    }

    public static long readUint32(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 24;
        n |= (buf[++offset] & 0xff) << 16;
        n |= (buf[++offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n & 0xFFFFFFFFL;
    }

    public static long readUint48(InputStream input)
            throws IOException {
        int hi = readUint24(input);
        int lo = readUint24(input);
        return ((long) (hi & 0xffffffffL) << 24) | (long) (lo & 0xffffffffL);
    }

    public static long readUint48(byte[] buf, int offset) {
        int hi = readUint24(buf, offset);
        int lo = readUint24(buf, offset + 3);
        return ((long) (hi & 0xffffffffL) << 24) | (long) (lo & 0xffffffffL);
    }

    public static byte[] readAllOrNothing(int length, InputStream input)
            throws IOException {
        if (length < 1) {
            return EMPTY_BYTES;
        }
        byte[] buf = new byte[length];
        int read = Streams.readFully(input, buf);
        if (read == 0) {
            return null;
        }
        if (read != length) {
            throw new EOFException();
        }
        return buf;
    }

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

    public static void readFully(byte[] buf, InputStream input)
            throws IOException {
        int length = buf.length;
        if (length > 0 && length != Streams.readFully(input, buf)) {
            throw new EOFException();
        }
    }

    public static byte[] readOpaque8(InputStream input)
            throws IOException {
        short length = readUint8(input);
        return readFully(length, input);
    }

    public static byte[] readOpaque8(InputStream input, int minLength)
            throws IOException {
        short length = readUint8(input);
        if (length < minLength) {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }
        return readFully(length, input);
    }

    public static byte[] readOpaque8(InputStream input, int minLength, int maxLength)
            throws IOException {
        short length = readUint8(input);
        if (length < minLength || maxLength < length) {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }
        return readFully(length, input);
    }

    public static byte[] readOpaque16(InputStream input)
            throws IOException {
        int length = readUint16(input);
        return readFully(length, input);
    }

    public static byte[] readOpaque16(InputStream input, int minLength)
            throws IOException {
        int length = readUint16(input);
        if (length < minLength) {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }
        return readFully(length, input);
    }

    public static byte[] readOpaque24(InputStream input)
            throws IOException {
        int length = readUint24(input);
        return readFully(length, input);
    }

    public static byte[] readOpaque24(InputStream input, int minLength)
            throws IOException {
        int length = readUint24(input);
        if (length < minLength) {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }
        return readFully(length, input);
    }

    public static short[] readUint8Array(int count, InputStream input)
            throws IOException {
        short[] uints = new short[count];
        for (int i = 0; i < count; ++i) {
            uints[i] = readUint8(input);
        }
        return uints;
    }

    public static short[] readUint8ArrayWithUint8Length(InputStream input, int minLength)
            throws IOException {
        int length = readUint8(input);
        if (length < minLength) {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }

        return readUint8Array(length, input);
    }

    public static int[] readUint16Array(int count, InputStream input)
            throws IOException {
        int[] uints = new int[count];
        for (int i = 0; i < count; ++i) {
            uints[i] = readUint16(input);
        }
        return uints;
    }

}
