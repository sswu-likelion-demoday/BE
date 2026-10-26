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
}
