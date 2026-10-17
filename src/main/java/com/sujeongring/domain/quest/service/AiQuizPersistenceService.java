package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.quest.entity.AiQuiz;
import com.sujeongring.domain.quest.repository.AiQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiQuizPersistenceService {

    private final AiQuizRepository aiQuizRepository;

    @Transactional
    public AiQuiz saveGeneratedQuiz(AiQuiz aiQuiz) {
        return aiQuizRepository.save(aiQuiz);
    }
}
