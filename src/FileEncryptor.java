import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.GeneralSecurityException;

public class FileEncryptor {

    public static byte[] encryptData(String encryption, String password, byte[] data) throws GeneralSecurityException, IOException {
        Cipher cipher;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if ("AES".equals(encryption)) {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = Utils.generateKey("AES", password);
            IvParameterSpec iv = Utils.generateIv(cipher.getBlockSize());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            baos.write(iv.getIV()); // Write IV at the beginning of the output stream
        } else if ("DES".equals(encryption)) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Utils.generateDesKey(password);
            IvParameterSpec iv = Utils.generateIv(cipher.getBlockSize());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            baos.write(iv.getIV()); // Write IV at the beginning of the output stream
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
            SecretKey secretKey = Utils.generateKey("AES", password);
            IvParameterSpec iv = new IvParameterSpec(encryptedData, 0, cipher.getBlockSize());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } else if ("DES".equals(encryption)) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Utils.generateDesKey(password);
            IvParameterSpec iv = new IvParameterSpec(encryptedData, 0, cipher.getBlockSize());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } else {
            throw new GeneralSecurityException("Unsupported encryption type");
        }
        return cipher.doFinal(encryptedData, cipher.getBlockSize(), encryptedData.length - cipher.getBlockSize());
    }
}
