package com.sujeongring.domain.category.dto.response;

import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;
import com.sujeongring.domain.user.enums.MbtiType;

import java.util.List;

public record CategoryMatchResultResponse(
        Long requestId,
        CategoryType category,
        CategoryMatchStatus status,
        Long groupId,
        List<Member> members
) {
    public record Member(
            Long userId,
            String nickname,
            String departmentName,
            Integer admissionYear,
            String profileImageUrl,
            MbtiType mbti,
            String contact
    ) {
    }
}
