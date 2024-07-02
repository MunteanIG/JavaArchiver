import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

public class Utils {

    public static SecretKey generateKey(String algorithm, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Use a secure method to generate the key from the password
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), "someRandomSalt".getBytes(), 65536, "AES".equals(algorithm) ? 128 : 64);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), algorithm);
    }

    public static SecretKeySpec generateDesKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = "someRandomSalt".getBytes(); // Use a secure salt in practice
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 56); // 56 bits for DES key
        SecretKey tmp = factory.generateSecret(spec);
        byte[] key = Arrays.copyOf(tmp.getEncoded(), 8); // DES key size is 8 bytes
        return new SecretKeySpec(key, "DES");
    }

    public static IvParameterSpec generateIv(int size) {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
