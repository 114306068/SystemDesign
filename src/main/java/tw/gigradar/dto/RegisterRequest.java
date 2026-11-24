package tw.gigradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tw.gigradar.model.enums.UserRole;

/** 註冊：role 可選 audience / performer / venue（管理者不能自行註冊）。 */
public record RegisterRequest(
        @NotBlank(message = "請輸入 Email") @Email(message = "Email 格式不正確") @Size(max = 255) String email,
        @NotBlank(message = "請輸入密碼") @Size(min = 8, max = 100, message = "密碼至少 8 個字元") String password,
        @NotBlank(message = "請輸入暱稱") @Size(max = 100, message = "暱稱最多 100 字") String displayName,
        @NotNull(message = "請選擇身分") UserRole role) {}
