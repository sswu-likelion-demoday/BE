package com.sujeongring.domain.user.dto.response;

public record MyPageHomeResponse(
        String nickname,
        String departmentName,
        String studentNumber,
        String profileImageUrl,
        Integer currencyBalance,
        Integer blockedUserCount
) {
}