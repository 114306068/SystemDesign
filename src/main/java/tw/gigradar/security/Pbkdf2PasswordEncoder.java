package tw.gigradar.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import tw.gigradar.service.PasswordHasher;

/** 讓 Spring Security 使用專案原本的 PBKDF2 雜湊格式，示範帳號與既有密碼都能直接登入。 */
public class Pbkdf2PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return PasswordHasher.hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return PasswordHasher.verify(rawPassword.toString(), encodedPassword);
    }
}
