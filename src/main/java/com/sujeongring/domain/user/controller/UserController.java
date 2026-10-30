package com.sujeongring.domain.user.controller;

//마이페이지
import com.sujeongring.domain.user.dto.request.UpdateMyProfileRequest;
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.dto.response.UpdateMyProfileResponse;
import com.sujeongring.domain.user.service.UserService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
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


}