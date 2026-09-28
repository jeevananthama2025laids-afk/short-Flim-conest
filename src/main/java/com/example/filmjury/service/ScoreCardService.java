package com.example.filmjury.service;

import com.example.filmjury.dto.ScoreCardRequest;
import com.example.filmjury.dto.ScoreResponse;
import com.example.filmjury.entity.Criterion;
import com.example.filmjury.entity.Entry;
import com.example.filmjury.entity.Judge;
import com.example.filmjury.entity.ScoreCard;
import com.example.filmjury.exception.BusinessRuleException;
import com.example.filmjury.exception.DuplicateScoreCardException;
import com.example.filmjury.exception.ResourceNotFoundException;
import com.example.filmjury.repository.CriterionRepository;
import com.example.filmjury.repository.EntryRepository;
import com.example.filmjury.repository.JudgeRepository;
import com.example.filmjury.repository.ScoreCardRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreCardService {

    private static final Set<String> REQUIRED_CRITERIA =
            Set.of("Story", "Direction", "Acting", "Cinematography");

    private final EntryRepository entryRepository;
    private final JudgeRepository judgeRepository;
    private final CriterionRepository criterionRepository;
    private final ScoreCardRepository scoreCardRepository;

    public ScoreCardService(EntryRepository entryRepository, JudgeRepository judgeRepository,
            CriterionRepository criterionRepository, ScoreCardRepository scoreCardRepository) {
        this.entryRepository = entryRepository;
        this.judgeRepository = judgeRepository;
        this.criterionRepository = criterionRepository;
        this.scoreCardRepository = scoreCardRepository;
    }

    @Transactional
    public ScoreResponse submit(Long entryId, Long judgeId, ScoreCardRequest request) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id: " + entryId));
        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found with id: " + judgeId));

        if (!entry.getJudges().contains(judge)) {
            throw new BusinessRuleException("Judge has not been assigned to this entry.");
        }
        if (scoreCardRepository.existsByEntry_IdAndJudge_Id(entryId, judgeId)) {
            throw new DuplicateScoreCardException("Judge has already submitted a scorecard for this entry.");
        }

        Map<String, Integer> scores = request.getScores();
        if (!scores.keySet().equals(REQUIRED_CRITERIA)) {
            throw new BusinessRuleException("Scores must include Story, Direction, Acting, and Cinematography only.");
        }
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            if (score.getValue() == null || score.getValue() < 1 || score.getValue() > 10) {
                throw new BusinessRuleException("Each score must be between 1 and 10.");
            }
        }
        List<String> existingCriteria = criterionRepository.findByNameIn(scores.keySet()).stream()
                .map(Criterion::getName).toList();
        if (!Set.copyOf(existingCriteria).equals(REQUIRED_CRITERIA)) {
            throw new ResourceNotFoundException("One or more required scoring criteria are missing.");
        }

        ScoreCard scoreCard = new ScoreCard();
        scoreCard.setEntry(entry);
        scoreCard.setJudge(judge);
        scoreCard.setScores(scores);
        scoreCardRepository.save(scoreCard);
        return getEntryScore(entryId);
    }

    @Transactional(readOnly = true)
    public ScoreResponse getEntryScore(Long entryId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id: " + entryId));
        List<ScoreCard> scoreCards = scoreCardRepository.findAllByEntry_Id(entryId);
        Double average = scoreCards.isEmpty() ? null
                : scoreCards.stream().mapToDouble(ScoreCard::getAverageScore).average().orElseThrow();
        return new ScoreResponse(entry.getId(), entry.getTitle(), average);
    }
}