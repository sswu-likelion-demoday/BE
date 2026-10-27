package com.sujeongring.domain.matching.exception;

public class DailyFreeMatchingLimitExceededException extends RuntimeException {
    public DailyFreeMatchingLimitExceededException() {
        super("오늘 무료 매칭 3회를 모두 사용했습니다.");
    }
}
