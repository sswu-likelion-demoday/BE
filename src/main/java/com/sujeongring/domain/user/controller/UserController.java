package com.sujeongring.domain.user.controller;

//마이페이지
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.service.UserService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}