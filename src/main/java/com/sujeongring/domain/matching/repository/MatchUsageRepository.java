package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.entity.MatchUsageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MatchUsageRepository
        extends JpaRepository<MatchUsage, Long> {

    long countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqual(
            Long userId,
            MatchUsageType usageType,
            LocalDateTime startDateTime
    );
}
