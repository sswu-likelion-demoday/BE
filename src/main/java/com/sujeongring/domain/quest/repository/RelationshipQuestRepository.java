package com.sujeongring.domain.quest.repository;

import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelationshipQuestRepository
        extends JpaRepository<RelationshipQuest, Long> {

    List<RelationshipQuest>
    findAllByRelationshipIdOrderByQuestStageAscQuestQuestOrderAsc(
            Long relationshipId
    );

    Optional<RelationshipQuest>
    findByRelationshipIdAndStatus(
            Long relationshipId,
            RelationshipQuestStatus status
    );
}
