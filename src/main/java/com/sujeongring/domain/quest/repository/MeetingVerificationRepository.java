package com.sujeongring.domain.quest.repository;

import com.sujeongring.domain.quest.entity.MeetingVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingVerificationRepository extends JpaRepository<MeetingVerification, Long> {

    Optional<MeetingVerification> findByRelationshipQuestIdAndUserId(
            Long relationshipQuestId,
            Long userId
    );

    boolean existsByRelationshipQuestIdAndUserId(
            Long relationshipQuestId,
            Long userId
    );
}
