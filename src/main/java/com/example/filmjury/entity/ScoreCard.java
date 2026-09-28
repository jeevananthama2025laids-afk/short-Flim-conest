package com.example.filmjury.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "score_cards", uniqueConstraints =
        @UniqueConstraint(name = "uk_score_card_entry_judge", columnNames = {"entry_id", "judge_id"}))
public class ScoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "judge_id", nullable = false)
    private Judge judge;

    @ElementCollection
    @CollectionTable(name = "score_card_scores", joinColumns = @JoinColumn(name = "score_card_id"))
    @MapKeyColumn(name = "criterion_name")
    @Column(name = "score", nullable = false)
    private Map<String, @Min(1) @Max(10) Integer> scores = new HashMap<>();

    public Long getId() { return id; }
    public Entry getEntry() { return entry; }
    public void setEntry(Entry entry) { this.entry = entry; }
    public Judge getJudge() { return judge; }
    public void setJudge(Judge judge) { this.judge = judge; }
    public Map<String, Integer> getScores() { return scores; }
    public void setScores(Map<String, Integer> scores) { this.scores = new HashMap<>(scores); }

    public double getAverageScore() {
        return scores.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}