package com.sujeongring.domain.matching.service.NormalMatching;

import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;


//온보딩 정보를 비교해서 사용자 간 유사도 점수를 계산
@Component
public class MatchingScoreCalculator {
    // Four equally weighted categories until product weights are agreed.
    public int calculate(Set<?> stylesA, Set<?> stylesB,
                         Set<?> interestsA, Set<?> interestsB,
                         Set<?> hobbiesA, Set<?> hobbiesB,
                         Set<?> friendTypesA, Set<?> friendTypesB) {
        return (int) Math.round(25.0 * (jaccard(stylesA, stylesB)
                + jaccard(interestsA, interestsB)
                + jaccard(hobbiesA, hobbiesB)
                + jaccard(friendTypesA, friendTypesB)));
    }

    private double jaccard(Set<?> a, Set<?> b) {
        if (a.isEmpty() && b.isEmpty()) return 0.0;
        Set<Object> union = new HashSet<>(a);
        union.addAll(b);
        Set<Object> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        return (double) intersection.size() / union.size();
    }
}
