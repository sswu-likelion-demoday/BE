package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByStudentId(String studentId);
}

