package com.sujeongring.domain.user.dto.response;

public record UpdateMyProfileResponse(
        String nickname,
        String profileImageUrl,
        String departmentName
) {
}