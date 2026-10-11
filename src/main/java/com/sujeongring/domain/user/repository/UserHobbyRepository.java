package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.UserHobby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHobbyRepository
        extends JpaRepository<UserHobby, Long> {

    List<UserHobby> findAllByUser_IdOrderByIdAsc(Long userId);
}
