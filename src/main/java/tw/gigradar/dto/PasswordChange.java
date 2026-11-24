package tw.gigradar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChange(
        @NotBlank(message = "請輸入目前的密碼") String currentPassword,
        @NotBlank(message = "請輸入新密碼") @Size(min = 8, max = 100, message = "新密碼至少 8 個字元") String newPassword) {}
