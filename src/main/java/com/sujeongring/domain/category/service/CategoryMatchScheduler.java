package com.sujeongring.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMatchScheduler {

    private final CategoryMatchService categoryMatchService;

    @Scheduled(fixedDelayString = "${category.match.rematch.fixed-delay-ms:300000}")
    public void retryWaitingCategoryMatches() {
        categoryMatchService.retryWaitingMatches();
    }
}
