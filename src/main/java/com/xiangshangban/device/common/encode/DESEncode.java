package com.xiangshangban.device.common.encode;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

public class DESEncode {
    // 密钥
    private final static String SECRET_KEY = "lkjrewqfdsfdsaAD876dsakndwqedlKJDlkjdsAAkjFFDADSASADLdslkjncxzcz=-3&$#saddsadsaLKJDSADSA@lx100$#365#$";
    // 向量
    private final static String IV = "01234567";
    // 加解密统一使用的编码方式
    private final static String ENCODING = "UTF-8";

    /**
     * 3DES加密
     *
     * @param plainText
     *            普通文本
     * @return
     * @throws Exception
     */
    public static String encrypt(String plainText) throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(SECRET_KEY.getBytes());
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        byte[] encryptData = cipher.doFinal(plainText.getBytes(ENCODING));
        return Base64.encode(encryptData);
    }

    /**
     * 3DES解密
     *
     * @param encryptText
     *            加密文本
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptText) throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(SECRET_KEY.getBytes());
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
        byte[] decryptData = cipher.doFinal(Base64.decode(encryptText));
        return new String(decryptData, ENCODING);
    }

    public static class Base64 {
        private static final char[] LEGAL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                .toCharArray();

        public static String encode(byte[] data) {
            int start = 0;
            int len = data.length;
            StringBuffer buf = new StringBuffer(data.length * 3 / 2);

            int end = len - 3;
            int i = start;
            int n = 0;

            while (i <= end) {
                int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 0x0ff) << 8)
                        | (((int) data[i + 2]) & 0x0ff);

                buf.append(LEGAL_CHARS[(d >> 18) & 63]);
                buf.append(LEGAL_CHARS[(d >> 12) & 63]);
                buf.append(LEGAL_CHARS[(d >> 6) & 63]);
                buf.append(LEGAL_CHARS[d & 63]);

                i += 3;

                if (n++ >= 14) {
                    n = 0;
                    buf.append(" ");
                }
            }

            if (i == start + len - 2) {
                int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 255) << 8);

                buf.append(LEGAL_CHARS[(d >> 18) & 63]);
                buf.append(LEGAL_CHARS[(d >> 12) & 63]);
                buf.append(LEGAL_CHARS[(d >> 6) & 63]);
                buf.append("=");
            } else if (i == start + len - 1) {
                int d = (((int) data[i]) & 0x0ff) << 16;

                buf.append(LEGAL_CHARS[(d >> 18) & 63]);
                buf.append(LEGAL_CHARS[(d >> 12) & 63]);
                buf.append("==");
            }

            return buf.toString();
        }

        private static int decode(char c) {
            if (c >= 'A' && c <= 'Z')
                return ((int) c) - 65;
            else if (c >= 'a' && c <= 'z')
                return ((int) c) - 97 + 26;
            else if (c >= '0' && c <= '9')
                return ((int) c) - 48 + 26 + 26;
            else
                switch (c) {
                    case '+':
                        return 62;
                    case '/':
                        return 63;
                    case '=':
                        return 0;
                    default:
                        throw new RuntimeException("unexpected code: " + c);
                }
        }

        /**
         * Decodes the given Base64 encoded String to a new byte array. The byte
         * array holding the decoded data is returned.
         */

        public static byte[] decode(String s) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                decode(s, bos);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            byte[] decodedBytes = bos.toByteArray();
            try {
                bos.close();
                bos = null;
            } catch (IOException ex) {
                System.err.println("Error while decoding BASE64: " + ex.toString());
            }
            return decodedBytes;
        }

        private static void decode(String s, OutputStream os) throws IOException {
            int i = 0;
            int len = s.length();
            while (true) {
                while (i < len && s.charAt(i) <= ' ')
                    i++;
                if (i == len)
                    break;
                int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6)
                        + (decode(s.charAt(i + 3)));
                os.write((tri >> 16) & 255);
                if (s.charAt(i + 2) == '=')
                    break;
                os.write((tri >> 8) & 255);
                if (s.charAt(i + 3) == '=')
                    break;
                os.write(tri & 255);
                i += 4;
            }
        }
    }

    /**
     * nfc 转换
     *
     * @param src
     * @return
     */
    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * nfc
     *
     * @param data
     * @return
     */
    public static String byteToString(byte[] data) {
        if (data.length < 4)
            return "0";
        long dataStr = data[0] + data[1] * 256 + data[2] * 256 * 256 + data[3] * 256 * 256 * 256;
        return String.valueOf(dataStr);
    }

     public static void main(String[] args) throws Exception {
     String testStr = "{\"command\":{\"ACTION\":\"UPDATE_USER_LABEL\",\"ACTIONCode\":\"2003\",\"subCMDID\":\"1\",\"superCMDID\":\"1\"},\"commandIndex\":\"1\",\"commandMode\":\"C\",\"commandTotal\":\"1\",\"commandType\":\"S\",\"data\":{\"userLabel\":{\"userFace\":{\"faceData\":\"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\",\"faceName\":\"赵武\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\"},\"userFinger1\":\"\",\"userFinger2\":\"\",\"userId\":\"9C305EC5587745FF9F0D8198512264D6\",\"userNFC\":\"943369744\"}},\"deviceId\":\"0f1a21d4e6fd3cb8-1-2-6277\",\"fileEdition\":\"1\",\"outOfTime\":\"\",\"sendTime\":\"2017-11-22 17:31:35\",\"serverId\":\"0\"}\n";
     String str = DESEncode.encrypt(testStr);
     System.out.println("加密后字符串: " + str);
     str = DESEncode.decrypt(str);
     System.out.println("解密后字符串: " + str);
     }
}
