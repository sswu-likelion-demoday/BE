package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.matching.dto.response.MatchingHomeResponse;
import com.sujeongring.domain.relationship.service.RelationshipSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingHomeService {

    private final RelationshipSlotService relationshipSlotService;

    @Transactional(readOnly = true)
    public MatchingHomeResponse getHome(Long userId) {

        long activeCount = relationshipSlotService.getActiveRelationshipCount(userId);

        int used = Math.toIntExact(activeCount);
        int available = Math.max(
                0,
                RelationshipSlotService.MAX_ACTIVE_RELATIONSHIPS - used
        );

        return new MatchingHomeResponse(
                new MatchingHomeResponse.RelationshipSlots(
                        used,
                        available,
                        RelationshipSlotService.MAX_ACTIVE_RELATIONSHIPS
                )
        );
    }
}
