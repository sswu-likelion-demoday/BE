package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserInterestRepository
        extends JpaRepository<UserInterest, Long> {

    // 기존 Relationship, AI Quiz 기능
    List<UserInterest> findAllByUser_IdOrderByIdAsc(Long userId);

    // 신규 일반 매칭 기능
    List<UserInterest> findAllByUser_IdIn(
            Collection<Long> userIds
    );
}