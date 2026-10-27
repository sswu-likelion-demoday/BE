package com.sujeongring.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "학번은 필수입니다.")
        String studentNumber,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        String newPassword
) {
}