package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByStudentId(String studentId);

    Optional<User> findByStudentId(String studentId);
}