package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserFriendType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFriendTypeRepository
        extends JpaRepository<UserFriendType, Long> {
}