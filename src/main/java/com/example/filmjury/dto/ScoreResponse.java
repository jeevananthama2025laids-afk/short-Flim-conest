package com.example.filmjury.dto;

public class ScoreResponse {

    private final Long entryId;
    private final String title;
    private final Double averageScore;

    public ScoreResponse(Long entryId, String title, Double averageScore) {
        this.entryId = entryId;
        this.title = title;
        this.averageScore = averageScore;
    }

    public Long getEntryId() { return entryId; }
    public String getTitle() { return title; }
    public Double getAverageScore() { return averageScore; }
}