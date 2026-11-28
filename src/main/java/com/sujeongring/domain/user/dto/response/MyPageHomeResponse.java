package com.sujeongring.domain.user.dto.response;

public record MyPageHomeResponse(
        String nickname,
        String profileImageUrl,
        String departmentName,
        String studentNumber,
        Integer admissionYear
) {
}