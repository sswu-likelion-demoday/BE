package com.sujeongring.domain.matching.service.SajuMatching;

import com.sujeongring.domain.user.dto.response.SajuElementResponse;
import com.sujeongring.domain.user.enums.SajuElement;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SajuElementCalculator {

    // 천간 → 오행
    private static final Map<Character, SajuElement> STEM_ELEMENTS =
            Map.of(
                    '甲', SajuElement.WOOD,
                    '乙', SajuElement.WOOD,
                    '丙', SajuElement.FIRE,
                    '丁', SajuElement.FIRE,
                    '戊', SajuElement.EARTH,
                    '己', SajuElement.EARTH,
                    '庚', SajuElement.METAL,
                    '辛', SajuElement.METAL,
                    '壬', SajuElement.WATER,
                    '癸', SajuElement.WATER
            );

    // 지지 → 대표 오행
    private static final Map<Character, SajuElement> BRANCH_ELEMENTS =
            Map.ofEntries(
                    Map.entry('子', SajuElement.WATER),
                    Map.entry('丑', SajuElement.EARTH),
                    Map.entry('寅', SajuElement.WOOD),
                    Map.entry('卯', SajuElement.WOOD),
                    Map.entry('辰', SajuElement.EARTH),
                    Map.entry('巳', SajuElement.FIRE),
                    Map.entry('午', SajuElement.FIRE),
                    Map.entry('未', SajuElement.EARTH),
                    Map.entry('申', SajuElement.METAL),
                    Map.entry('酉', SajuElement.METAL),
                    Map.entry('戌', SajuElement.EARTH),
                    Map.entry('亥', SajuElement.WATER)
            );

    public SajuElementResponse calculate(String gapja) {

        if (gapja == null || gapja.isBlank()) {
            throw new IllegalArgumentException(
                    "간지 정보가 없습니다."
            );
        }

        String[] pillars = gapja.trim().split("\\s+");

        if (pillars.length < 3) {
            throw new IllegalArgumentException(
                    "간지 형식이 올바르지 않습니다."
            );
        }

        // 1. 오행별 개수 초기화
        Map<SajuElement, Integer> elements =
                new EnumMap<>(SajuElement.class);

        for (SajuElement element : SajuElement.values()) {
            elements.put(element, 0);
        }

        // 2. 연주, 월주, 일주 분석
        for (int i = 0; i < 3; i++) {

            String pillar = pillars[i];

            if (pillar.length() < 2) {
                throw new IllegalArgumentException(
                        "간지 형식이 올바르지 않습니다."
                );
            }

            SajuElement stem =
                    STEM_ELEMENTS.get(pillar.charAt(0));

            SajuElement branch =
                    BRANCH_ELEMENTS.get(pillar.charAt(1));

            if (stem == null || branch == null) {
                throw new IllegalArgumentException(
                        "지원하지 않는 간지입니다."
                );
            }

            elements.merge(stem, 1, Integer::sum);
            elements.merge(branch, 1, Integer::sum);
        }

        // 3. 가장 적은 오행 개수 조회
        int minCount = elements.values()
                .stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        // 4. 가장 부족한 오행 추출
        List<SajuElement> deficientElements =
                elements.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue() == minCount
                        )
                        .map(Map.Entry::getKey)
                        .toList();

        // 5. 분석 결과 반환
        return new SajuElementResponse(
                deficientElements,
                elements
        );
    }
}
