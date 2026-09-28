package com.example.filmjury.dto;

import jakarta.validation.constraints.NotBlank;

public class EntryRequest {

    @NotBlank(message = "title cannot be empty")
    private String title;

    @NotBlank(message = "genre cannot be empty")
    private String genre;

    @NotBlank(message = "videoLink cannot be empty")
    private String videoLink;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getVideoLink() { return videoLink; }
    public void setVideoLink(String videoLink) { this.videoLink = videoLink; }
}