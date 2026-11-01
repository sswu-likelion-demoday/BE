package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRequestRepository
        extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            MatchRequestStatus status
    );

    List<MatchRequest> findAllBySenderIdAndStatusOrderByCreatedAtDesc(
            Long senderId,
            MatchRequestStatus status
    );

    boolean existsByMatchRecommendationId(Long recommendationId);

    @Query("""
            select mr
            from MatchRequest mr
            where mr.status = :status
              and ((mr.sender.id = :userAId and mr.receiver.id = :userBId)
                or (mr.sender.id = :userBId and mr.receiver.id = :userAId))
            order by mr.createdAt desc
            """)
    List<MatchRequest> findPairRequestsByStatus(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId,
            @Param("status") MatchRequestStatus status
    );


    @Query("""
    SELECT mr
    FROM MatchRequest mr
    JOIN FETCH mr.sender
    WHERE mr.receiver.id = :userId
      AND mr.status = :pending
      AND mr.expiresAt > :now
    ORDER BY mr.createdAt DESC
    """)
    List<MatchRequest> findVisibleReceivedRequests(
            @Param("userId") Long userId,
            @Param("pending") MatchRequestStatus pending,
            @Param("now") LocalDateTime now
    );

    @Query("""
    SELECT mr
    FROM MatchRequest mr
    JOIN FETCH mr.receiver
    WHERE mr.sender.id = :userId
      AND (
          (mr.status = :pending AND mr.expiresAt > :now)
          OR
          (mr.status = :accepted
           AND mr.senderConfirmedAt IS NULL
           AND mr.respondedAt > :acceptedCutoff)
      )
    ORDER BY mr.createdAt DESC
    """)
    List<MatchRequest> findVisibleSentRequests(
            @Param("userId") Long userId,
            @Param("pending") MatchRequestStatus pending,
            @Param("accepted") MatchRequestStatus accepted,
            @Param("now") LocalDateTime now,
            @Param("acceptedCutoff") LocalDateTime acceptedCutoff
    );
}
