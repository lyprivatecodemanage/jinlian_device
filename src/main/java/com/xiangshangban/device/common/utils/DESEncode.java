package com.xiangshangban.device.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class DESEncode {
	// 密钥  
    private final static String SECRET_KEY = "lkjrewqfdsfdsaAD876dsakndwqedlKJ";
    //private final static String SECRET_KEY = "909485";
    // 向量  
    private final static String IV = "01234567";  
    // 加解密统一使用的编码方式  
    private final static String ENCODING = "UTF-8";  
    
    public DESEncode getInstance(){
    	return new DESEncode();
    }
  
    /** 
     * 3DES加密 
     *  
     * @param plainText 普通文本 
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
     * @param encryptText 加密文本 
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
        private static final char[] LEGAL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        public static  String encode(byte[] data) {  
            int start = 0;  
            int len = data.length;  
            StringBuffer buf = new StringBuffer(data.length * 3 / 2);  
      
            int end = len - 3;  
            int i = start;  
            int n = 0;  
      
            while (i <= end) {  
                int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 0x0ff) << 8) | (((int) data[i + 2]) & 0x0ff);  
      
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
         * Decodes the given Base64 encoded String to a new byte array. The byte array holding the decoded data is returned. 
         */  
      
        public static  byte[] decode(String s) {  
      
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
      
        private static  void decode(String s, OutputStream os) throws IOException {  
            int i = 0;  
            int len = s.length();  
            while (true) {  
                while (i < len && s.charAt(i) <= ' ')  
                    i++;  
                if (i == len)  
                    break;  
                int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6) + (decode(s.charAt(i + 3)));  
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
    
    public static void main(String[] args) throws Exception {  
        String testStr = "胡海海,胡海海有限公司,2018-01-01 00:00:00,2018-01-02 23:59:59,2545abef;44646abc";  
        String str = DESEncode.encrypt(testStr);  
        System.out.println("加密后字符串: " + str);  
        str = DESEncode.decrypt(str);  
        System.out.println("解密后字符串: " + str);  
    }  
    public static String getSecretKey() {
		return SECRET_KEY;
	} 
}  
