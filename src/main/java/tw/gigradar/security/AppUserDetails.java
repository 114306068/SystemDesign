package tw.gigradar.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tw.gigradar.model.User;
import tw.gigradar.model.enums.UserRole;

/**
 * 登入中的使用者（存在 Session 裡）。Controller 用 {@code @AuthenticationPrincipal AppUserDetails me} 取得。
 * 角色轉成 Spring Security 的權限：audience → ROLE_AUDIENCE、admin → ROLE_ADMIN……
 */
public class AppUserDetails implements UserDetails, CredentialsContainer {

    private final Integer id;
    private final String email;
    private final UserRole role;
    private String passwordHash;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.passwordHash = user.getPasswordHash();
    }

    public Integer getId() { return id; }
    public UserRole getRole() { return role; }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
    }

    /** 登入驗證完就把密碼雜湊清掉，不留在 Session 裡。 */
    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}
