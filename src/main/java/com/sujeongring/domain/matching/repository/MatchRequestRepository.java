package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
