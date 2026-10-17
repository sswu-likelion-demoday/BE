package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdForUpdate(
            @Param("userId") Long userId
    );

    boolean existsByStudentNumber(String studentNumber);

    Optional<User> findByStudentNumber(String studentNumber);

    // 자동 매칭 가능한 사용자 조회
    @Query("""
        SELECT u
        FROM User u
        WHERE u.id <> :userId
          AND u.onboardingCompleted = true
          AND u.status = :status

          AND NOT EXISTS (
              SELECT b.id
              FROM Block b
              WHERE
                  (b.blocker.id = :userId
                   AND b.blocked.id = u.id)
                  OR
                  (b.blocker.id = u.id
                   AND b.blocked.id = :userId)
          )

          AND NOT EXISTS (
              SELECT mr.id
              FROM MatchRecommendation mr
              WHERE mr.user.id = :userId
                AND mr.candidate.id = u.id
          )
        """)
    List<User> findEligibleMatchingCandidates(
            @Param("userId") Long userId,
            @Param("status") UserStatus status
    );
}
