package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserHobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHobbyRepository
        extends JpaRepository<UserHobby, Long> {
}