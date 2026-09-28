package com.example.filmjury.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.filmjury.dto.ScoreCardRequest;
import com.example.filmjury.dto.ScoreResponse;
import com.example.filmjury.entity.Entry;
import com.example.filmjury.entity.Judge;
import com.example.filmjury.entity.ScoreCard;
import com.example.filmjury.exception.BusinessRuleException;
import com.example.filmjury.exception.DuplicateScoreCardException;
import com.example.filmjury.repository.CriterionRepository;
import com.example.filmjury.repository.EntryRepository;
import com.example.filmjury.repository.JudgeRepository;
import com.example.filmjury.repository.ScoreCardRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoreCardServiceTest {

    private static final Map<String, Integer> SCORES = Map.of(
            "Story", 8, "Direction", 9, "Acting", 7, "Cinematography", 8);

    @Mock private EntryRepository entryRepository;
    @Mock private JudgeRepository judgeRepository;
    @Mock private CriterionRepository criterionRepository;
    @Mock private ScoreCardRepository scoreCardRepository;

    private ScoreCardService service;
    private Entry entry;
    private Judge judge;

    @BeforeEach
    void setUp() {
        service = new ScoreCardService(entryRepository, judgeRepository, criterionRepository, scoreCardRepository);
        entry = new Entry("The Last Hope", "Drama", "https://example.com/video");
        entry.setId(1L);
        judge = new Judge("Judge One", "judge1@gmail.com");
        judge.setId(2L);
    }

    @Test
    void rejectsAnUnassignedJudge() {
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(judgeRepository.findById(2L)).thenReturn(Optional.of(judge));

        assertThrows(BusinessRuleException.class, () -> service.submit(1L, 2L, request()));
        verify(scoreCardRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsASecondScorecardFromTheSameJudge() {
        entry.getJudges().add(judge);
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(judgeRepository.findById(2L)).thenReturn(Optional.of(judge));
        when(scoreCardRepository.existsByEntry_IdAndJudge_Id(1L, 2L)).thenReturn(true);

        assertThrows(DuplicateScoreCardException.class, () -> service.submit(1L, 2L, request()));
        verify(scoreCardRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

        @Test
        void savesOneScorecardForAnAssignedJudge() {
        entry.getJudges().add(judge);
            List<ScoreCard> savedCards = new ArrayList<>();
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(judgeRepository.findById(2L)).thenReturn(Optional.of(judge));
        when(criterionRepository.findByNameIn(SCORES.keySet())).thenReturn(List.of(
            new com.example.filmjury.entity.Criterion("Story"),
            new com.example.filmjury.entity.Criterion("Direction"),
            new com.example.filmjury.entity.Criterion("Acting"),
            new com.example.filmjury.entity.Criterion("Cinematography")));
        when(scoreCardRepository.save(any(ScoreCard.class))).thenAnswer(invocation -> {
            ScoreCard scoreCard = invocation.getArgument(0);
            savedCards.add(scoreCard);
            return scoreCard;
        });
        when(scoreCardRepository.findAllByEntry_Id(1L)).thenAnswer(invocation -> savedCards);

        ScoreResponse response = service.submit(1L, 2L, request());

        assertEquals(1L, response.getEntryId());
        assertEquals(8.0, response.getAverageScore());
        assertNotNull(response.getTitle());
        verify(scoreCardRepository).save(org.mockito.ArgumentMatchers.argThat(card ->
            card.getEntry() == entry && card.getJudge() == judge && card.getScores().equals(SCORES)));
        }

    @Test
    void averagesSubmittedJudgeScorecardsAndHandlesNoScores() {
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        ScoreCard first = card(entry, Map.of("Story", 8, "Direction", 9, "Acting", 7, "Cinematography", 8));
        ScoreCard second = card(entry, Map.of("Story", 9, "Direction", 9, "Acting", 9, "Cinematography", 9));
        when(scoreCardRepository.findAllByEntry_Id(1L)).thenReturn(List.of(first, second));

        ScoreResponse response = service.getEntryScore(1L);

        assertEquals(8.5, response.getAverageScore());
        when(scoreCardRepository.findAllByEntry_Id(1L)).thenReturn(List.of());
        assertEquals(null, service.getEntryScore(1L).getAverageScore());
    }

    private ScoreCardRequest request() {
        ScoreCardRequest request = new ScoreCardRequest();
        request.setScores(SCORES);
        return request;
    }

    private ScoreCard card(Entry scoreEntry, Map<String, Integer> scores) {
        ScoreCard card = new ScoreCard();
        card.setEntry(scoreEntry);
        card.setScores(scores);
        return card;
    }
}