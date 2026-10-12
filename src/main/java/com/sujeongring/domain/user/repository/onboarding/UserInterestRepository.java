package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInterestRepository
        extends JpaRepository<UserInterest, Long> {
}