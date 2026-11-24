package tw.gigradar.dto;

import java.time.LocalDateTime;
import tw.gigradar.model.User;

/** 登入者自己的資料（不含密碼）。 */
public record MeOut(Integer id, String email, String displayName, String role, String phone, String district,
                    String favoriteGenre, LocalDateTime createdAt) {

    public static MeOut from(User u) {
        return new MeOut(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole().name(), u.getPhone(),
                u.getDistrict(), u.getFavoriteGenre(), u.getCreatedAt());
    }
}
