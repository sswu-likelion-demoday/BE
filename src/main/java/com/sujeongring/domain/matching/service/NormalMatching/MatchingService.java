package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.user.entity.*;
import com.sujeongring.domain.user.enums.*;
import com.sujeongring.domain.user.repository.onboarding.UserFriendTypeRepository;
import com.sujeongring.domain.user.repository.onboarding.UserHobbyRepository;
import com.sujeongring.domain.user.repository.onboarding.UserInterestRepository;
import com.sujeongring.domain.user.repository.onboarding.UserSocialStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


//scoreCalculator를 이용한 후보별 유사도 계산
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserSocialStyleRepository socialStyleRepository;
    private final UserInterestRepository interestRepository;
    private final UserHobbyRepository hobbyRepository;
    private final UserFriendTypeRepository friendTypeRepository;
    private final MatchingScoreCalculator scoreCalculator;

    @Transactional(readOnly = true)
    public Map<Long, Integer> scoreCandidates(Long requesterId, Collection<Long> eligibleCandidateIds) {
        if (eligibleCandidateIds.isEmpty()) return Map.of();
        Set<Long> ids = new HashSet<>(eligibleCandidateIds);
        ids.add(requesterId);

        Map<Long, Set<SocialStyleType>> styles = group(
                socialStyleRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(), UserSocialStyle::getSocialStyle);

        Map<Long, Set<InterestType>> interests = group(
                interestRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(), UserInterest::getInterest);

        Map<Long, Set<HobbyType>> hobbies = group(
                hobbyRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(), UserHobby::getHobby);

        Map<Long, Set<FriendType>> friends = group(
                friendTypeRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(), UserFriendType::getFriendType);

        Map<Long, Integer> result = new HashMap<>();
        for (Long candidateId : eligibleCandidateIds) {
            if (candidateId.equals(requesterId)) continue;
            result.put(candidateId, scoreCalculator.calculate(
                    values(styles, requesterId), values(styles, candidateId),
                    values(interests, requesterId), values(interests, candidateId),
                    values(hobbies, requesterId), values(hobbies, candidateId),
                    values(friends, requesterId), values(friends, candidateId)));
        }
        return result;
    }

    private static <T> Set<T> values(Map<Long, Set<T>> map, Long id) {
        return map.getOrDefault(id, Set.of());
    }

    private static <E, T> Map<Long, Set<T>> group(
            List<E> entries, Function<E, Long> userId, Function<E, T> value) {
        return entries.stream().collect(Collectors.groupingBy(userId,
                Collectors.mapping(value, Collectors.toSet())));
    }


    @Transactional(readOnly = true)
    public String generateMatchingReason(
            Long requesterId,
            Long candidateId
    ) {
        Set<Long> ids = Set.of(requesterId, candidateId);

        // 취미 비교
        Map<Long, Set<HobbyType>> hobbies = group(
                hobbyRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(),
                UserHobby::getHobby);

        if (!Collections.disjoint(
                values(hobbies, requesterId),
                values(hobbies, candidateId)
        )) {
            return "같은 취미를 가졌어요";
        }

        // 관심사 비교
        Map<Long, Set<InterestType>> interests = group(
                interestRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(),
                UserInterest::getInterest);

        if (!Collections.disjoint(
                values(interests, requesterId),
                values(interests, candidateId)
        )) {
            return "관심사가 비슷해요";
        }

        // 선호 친구 유형 비교
        Map<Long, Set<FriendType>> friends = group(
                friendTypeRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(),
                UserFriendType::getFriendType);

        if (!Collections.disjoint(
                values(friends, requesterId),
                values(friends, candidateId)
        )) {
            return "원하는 친구 유형이 비슷해요";
        }

        // 사회적 성향 비교
        Map<Long, Set<SocialStyleType>> styles = group(
                socialStyleRepository.findAllByUser_IdIn(ids),
                v -> v.getUser().getId(),
                UserSocialStyle::getSocialStyle);

        if (!Collections.disjoint(
                values(styles, requesterId),
                values(styles, candidateId)
        )) {
            return "소통 스타일이 비슷해요";
        }

        return "새로운 인연으로 추천드려요";
    }

}
