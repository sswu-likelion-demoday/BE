package com.sujeongring.global.config;

import com.sujeongring.domain.user.entity.*;
import com.sujeongring.domain.user.enums.*;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.user.repository.onboarding.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDummyDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSocialStyleRepository socialStyleRepository;
    private final UserInterestRepository interestRepository;
    private final UserHobbyRepository hobbyRepository;
    private final UserFriendTypeRepository friendTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(String... args) {
        transactionTemplate.executeWithoutResult(status -> {

            createUser(
                    1, MbtiType.ISTP,
                    List.of(SocialStyleType.SLOW_AND_DEEP),
                    List.of(InterestType.IT_TECH, InterestType.GAME, InterestType.TRAVEL),
                    List.of(HobbyType.GAME, HobbyType.TRAVEL, HobbyType.CAFE),
                    List.of(FriendType.SHARED_INTEREST, FriendType.EASY_TO_TALK)
            );

            createUser(
                    2, MbtiType.ISTP,
                    List.of(SocialStyleType.SLOW_AND_DEEP),
                    List.of(InterestType.IT_TECH, InterestType.GAME, InterestType.TRAVEL),
                    List.of(HobbyType.GAME, HobbyType.TRAVEL, HobbyType.CAFE),
                    List.of(FriendType.SHARED_INTEREST, FriendType.EASY_TO_TALK)
            );

            createUser(
                    3, MbtiType.ENFP,
                    List.of(SocialStyleType.FIRST_APPROACH),
                    List.of(InterestType.MUSIC_CONCERT, InterestType.FASHION_BEAUTY, InterestType.FOOD_CAFE),
                    List.of(HobbyType.MUSIC, HobbyType.PHOTOGRAPHY, HobbyType.RESTAURANT),
                    List.of(FriendType.FUN_TO_BE_WITH, FriendType.NEW_PERSPECTIVE)
            );

            createUser(
                    4, MbtiType.INTJ,
                    List.of(SocialStyleType.SLOW_AND_DEEP),
                    List.of(InterestType.IT_TECH, InterestType.BOOK_WRITING, InterestType.FINANCE),
                    List.of(HobbyType.READING, HobbyType.GAME, HobbyType.WALKING),
                    List.of(FriendType.SHARED_INTEREST, FriendType.LONG_TERM)
            );

            createUser(
                    5, MbtiType.ESFP,
                    List.of(SocialStyleType.ACTIVITY_BASED),
                    List.of(InterestType.SPORTS, InterestType.TRAVEL, InterestType.FOOD_CAFE),
                    List.of(HobbyType.RUNNING, HobbyType.TRAVEL, HobbyType.FITNESS),
                    List.of(FriendType.FUN_TO_BE_WITH, FriendType.MOTIVATING)
            );
        });
    }

    private void createUser(
            int number,
            MbtiType mbti,
            List<SocialStyleType> styles,
            List<InterestType> interests,
            List<HobbyType> hobbies,
            List<FriendType> friendTypes
    ) {
        String studentNumber = String.format("test%03d", number);

        // 이미 생성된 사용자는 건너뜀
        if (userRepository.existsByStudentNumber(studentNumber)) {
            return;
        }

        User user = new User(
                "테스트유저" + number,
                "테스트" + number,
                passwordEncoder.encode("Test1234!"),
                studentNumber,
                "AI융합학부"
        );

        user.completeOnboarding(mbti);
        userRepository.save(user);

        socialStyleRepository.saveAll(
                styles.stream()
                        .map(style -> new UserSocialStyle(user, style))
                        .toList()
        );

        interestRepository.saveAll(
                interests.stream()
                        .map(interest -> new UserInterest(user, interest))
                        .toList()
        );

        hobbyRepository.saveAll(
                hobbies.stream()
                        .map(hobby -> new UserHobby(user, hobby))
                        .toList()
        );

        friendTypeRepository.saveAll(
                friendTypes.stream()
                        .map(type -> new UserFriendType(user, type))
                        .toList()
        );
    }
}