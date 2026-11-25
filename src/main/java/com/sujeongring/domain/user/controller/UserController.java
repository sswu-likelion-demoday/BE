package com.sujeongring.domain.user.controller;

//마이페이지
import com.sujeongring.domain.user.dto.request.MatchingProfileUpdateRequest;
import com.sujeongring.domain.user.dto.request.UpdateMyProfileRequest;
import com.sujeongring.domain.user.dto.response.MatchingProfileResponse;
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.dto.response.UpdateMyProfileResponse;
import com.sujeongring.domain.user.service.UserService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService myPageService;

    @GetMapping("/me")
    public ApiResponse<MyPageHomeResponse> getMyPageHome(
            @CurrentUserId Long userId
    ) {
        MyPageHomeResponse response =
                myPageService.getMyPageHome(userId);

        return ApiResponse.success(
                "마이페이지 정보를 조회하였습니다.",
                response
        );
    }

    @GetMapping("/me/profile")
    public ApiResponse<MyPageHomeResponse> getMyProfile(
            @CurrentUserId Long userId
    ) {
        MyPageHomeResponse response =
                myPageService.getMyPageHome(userId);

        return ApiResponse.success(
                "내 정보를 조회하였습니다.",
                response
        );
    }

    @PatchMapping("/me/profile")
    public ApiResponse<UpdateMyProfileResponse> updateMyProfile(
            @CurrentUserId Long userId,
            @RequestBody UpdateMyProfileRequest request
    ) {
        UpdateMyProfileResponse response =
                myPageService.updateMyProfile(userId, request);

        return ApiResponse.success(
                "내 정보가 수정되었습니다.",
                response
        );
    }

    private final UserService userService;

    @GetMapping("/me/matching-profile")
    @Operation(
            summary = "매칭 정보 조회",
            description = "온보딩에서 설정한 매칭 관련 성향 및 설정을 조회합니다."
    )
    public ApiResponse<MatchingProfileResponse> getMatchingProfile(
            @CurrentUserId Long userId
    ) {

        MatchingProfileResponse response =
                userService.getMatchingProfile(userId);

        return ApiResponse.success(
                "매칭 정보를 조회하였습니다.",
                response
        );
    }

    @PatchMapping("/me/matching-profile")
    @Operation(
            summary = "매칭 정보 수정",
            description = "온보딩에서 설정한 매칭 정보를 수정합니다. " +
                    "요청에 포함된 항목만 변경됩니다."
    )
    public ApiResponse<MatchingProfileResponse> updateMatchingProfile(
            @CurrentUserId Long userId,
            @RequestBody MatchingProfileUpdateRequest request
    ) {

        MatchingProfileResponse response =
                userService.updateMatchingProfile(
                        userId,
                        request
                );

        return ApiResponse.success(
                "매칭 정보를 수정하였습니다.",
                response
        );
    }
}

