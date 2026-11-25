package com.sujeongring.domain.currency.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyReason {

    QUEST_REWARD("퀘스트 완료"),
    RELATIONSHIP_REWARD("관계 단계 달성"),
    EXTRA_MATCH("추가 매칭");
    private final String title;
}
