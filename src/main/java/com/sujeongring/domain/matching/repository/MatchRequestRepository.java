package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.entity.MatchRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
