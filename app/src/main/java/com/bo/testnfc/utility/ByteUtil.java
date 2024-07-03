package com.bo.testnfc.utility;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ByteUtil {

    /** Print content */
    public static String byte2PrintHex(byte[] raw, int offset, int count) {
        if (raw == null) {
            return null;
        }
        if (offset < 0 || offset > raw.length) {
            offset = 0;
        }
        int end = offset + count;
        if (end > raw.length) {
            end = raw.length;
        }
        StringBuilder hex = new StringBuilder();
        for (int i = offset; i < end; i++) {
            int v = raw[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                hex.append(0);
            }
            hex.append(hv);
            hex.append(" ");
        }
        if (hex.length() > 0) {
            hex.deleteCharAt(hex.length() - 1);
        }
        return hex.toString().toUpperCase();
    }

    /**
     * Convert byte array to hexadecimal string
     *
     * @param bytes Source byte array
     * @return Converted hexadecimal string
     */
    public static String bytes2HexStr(byte... bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return bytes2HexStr(bytes, 0, bytes.length);
    }

    /**
     * Convert byte array to hexadecimal string
     *
     * @param src    Source byte array
     * @param offset Offset
     * @param len    Data length
     * @return Converted hexadecimal string
     */
    public static String bytes2HexStr(byte[] src, int offset, int len) {
        int end = offset + len;
        if (src == null || src.length == 0 || offset < 0 || len < 0 || end > src.length) {
            return "";
        }
        byte[] buffer = new byte[len * 2];
        int h = 0, l = 0;
        for (int i = offset, j = 0; i < end; i++) {
            h = src[i] >> 4 & 0x0f;
            l = src[i] & 0x0f;
            buffer[j++] = (byte) (h > 9 ? h - 10 + 'A' : h + '0');
            buffer[j++] = (byte) (l > 9 ? l - 10 + 'A' : l + '0');
        }
        return new String(buffer);
    }

    public static byte[] hexStr2Bytes(String hexStr) {
        if (TextUtils.isEmpty(hexStr)) {
            return new byte[0];
        }
        int length = hexStr.length() / 2;
        char[] chars = hexStr.toCharArray();
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = (byte) (char2Byte(chars[i * 2]) << 4 | char2Byte(chars[i * 2 + 1]));
        }
        return b;
    }

    public static byte hexStr2Byte(String hexStr) {
        return (byte) Integer.parseInt(hexStr, 16);
    }

    public static String hexStr2Str(String hexStr) {
        String vi = "0123456789ABC DEF".trim();
        char[] array = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            char c = array[2 * i];
            temp = vi.indexOf(c) * 16;
            c = array[2 * i + 1];
            temp += vi.indexOf(c);
            bytes[i] = (byte) (temp & 0xFF);
        }
        return new String(bytes);
    }

    public static String hexStr2AsciiStr(String hexStr) {
        String vi = "0123456789ABC DEF".trim();
        hexStr = hexStr.trim().replace(" ", "").toUpperCase(Locale.US);
        char[] array = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int temp = 0x00;
        for (int i = 0; i < bytes.length; i++) {
            char c = array[2 * i];
            temp = vi.indexOf(c) << 4;
            c = array[2 * i + 1];
            temp |= vi.indexOf(c);
            bytes[i] = (byte) (temp & 0xFF);
        }
        return new String(bytes);
    }

    /**
     * Convert unsigned short to int, big endian mode (high bit first)
     */
    public static int unsignedShort2IntBE(byte[] src, int offset) {
        return (src[offset] & 0xff) << 8 | (src[offset + 1] & 0xff);
    }

    /**
     * Convert unsigned short to int, little endian mode (lowest bit first)
     */
    public static int unsignedShort2IntLE(byte[] src, int offset) {
        return (src[offset] & 0xff) | (src[offset + 1] & 0xff) << 8;
    }

    /**
     * Convert unsigned byte to int
     */
    public static int unsignedByte2Int(byte[] src, int offset) {
        return src[offset] & 0xFF;
    }

    /**
     * Convert byte array to int, endian mode (high byte first)
     */
    public static int unsignedInt2IntBE(byte[] src, int offset) {
        int result = 0;
        for (int i = offset; i < offset + 4; i++) {
            result |= (src[i] & 0xff) << (offset + 3 - i) * 8;
        }
        return result;
    }

    /**
     * Convert byte array to int, little endian mode (low byte first)
     */
    public static int unsignedInt2IntLE(byte[] src, int offset) {
        int value = 0;
        for (int i = offset; i < offset + 4; i++) {
            value |= (src[i] & 0xff) << (i - offset) * 8;
        }
        return value;
    }

    /**
     * Convert int to byte array, big endian mode (high byte first)
     */
    public static byte[] int2BytesBE(int src) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (src >> (3 - i) * 8);
        }
        return result;
    }

    /**
     * Convert int to byte array, little endian mode (low byte first)
     */
    public static byte[] int2BytesLE(int src) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (src >> i * 8);
        }
        return result;
    }

    /**
     * Convert short to byte array, big endian mode (high bit first)
     */
    public static byte[] short2BytesBE(short src) {
        byte[] result = new byte[2];
        for (int i = 0; i < 2; i++) {
            result[i] = (byte) (src >> (1 - i) * 8);
        }
        return result;
    }

    /**
     * Convert short to byte array, little endian mode (low byte first)
     */
    public static byte[] short2BytesLE(short src) {
        byte[] result = new byte[2];
        for (int i = 0; i < 2; i++) {
            result[i] = (byte) (src >> i * 8);
        }
        return result;
    }

    /**
     * Merge a list of byte arrays into a single byte array
     */
    public static byte[] concatByteArrays(byte[]... list) {
        if (list == null || list.length == 0) {
            return new byte[0];
        }
        return concatByteArrays(Arrays.asList(list));
    }

    /**
     * Merge a list of byte arrays into a single byte array
     */
    public static byte[] concatByteArrays(List<byte[]> list) {
        if (list == null || list.isEmpty()) {
            return new byte[0];
        }
        int totalLen = 0;
        for (byte[] b : list) {
            if (b == null || b.length == 0) {
                continue;
            }
            totalLen += b.length;
        }
        byte[] result = new byte[totalLen];
        int index = 0;
        for (byte[] b : list) {
            if (b == null || b.length == 0) {
                continue;
            }
            System.arraycopy(b, 0, result, index, b.length);
            index += b.length;
        }
        return result;
    }


    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static int char2Byte(char c) {
        if (c >= 'a') {
            return (c - 'a' + 10) & 0x0f;
        }
        if (c >= 'A') {
            return (c - 'A' + 10) & 0x0f;
        }
        return (c - '0') & 0x0f;
    }


}
