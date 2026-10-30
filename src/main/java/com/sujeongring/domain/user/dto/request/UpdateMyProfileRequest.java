package com.sujeongring.domain.user.dto.request;

public record UpdateMyProfileRequest(
        String nickname,
        String departmentName
) {
}