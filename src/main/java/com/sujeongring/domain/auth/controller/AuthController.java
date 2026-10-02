package com.sujeongring.domain.auth.controller;

import com.sujeongring.domain.auth.dto.request.*;
import com.sujeongring.domain.auth.dto.response.EmailSendResponse;
import com.sujeongring.domain.auth.dto.response.EmailVerifyResponse;
import com.sujeongring.domain.auth.dto.response.LoginResponse;
import com.sujeongring.domain.auth.dto.response.SignupResponse;
import com.sujeongring.domain.auth.dto.response.StudentNumberCheckResponse;
import com.sujeongring.domain.auth.dto.response.TokenResponse;
import com.sujeongring.domain.auth.service.AuthService;
import com.sujeongring.domain.auth.service.EmailVerificationService;
import com.sujeongring.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    @PostMapping("/email/send")
    public ApiResponse<EmailSendResponse> sendEmail(
            @Valid @RequestBody EmailSendRequest request
    ) {
        EmailSendResponse response =
                emailVerificationService.sendVerificationCode(request.email());

        return ApiResponse.success(
                "인증번호가 발송되었습니다.",
                response
        );
    }

    @PostMapping("/email/verify")
    public ApiResponse<EmailVerifyResponse> verifyEmail(
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        EmailVerifyResponse response =
                emailVerificationService.verifyCode(
                        request.email(),
                        request.verificationCode()
                );

        return ApiResponse.success(
                "이메일 인증이 완료되었습니다.",
                response
        );
    }

    @GetMapping("/student-numbers/check")
    public ApiResponse<StudentNumberCheckResponse> checkStudentNumber(
            @RequestParam String studentNumber
    ) {
        StudentNumberCheckResponse response =
                authService.checkStudentNumber(studentNumber);

        String message = response.available()
                ? "사용 가능한 학번입니다."
                : "이미 사용 중인 학번입니다.";

        return ApiResponse.success(message, response);
    }

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response =
                authService.signup(request);

        return ApiResponse.success(
                "회원가입이 완료되었습니다.",
                response
        );
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response =
                authService.login(request);

        return ApiResponse.success(
                "로그인에 성공하였습니다.",
                response
        );
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(
            @Valid @RequestBody TokenReissueRequest request
    ) {
        TokenResponse response =
                authService.reissue(request);

        return ApiResponse.success(
                "토큰을 재발급하였습니다.",
                response
        );
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);

        return ApiResponse.success(
                "비밀번호가 재설정되었습니다.",
                null
        );
    }
}