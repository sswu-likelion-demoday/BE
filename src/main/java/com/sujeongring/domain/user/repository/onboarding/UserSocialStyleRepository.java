package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserSocialStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserSocialStyleRepository
        extends JpaRepository<UserSocialStyle, Long> {

    List<UserSocialStyle> findAllByUser_IdOrderByIdAsc(Long userId);

    List<UserSocialStyle> findAllByUser_IdIn(
            Collection<Long> userIds
    );
}