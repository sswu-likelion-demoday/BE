package com.sujeongring.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import com.sujeongring.global.auth.CurrentUserId;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급, 비밀번호 재설정 API")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증번호 발송", description = "회원가입/비밀번호 재설정을 위한 인증번호를 이메일로 발송합니다.")
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
    @Operation(summary = "이메일 인증번호 검증", description = "발급된 인증번호를 검증해 이메일 인증을 완료합니다.")
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
    @Operation(summary = "학번 중복 확인", description = "회원가입 전 학번 사용 가능 여부를 조회합니다.")
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

    @PostMapping(
            value = "/signup",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "회원가입",
            description = "회원가입을 진행합니다."
    )
    public ApiResponse<SignupResponse> signup(
            @RequestPart("request")
            String requestJson,

            @RequestPart(
                    value = "profileImage",
                    required = false
            )
            MultipartFile profileImage
    ) {

        SignupRequest request =
                parseSignupRequest(requestJson);

        return ApiResponse.success(
                authService.signup(
                        request,
                        profileImage
                )
        );
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "학번/비밀번호로 로그인하고 토큰을 발급합니다.")
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
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급합니다.")
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
    @Operation(summary = "비밀번호 재설정", description = "이메일 인증 완료 후 비밀번호를 재설정합니다.")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);

        return ApiResponse.success(
                "비밀번호가 재설정되었습니다.",
                null
        );
    }

    private SignupRequest parseSignupRequest(
            String requestJson
    ) {
        final SignupRequest request;

        request = objectMapper.readValue(
                requestJson,
                SignupRequest.class
        );

        Set<ConstraintViolation<SignupRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return request;
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "저장된 Refresh Token을 삭제합니다."
    )
    public ApiResponse<Void> logout(
            @CurrentUserId Long userId
    ) {

        authService.logout(userId);

        return ApiResponse.success(
                "로그아웃되었습니다.",
                null
        );
    }
}