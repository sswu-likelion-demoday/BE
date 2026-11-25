package com.sujeongring.domain.category.dto.response;

import com.sujeongring.domain.category.enums.CategoryMatchState;
import com.sujeongring.domain.category.enums.CategoryType;

import java.util.List;

public record CategoryMatchStatusResponse(
        List<CategoryStateItem> categories
) {
    public record CategoryStateItem(
            CategoryType category,
            CategoryMatchState state,
            Long requestId,
            boolean hasNewResult
    ) {
    }
}
