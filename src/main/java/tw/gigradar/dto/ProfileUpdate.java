package tw.gigradar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 修改個人資料。Email 與角色不能在這裡改。空字串視為清除該欄位。 */
public record ProfileUpdate(
        @NotBlank(message = "請輸入暱稱") @Size(max = 100, message = "暱稱最多 100 字") String displayName,
        @Pattern(regexp = "^$|^[0-9+() -]{6,20}$", message = "電話格式不正確") String phone,
        @Size(max = 20, message = "地區最多 20 字") String district,
        @Size(max = 50, message = "風格最多 50 字") String favoriteGenre) {}
