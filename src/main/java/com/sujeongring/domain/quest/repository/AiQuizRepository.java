package com.sujeongring.domain.quest.repository;

import com.sujeongring.domain.quest.entity.AiQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiQuizRepository extends JpaRepository<AiQuiz, Long> {

    Optional<AiQuiz> findByRelationshipQuestId(Long relationshipQuestId);
}
