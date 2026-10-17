
package com.sujeongring.domain.matching.service;

import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.entity.UserFriendType;
import com.sujeongring.domain.user.entity.UserHobby;
import com.sujeongring.domain.user.entity.UserInterest;
import com.sujeongring.domain.user.entity.UserSocialStyle;
import com.sujeongring.domain.user.repository.onboarding.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingScoreService {

    private final UserInterestRepository interestRepository;
    private final UserHobbyRepository hobbyRepository;
    private final UserFriendTypeRepository friendTypeRepository;
    private final UserSocialStyleRepository socialStyleRepository;

    public double calculateScore(User user, User candidate) {

        double interestScore = calculateJaccard(
                interestRepository.findAllByUser_IdOrderByIdAsc(user.getId()),
                interestRepository.findAllByUser_IdOrderByIdAsc(candidate.getId()),
                UserInterest::getInterest
        );

        double hobbyScore = calculateJaccard(
                hobbyRepository.findAllByUser_IdOrderByIdAsc(user.getId()),
                hobbyRepository.findAllByUser_IdOrderByIdAsc(candidate.getId()),
                UserHobby::getHobby
        );

        double friendScore = calculateJaccard(
                friendTypeRepository.findAllByUser_IdOrderByIdAsc(user.getId()),
                friendTypeRepository.findAllByUser_IdOrderByIdAsc(candidate.getId()),
                UserFriendType::getFriendType
        );

        double socialScore = calculateJaccard(
                socialStyleRepository.findAllByUser_IdOrderByIdAsc(user.getId()),
                socialStyleRepository.findAllByUser_IdOrderByIdAsc(candidate.getId()),
                UserSocialStyle::getSocialStyle
        );

        double mbtiScore = user.getMbti() != null
                && user.getMbti() == candidate.getMbti()
                ? 1.0 : 0.0;

        return (interestScore * 0.30
                + hobbyScore * 0.25
                + friendScore * 0.20
                + socialScore * 0.15
                + mbtiScore * 0.10) * 100;
    }

    private <T, E> double calculateJaccard(
            List<T> first,
            List<T> second,
            Function<T, E> extractor
    ) {
        Set<E> firstSet = first.stream()
                .map(extractor)
                .collect(Collectors.toSet());

        Set<E> secondSet = second.stream()
                .map(extractor)
                .collect(Collectors.toSet());

        if (firstSet.isEmpty() || secondSet.isEmpty()) {
            return 0.0;
        }

        Set<E> intersection = new HashSet<>(firstSet);
        intersection.retainAll(secondSet);

        Set<E> union = new HashSet<>(firstSet);
        union.addAll(secondSet);

        return (double) intersection.size() / union.size();
    }
}
