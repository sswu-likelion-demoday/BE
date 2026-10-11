package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.service.QuestCompletionResult;
import com.sujeongring.domain.quest.service.QuestProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/quests")
@RequiredArgsConstructor
@Profile("local")
public class QuestTestController {

    private final QuestProgressService questProgressService;

    @PostMapping("/{relationshipQuestId}/complete")
    public QuestCompletionResult completeQuest(
            @PathVariable Long relationshipQuestId
    ) {
        return questProgressService.completeQuest(
                relationshipQuestId
        );
    }
}
