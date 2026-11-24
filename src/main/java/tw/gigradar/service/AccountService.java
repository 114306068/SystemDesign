package tw.gigradar.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tw.gigradar.dto.PasswordChange;
import tw.gigradar.dto.ProfileUpdate;
import tw.gigradar.dto.RegisterRequest;
import tw.gigradar.model.User;
import tw.gigradar.model.enums.UserRole;
import tw.gigradar.repository.UserRepository;
import tw.gigradar.security.AppUserDetailsService;

/** 會員帳號：註冊、修改個人資料、修改密碼（D1）。 */
@Service
@Transactional
public class AccountService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest req) {
        if (req.role() == UserRole.admin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能自行註冊管理者帳號");
        }
        String email = AppUserDetailsService.normalizeEmail(req.email());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "這個 Email 已經註冊過");
        }
        User user = new User(email, req.displayName().strip(), req.role(), passwordEncoder.encode(req.password()));
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public User get(Integer userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號不存在，請重新登入"));
    }

    public User updateProfile(Integer userId, ProfileUpdate req) {
        User user = get(userId);
        user.setDisplayName(req.displayName().strip());
        user.setPhone(blankToNull(req.phone()));
        user.setDistrict(blankToNull(req.district()));
        user.setFavoriteGenre(blankToNull(req.favoriteGenre()));
        return user; // 交易結束時 JPA 自動把修改寫回資料庫
    }

    public void changePassword(Integer userId, PasswordChange req) {
        User user = get(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目前的密碼不正確");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
