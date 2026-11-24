package tw.gigradar.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 密碼雜湊（JDK 內建 PBKDF2-HMAC-SHA256，不需額外套件）。登入 API 之後用 {@link #verify} 比對。
 * 儲存格式：{@code pbkdf2_sha256$迭代次數$salt$digest}
 */
public final class PasswordHasher {

    private static final int ITERATIONS = 200_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordHasher() {}

    public static String hash(String password) {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = HEX.formatHex(saltBytes);
        return "pbkdf2_sha256$" + ITERATIONS + "$" + salt + "$" + digest(password, salt, ITERATIONS);
    }

    public static boolean verify(String password, String stored) {
        String[] parts = stored == null ? new String[0] : stored.split("\\$");
        if (parts.length != 4 || !parts[1].matches("\\d{1,7}")) return false;
        String check = digest(password, parts[2], Integer.parseInt(parts[1]));
        // 固定時間比較，避免時間側通道攻擊
        return MessageDigest.isEqual(check.getBytes(StandardCharsets.US_ASCII), parts[3].getBytes(StandardCharsets.US_ASCII));
    }

    private static String digest(String password, String salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.US_ASCII), iterations, 256);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return HEX.formatHex(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
