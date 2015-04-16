package org.repetti.srs.core;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author repetti
 */
public class StringHelper {
    public static final Random r = new Random();
    public static final String lettersSmall = "qwertyuiopasdfghjklzxcvbnm";
    public static final String lettersBig = "QWERTYUIOPASDFGHJKLZXCVBNM";
    public static final String lettersAll = lettersBig + lettersSmall;
    private static final char[] base32 = "0123456789abcdefghijklmnopqrstuv".toCharArray();
    private static final SecureRandom sr = new SecureRandom();

    public static String generateString(final String letters, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(r.nextInt(letters.length())));
        }
        return sb.toString();
    }

    public static String generateSecureWord(final String letters, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(r.nextInt(letters.length())));
        }
        return sb.toString();
    }

    /**
     * byte array to base16
     *
     * @param bytes input value
     * @return hex representation of the byte array
     */
    public static String toHexString(byte[] bytes) {
        final char[] res = new char[bytes.length * 2];
//        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            int t = ((int) bytes[i]) & 0xFF;
            res[i * 2] = base32[(t >> 4) & 15];
            res[i * 2 + 1] = base32[t & 15];
        }
        return new String(res);
    }

//    /**
//     * byte array to base10
//     *
//     * @param bytes input value
//     * @return decimal representation of the byte array
//     */
//    public static String toDecString(byte[] bytes) {
//        final StringBuilder sb = new StringBuilder(bytes.length * 2);
//        for (final byte b : bytes) {
//            sb.append(String.format("%02x", b & 0xff));
//        }
//        return sb.toString();
//    }

    /**
     * byte array to base8
     *
     * @param bytes input value
     * @return octal representation of the byte array
     */
    public static String toOctString(byte[] bytes) {

        // 3h => 8o
        final int len = bytes.length % 3;
        final int newSize = (bytes.length / 3 * 8 + len * 3);
        final char[] res = new char[newSize];
        int resStart = len * 3;

        if (len == 1) {
            final int i = bytes[0] & 0xFF;
            res[0] = base32[(i >>> 6) & 7];
            res[1] = base32[(i >>> 3) & 7];
            res[2] = base32[i & 7];
        } else if (len == 2) {
            final int i = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
            res[0] = base32[(i >>> 15) & 7];
            res[1] = base32[(i >>> 12) & 7];
            res[2] = base32[(i >>> 9) & 7];
            res[3] = base32[(i >>> 6) & 7];
            res[4] = base32[(i >>> 3) & 7];
            res[5] = base32[i & 7];
        }

        for (int i = len; i < bytes.length; i += 3) {
            int tmp = (bytes[i + 2] & 0xFF) | ((bytes[i + 1] & 0xFF) << 8) | ((bytes[i] & 0xFF) << 16);
            for (int j = 0; j < 8; j++) {
                res[resStart++] = base32[(tmp >>> ((7 - j) * 3)) & 7];
            }
        }
        return new String(res);
    }

    /**
     * byte array to base8
     *
     * @param bytes input value
     * @return quasi-octal representation of the byte array
     * 1 will be added to each symbol, meaning 0 -> 1, 1 -> 2, ... 7 -> 8
     */
    public static String toEpamString(byte[] bytes) {
        final char[] baseEpam = "12345678".toCharArray();

        // 3h => 8o
        final int len = bytes.length % 3;
        final int newSize = (bytes.length / 3 * 8 + len * 3);
        final char[] res = new char[newSize];
        int resStart = len * 3;

        if (len == 1) {
            final int i = bytes[0] & 0xFF;
            res[0] = baseEpam[(i >>> 6) & 7];
            res[1] = baseEpam[(i >>> 3) & 7];
            res[2] = baseEpam[i & 7];
        } else if (len == 2) {
            final int i = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
            res[0] = baseEpam[(i >>> 15) & 7];
            res[1] = baseEpam[(i >>> 12) & 7];
            res[2] = baseEpam[(i >>> 9) & 7];
            res[3] = baseEpam[(i >>> 6) & 7];
            res[4] = baseEpam[(i >>> 3) & 7];
            res[5] = baseEpam[i & 7];
        }

        for (int i = len; i < bytes.length; i += 3) {
            int tmp = (bytes[i + 2] & 0xFF) | ((bytes[i + 1] & 0xFF) << 8) | ((bytes[i] & 0xFF) << 16);
            for (int j = 0; j < 8; j++) {
                res[resStart++] = baseEpam[(tmp >>> ((7 - j) * 3)) & 7];
            }
        }
        return new String(res);
    }
}
