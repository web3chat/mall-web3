package com.fzm.mall.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class AesUtils {
    private static final String AES = "AES";
    private static final String AES_CBC_NoPadding = "AES/CBC/NoPadding";
    private static final byte[] magic_bytes = longToBytes(Long.parseLong("ABCD1234", 16));
    private static final int long_to_bytes_length = 4;

    // 头 + 密钥长度 + 正文长度 + 密钥 + 加密正文（iv+body）
    public static String decode(String source) {
        try {
            byte[] sourceBytes = Base64.getDecoder().decode(source);

            // 头
            int srcPos = 0;
            byte[] magicBytes = new byte[long_to_bytes_length];
            System.arraycopy(sourceBytes, srcPos, magicBytes, 0, long_to_bytes_length);
            boolean equals = Arrays.equals(magicBytes, magic_bytes);
            if (!equals) {
                return null;
            }

            // 密钥长度
            srcPos += long_to_bytes_length;
            byte[] keyLengthBytes = new byte[long_to_bytes_length];
            System.arraycopy(sourceBytes, srcPos, keyLengthBytes, 0, long_to_bytes_length);
            int keyLength = bytesToInt(keyLengthBytes);

            // 正文长度
            srcPos += long_to_bytes_length;
            byte[] bodyLengthBytes = new byte[long_to_bytes_length];
            System.arraycopy(sourceBytes, srcPos, bodyLengthBytes, 0, long_to_bytes_length);
            int bodyLength = bytesToInt(bodyLengthBytes);

            // 密钥
            srcPos += long_to_bytes_length;
            byte[] keyBytes = new byte[keyLength];
            System.arraycopy(sourceBytes, srcPos, keyBytes, 0, keyLength);

            // 加密正文
            srcPos += keyLength;
            byte[] bodyEncrypt = new byte[bodyLength];
            System.arraycopy(sourceBytes, srcPos, bodyEncrypt, 0, bodyLength);

            byte[] decrypt = decrypt(bodyEncrypt, keyBytes);

            return new String(decrypt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return source;
        }

    }

    // 头 + 密钥长度 + 正文长度 + 密钥 + 加密正文（iv+body）
    public static String encode(String source) {
        try {
            byte[] keyBytes = new byte[16];
            new SecureRandom().nextBytes(keyBytes);
            byte[] keyBytesLengthBytes = longToBytes(keyBytes.length);

            byte[] bodyEncrypt = encrypt(source.getBytes(StandardCharsets.UTF_8), keyBytes);
            byte[] bodyEncryptLengthBytes = longToBytes(bodyEncrypt.length);

            byte[] target = new byte[magic_bytes.length + keyBytesLengthBytes.length + bodyEncryptLengthBytes.length + keyBytes.length + bodyEncrypt.length];

            int destPos = 0;
            System.arraycopy(magic_bytes, 0, target, destPos, magic_bytes.length);

            destPos += magic_bytes.length;
            System.arraycopy(keyBytesLengthBytes, 0, target, destPos, keyBytesLengthBytes.length);

            destPos += keyBytesLengthBytes.length;
            System.arraycopy(bodyEncryptLengthBytes, 0, target, destPos, bodyEncryptLengthBytes.length);

            destPos += bodyEncryptLengthBytes.length;
            System.arraycopy(keyBytes, 0, target, destPos, keyBytes.length);

            destPos += keyBytes.length;
            System.arraycopy(bodyEncrypt, 0, target, destPos, bodyEncrypt.length);

            return Base64.getEncoder().encodeToString(target);
        } catch (Exception e) {
            return source;
        }
    }

    private static byte[] encrypt(byte[] contentBytes, byte[] keyBytes) throws Exception {
        contentBytes = paddingTail(contentBytes);

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_NoPadding);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

        byte[] bytes = cipher.doFinal(contentBytes);

        byte[] targetBytes = new byte[16 + bytes.length];
        System.arraycopy(iv, 0, targetBytes, 0, 16);
        System.arraycopy(bytes, 0, targetBytes, 16, bytes.length);

        return targetBytes;
    }

    private static byte[] decrypt(byte[] encryptBytes, byte[] keyBytes) throws Exception {
        byte[] iv = new byte[16];
        System.arraycopy(encryptBytes, 0, iv, 0, 16);

        int contentLength = encryptBytes.length - 16;
        byte[] contentBytes = new byte[contentLength];
        System.arraycopy(encryptBytes, 16, contentBytes, 0, contentLength);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_NoPadding);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

        byte[] bytes = cipher.doFinal(contentBytes);

        int tailLength = bytes[contentLength - 1] & 0xff;
        byte[] targetBytes = new byte[contentLength - tailLength];
        System.arraycopy(bytes, 0, targetBytes, 0, targetBytes.length);

        return targetBytes;
    }

    private static byte[] longToBytes(long num) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (num & 0xff);
        bytes[2] = (byte) (num >> 8 & 0xff);
        bytes[1] = (byte) (num >> 16 & 0xff);
        bytes[0] = (byte) (num >> 24 & 0xff);
        return bytes;
    }

    private static int bytesToInt(byte[] bytes) {
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            num += (bytes[i] & 0xff) << ((3 - i) * 8);
        }
        return num;
    }

    private static byte[] paddingTail(byte[] bytes) {
        int length = bytes.length;
        int tailLength = 16 - length % 16;

        byte[] tailBytes = new byte[tailLength];
        Arrays.fill(tailBytes, (byte) tailLength);

        byte[] targetBytes = new byte[length + tailLength];
        System.arraycopy(bytes, 0, targetBytes, 0, length);
        System.arraycopy(tailBytes, 0, targetBytes, length, tailLength);

        return targetBytes;
    }
}
