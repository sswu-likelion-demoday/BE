package com.sujeongring.domain.relationship.service;

import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.exception.RelationshipErrorCode;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationshipSlotService {

    public static final int MAX_ACTIVE_RELATIONSHIPS = 3;

    private final RelationshipRepository relationshipRepository;

    public long getActiveRelationshipCount(Long userId) {
        return relationshipRepository.countByUserAIdAndStatusOrUserBIdAndStatus(
                userId,
                RelationshipStatus.ACTIVE,
                userId,
                RelationshipStatus.ACTIVE
        );
    }

    public boolean hasAvailableSlot(Long userId) {
        return getActiveRelationshipCount(userId) < MAX_ACTIVE_RELATIONSHIPS;
    }

    public void validateAvailableSlot(Long userId) {
        if (!hasAvailableSlot(userId)) {
            throw new BaseException(RelationshipErrorCode.MATCH_SLOT_FULL);
        }
    }

    public boolean existsActiveRelationshipBetween(Long userAId, Long userBId) {
        if (userAId.equals(userBId)) {
            return false;
        }

        return relationshipRepository.existsActiveRelationshipBetween(
                userAId,
                userBId,
                RelationshipStatus.ACTIVE
        );
    }
}
