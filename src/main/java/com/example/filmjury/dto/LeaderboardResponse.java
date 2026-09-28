package com.example.filmjury.dto;

public class LeaderboardResponse {

    private final int rank;
    private final Long entryId;
    private final String title;
    private final String genre;
    private final double averageScore;

    public LeaderboardResponse(int rank, Long entryId, String title, String genre, double averageScore) {
        this.rank = rank;
        this.entryId = entryId;
        this.title = title;
        this.genre = genre;
        this.averageScore = averageScore;
    }

    public int getRank() { return rank; }
    public Long getEntryId() { return entryId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public double getAverageScore() { return averageScore; }
}