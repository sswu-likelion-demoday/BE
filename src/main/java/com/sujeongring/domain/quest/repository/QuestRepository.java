package com.sujeongring.domain.quest.repository;

import com.sujeongring.domain.quest.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findAllByStageAndActiveTrueOrderByQuestOrderAsc(int stage);
}
