package com.example.filmjury.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class ScoreCardRequest {

    @NotNull(message = "scores are required")
    @NotEmpty(message = "scores cannot be empty")
    private Map<String, @NotNull @Min(1) @Max(10) Integer> scores;

    public Map<String, Integer> getScores() { return scores; }
    public void setScores(Map<String, Integer> scores) { this.scores = scores; }
}