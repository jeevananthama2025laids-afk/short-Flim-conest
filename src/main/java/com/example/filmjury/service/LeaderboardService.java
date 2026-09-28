package com.example.filmjury.service;

import com.example.filmjury.dto.LeaderboardResponse;
import com.example.filmjury.entity.Entry;
import com.example.filmjury.entity.ScoreCard;
import com.example.filmjury.repository.ScoreCardRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {

    private final ScoreCardRepository scoreCardRepository;

    public LeaderboardService(ScoreCardRepository scoreCardRepository) {
        this.scoreCardRepository = scoreCardRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard() {
        Map<Long, List<ScoreCard>> cardsByEntry = scoreCardRepository.findAll().stream()
                .collect(Collectors.groupingBy(card -> card.getEntry().getId()));

        List<LeaderboardResponse> ranked = cardsByEntry.values().stream()
                .map(cards -> {
                    Entry entry = cards.get(0).getEntry();
                    double average = cards.stream().mapToDouble(ScoreCard::getAverageScore).average().orElseThrow();
                    return new LeaderboardResponse(0, entry.getId(), entry.getTitle(), entry.getGenre(), average);
                })
                .sorted(Comparator.comparingDouble(LeaderboardResponse::getAverageScore).reversed()
                        .thenComparing(LeaderboardResponse::getEntryId))
                .toList();

        return java.util.stream.IntStream.range(0, ranked.size())
                .mapToObj(index -> {
                    LeaderboardResponse row = ranked.get(index);
                    return new LeaderboardResponse(index + 1, row.getEntryId(), row.getTitle(),
                            row.getGenre(), row.getAverageScore());
                })
                .toList();
    }
}