package com.sujeongring.domain.category.dto.response;

import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;

import java.time.LocalDateTime;

public record CategoryMatchCreateResponse(
        Long requestId,
        CategoryType category,
        CategoryMatchStatus status,
        LocalDateTime createdAt
) {
}
