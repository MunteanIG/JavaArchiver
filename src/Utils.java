import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;

public class Utils {

    public static IvParameterSpec generateIv(int size) {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
