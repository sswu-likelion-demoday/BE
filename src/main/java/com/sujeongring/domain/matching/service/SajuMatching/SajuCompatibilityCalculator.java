package com.sujeongring.domain.matching.service.SajuMatching;

//궁합 점수 계산
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SajuCompatibilityCalculator {

    // 천간 → 오행
    private static final Map<Character, String> ELEMENTS = Map.of(
            '甲', "WOOD", '乙', "WOOD",
            '丙', "FIRE", '丁', "FIRE",
            '戊', "EARTH", '己', "EARTH",
            '庚', "METAL", '辛', "METAL",
            '壬', "WATER", '癸', "WATER"
    );

    // 육합 관계
    private static final Set<String> HARMONY = Set.of(
            "子丑", "寅亥", "卯戌",
            "辰酉", "巳申", "午未"
    );

    // 충 관계
    private static final Set<String> CLASH = Set.of(
            "子午", "丑未", "寅申",
            "卯酉", "辰戌", "巳亥"
    );

    public int calculate(String myGapja, String candidateGapja) {

        String[] mine = parse(myGapja);
        String[] other = parse(candidateGapja);

        String myYear = mine[0];
        String myDay = mine[2];

        String otherYear = other[0];
        String otherDay = other[2];

        int score = 0;

        // 1. 일간 오행 관계: 최대 40점
        score += elementScore(
                myDay.charAt(0),
                otherDay.charAt(0),
                40
        );

        // 2. 일지 관계: 최대 30점
        score += branchScore(
                myDay.charAt(1),
                otherDay.charAt(1),
                30
        );

        // 3. 연간 오행 관계: 최대 20점
        score += elementScore(
                myYear.charAt(0),
                otherYear.charAt(0),
                20
        );

        // 4. 연지 관계: 최대 10점
        score += branchScore(
                myYear.charAt(1),
                otherYear.charAt(1),
                10
        );

        return Math.max(0, Math.min(100, score));
    }

    private String[] parse(String gapja) {
        if (gapja == null) {
            throw new IllegalArgumentException("간지 정보가 없습니다.");
        }

        String[] parts = gapja.trim().split("\\s+");

        if (parts.length < 3) {
            throw new IllegalArgumentException("간지 형식이 올바르지 않습니다.");
        }

        String[] result = new String[3];

        for (int i = 0; i < 3; i++) {
            String part = parts[i];

            if (part.length() < 2) {
                throw new IllegalArgumentException("간지 형식이 올바르지 않습니다.");
            }

            String pillar = part.substring(0, 2);

            if (!ELEMENTS.containsKey(pillar.charAt(0))
                    || "子丑寅卯辰巳午未申酉戌亥"
                    .indexOf(pillar.charAt(1)) < 0) {
                throw new IllegalArgumentException("지원하지 않는 간지입니다.");
            }

            result[i] = pillar;
        }

        return result;
    }

    private int elementScore(char a, char b, int max) {

        String first = ELEMENTS.get(a);
        String second = ELEMENTS.get(b);

        if (first.equals(second)) {
            return (int) (max * 0.8);
        }

        if (generates(first, second)
                || generates(second, first)) {
            return max;
        }

        if (controls(first, second)
                || controls(second, first)) {
            return (int) (max * 0.3);
        }

        return (int) (max * 0.6);
    }

    private boolean generates(String a, String b) {
        return switch (a) {
            case "WOOD" -> b.equals("FIRE");
            case "FIRE" -> b.equals("EARTH");
            case "EARTH" -> b.equals("METAL");
            case "METAL" -> b.equals("WATER");
            case "WATER" -> b.equals("WOOD");
            default -> false;
        };
    }

    private boolean controls(String a, String b) {
        return switch (a) {
            case "WOOD" -> b.equals("EARTH");
            case "EARTH" -> b.equals("WATER");
            case "WATER" -> b.equals("FIRE");
            case "FIRE" -> b.equals("METAL");
            case "METAL" -> b.equals("WOOD");
            default -> false;
        };
    }

    private int branchScore(char a, char b, int max) {

        String pair = "" + a + b;
        String reverse = "" + b + a;

        if (HARMONY.contains(pair) || HARMONY.contains(reverse)) {
            return max;
        }

        if (CLASH.contains(pair) || CLASH.contains(reverse)) {
            return (int) (max * 0.2);
        }

        if (a == b) {
            return (int) (max * 0.8);
        }

        return (int) (max * 0.6);
    }
}
