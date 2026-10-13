package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository
        extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findAllByUser_IdOrderByIdAsc(Long userId);
}