package com.sujeongring.domain.user.repository.onboarding;

import com.sujeongring.domain.user.entity.UserFriendType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFriendTypeRepository
        extends JpaRepository<UserFriendType, Long> {

    List<UserFriendType> findAllByUser_IdOrderByIdAsc(Long userId);
}