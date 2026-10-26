package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserHobby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserHobbyRepository
        extends JpaRepository<UserHobby, Long> {

    // 기존 Relationship, AI Quiz 기능
    List<UserHobby> findAllByUser_IdOrderByIdAsc(Long userId);

    // 신규 일반 매칭 기능
    List<UserHobby> findAllByUser_IdIn(
            Collection<Long> userIds
    );
}