package com.aurora.util;

public final class Base64 {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String encode(byte[] data) {
        if (data == null || data.length == 0) return "";
        StringBuffer sb = new StringBuffer((data.length * 4 + 2) / 3);
        int i = 0;
        while (i < data.length) {
            int b0 = data[i++] & 0xFF;
            int b1 = (i < data.length) ? (data[i++] & 0xFF) : -1;
            int b2 = (i < data.length) ? (data[i++] & 0xFF) : -1;

            sb.append(ALPHABET.charAt((b0 >> 2) & 0x3F));
            if (b1 < 0) {
                sb.append(ALPHABET.charAt((b0 & 0x3) << 4));
                sb.append('=');
                sb.append('=');
                continue;
            }

            sb.append(ALPHABET.charAt(((b0 & 0x3) << 4) | ((b1 >> 4) & 0xF)));
            if (b2 < 0) {
                sb.append(ALPHABET.charAt((b1 & 0xF) << 2));
                sb.append('=');
                continue;
            }

            sb.append(ALPHABET.charAt(((b1 & 0xF) << 2) | ((b2 >> 6) & 0x3)));
            sb.append(ALPHABET.charAt(b2 & 0x3F));
        }
        return sb.toString();
    }

    public static byte[] decode(String s) {
        if (s == null) return new byte[0];
        // remove non-base64 chars (whitespace)
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 32) sb.append(c);
        }
        String in = sb.toString();
        int pad = 0;
        int len = in.length();
        if (len == 0) return new byte[0];
        if (in.charAt(len - 1) == '=') pad++;
        if (len > 1 && in.charAt(len - 2) == '=') pad++;
        int outLen = (len * 6) / 8 - pad;
        byte[] out = new byte[outLen];
        int outPos = 0;

        int bits = 0, bitCount = 0;
        for (int i = 0; i < len; i++) {
            char c = in.charAt(i);
            int val;
            if (c == '=') break;
            int idx = ALPHABET.indexOf(c);
            if (idx >= 0) {
                val = idx;
            } else {
                continue;
            }
            bits = (bits << 6) | val;
            bitCount += 6;
            if (bitCount >= 8) {
                bitCount -= 8;
                out[outPos++] = (byte) ((bits >> bitCount) & 0xFF);
            }
        }
        return out;
    }
}
