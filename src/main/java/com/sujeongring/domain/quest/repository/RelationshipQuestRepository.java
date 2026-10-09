package com.sujeongring.domain.quest.repository;

import com.sujeongring.domain.quest.entity.RelationshipQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelationshipQuestRepository
        extends JpaRepository<RelationshipQuest, Long> {

    @Query("""
            SELECT rq
            FROM RelationshipQuest rq
            JOIN FETCH rq.quest q
            WHERE rq.relationship.id = :relationshipId
              AND q.stage = :stage
            ORDER BY q.questOrder ASC
            """)
    List<RelationshipQuest> findByRelationshipIdAndStage(
            @Param("relationshipId") Long relationshipId,
            @Param("stage") int stage
    );
}
