import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class FileEncryptor {

    public static byte[] encryptData(String encryption, String password, byte[] data) throws GeneralSecurityException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Cipher cipher;
        if ("AES".equals(encryption)) {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = generateKey("AES", password);
            IvParameterSpec iv = generateIv(cipher.getBlockSize());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            baos.write(iv.getIV());
        } else if ("DES".equals(encryption)) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = generateDesKey(password);
            IvParameterSpec iv = generateIv(cipher.getBlockSize());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            baos.write(iv.getIV());
        } else {
            throw new GeneralSecurityException("Unsupported encryption type");
        }
        byte[] encryptedData = cipher.doFinal(data);
        baos.write(encryptedData);
        return baos.toByteArray();
    }

    public static byte[] decryptData(String encryption, String password, byte[] encryptedData) throws GeneralSecurityException {
        Cipher cipher;
        if ("AES".equals(encryption)) {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = generateKey("AES", password);
            IvParameterSpec iv = new IvParameterSpec(encryptedData, 0, cipher.getBlockSize());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } else if ("DES".equals(encryption)) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = generateDesKey(password);
            IvParameterSpec iv = new IvParameterSpec(encryptedData, 0, cipher.getBlockSize());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } else {
            throw new GeneralSecurityException("Unsupported encryption type");
        }
        return cipher.doFinal(encryptedData, cipher.getBlockSize(), encryptedData.length - cipher.getBlockSize());
    }

    private static SecretKey generateKey(String algorithm, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), "someRandomSalt".getBytes(), 65536, "AES".equals(algorithm) ? 128 : 64);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), algorithm);
    }

    private static SecretKeySpec generateDesKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = "someRandomSalt".getBytes();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 56);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] key = Arrays.copyOf(tmp.getEncoded(), 8);
        return new SecretKeySpec(key, "DES");
    }

    private static IvParameterSpec generateIv(int size) {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
