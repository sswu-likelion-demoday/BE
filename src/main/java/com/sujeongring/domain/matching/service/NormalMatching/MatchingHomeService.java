package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.matching.dto.response.MatchingHomeResponse;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingHomeService {

    public static final int TOTAL_SLOTS = 3;

    private final RelationshipRepository relationshipRepository;

    @Transactional(readOnly = true)
    public MatchingHomeResponse getHome(Long userId) {

        long activeCount =
                relationshipRepository.countByUserAIdAndStatusOrUserBIdAndStatus(
                        userId,
                        RelationshipStatus.ACTIVE,
                        userId,
                        RelationshipStatus.ACTIVE
                );

        int used = Math.toIntExact(activeCount);
        int available = Math.max(0, TOTAL_SLOTS - used);

        return new MatchingHomeResponse(
                new MatchingHomeResponse.RelationshipSlots(
                        used,
                        available,
                        TOTAL_SLOTS
                )
        );
    }
}
