
package com.sujeongring.domain.relationship.repository;

import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRepository
        extends JpaRepository<Relationship, Long> {

    // 사용자가 포함된 모든 관계 조회
    List<Relationship> findAllByUserAIdOrUserBId(
            Long userAId,
            Long userBId
    );

    // 사용자의 특정 상태 관계 개수 조회
    long countByUserAIdAndStatusOrUserBIdAndStatus(
            Long userAId,
            RelationshipStatus userAStatus,
            Long userBId,
            RelationshipStatus userBStatus
    );

    // 사용자가 포함된 ACTIVE 관계 조회
    List<Relationship> findAllByStatusAndUserAIdOrStatusAndUserBId(
            RelationshipStatus statusA,
            Long userAId,
            RelationshipStatus statusB,
            Long userBId
    );
}
