package com.sujeongring.domain.relationship.repository;

import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.entity.RelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRepository
        extends JpaRepository<Relationship, Long> {

    List<Relationship> findAllByUserAIdOrUserBId(
            Long userAId,
            Long userBId
    );

    long countByUserAIdAndStatusOrUserBIdAndStatus(
            Long userAId,
            RelationshipStatus userAStatus,
            Long userBId,
            RelationshipStatus userBStatus
    );
}
