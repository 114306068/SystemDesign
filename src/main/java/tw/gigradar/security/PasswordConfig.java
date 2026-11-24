package tw.gigradar.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密碼雜湊器。和 SecurityConfig 分開，因為 seed / crawl 這些不開網站的工作也需要它
 * （SecurityConfig 只在啟動網站時載入）。
 */
@Configuration
public class PasswordConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder();
    }
}
