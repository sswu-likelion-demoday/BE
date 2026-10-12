package com.sujeongring.domain.user.repository.onboarding;


import com.sujeongring.domain.user.entity.UserSocialStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialStyleRepository
        extends JpaRepository<UserSocialStyle, Long> {
}