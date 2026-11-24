package tw.gigradar.security;

import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tw.gigradar.repository.UserRepository;

/** 登入時 Spring Security 用 email 到 users 表找人。 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return users.findByEmail(normalizeEmail(email))
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    /** email 一律去空白、轉小寫再存 / 比對，登入不分大小寫。 */
    public static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
