
package com.sujeongring.domain.relationship.repository;

import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query("""
            select count(r) > 0
            from Relationship r
            where r.status = :status
              and ((r.userA.id = :userAId and r.userB.id = :userBId)
                or (r.userA.id = :userBId and r.userB.id = :userAId))
            """)
    boolean existsActiveRelationshipBetween(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId,
            @Param("status") RelationshipStatus status
    );

    @Query("""
            select r
            from Relationship r
            where ((r.userA.id = :userAId and r.userB.id = :userBId)
                or (r.userA.id = :userBId and r.userB.id = :userAId))
              and r.status in :statuses
            """)
    List<Relationship> findPairRelationshipsByStatuses(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId,
            @Param("statuses") Collection<RelationshipStatus> statuses
    );
}
