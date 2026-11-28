package com.sujeongring.domain.user.dto.response;

import com.sujeongring.domain.user.enums.SajuElement;

import java.util.List;
import java.util.Map;

public record SajuElementResponse(
        List<SajuElement> deficientElements,
        Map<SajuElement, Integer> elements
) {
}