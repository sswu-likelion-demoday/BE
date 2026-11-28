package com.sujeongring.domain.user.service;

import com.sujeongring.domain.matching.service.SajuMatching.SajuCalendarService;
import com.sujeongring.domain.matching.service.SajuMatching.SajuElementCalculator;
import com.sujeongring.domain.user.dto.response.SajuElementResponse;
import com.sujeongring.domain.user.entity.SajuProfile;
import com.sujeongring.domain.user.repository.SajuProfileRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SajuElementService {

    private final SajuProfileRepository sajuProfileRepository;
    private final SajuCalendarService sajuCalendarService;
    private final SajuElementCalculator sajuElementCalculator;

    public SajuElementResponse getMyElements(Long userId) {

        // 1. 사용자 사주 프로필 조회
        SajuProfile profile = sajuProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "사주 정보가 등록되지 않았습니다."
                        )
                );

        // 2. 필수 사주 정보 검증
        if (profile.getBirthDate() == null
                || profile.getCalendarType() == null) {

            throw new IllegalStateException(
                    "필수 사주 정보가 등록되지 않았습니다."
            );
        }

        // 3. 기존 간지 계산 서비스 재사용
        String gapja = sajuCalendarService
                .calculateGapja(profile);

        // 4. 오행 분포 및 부족한 기운 계산
        return sajuElementCalculator.calculate(gapja);
    }
}