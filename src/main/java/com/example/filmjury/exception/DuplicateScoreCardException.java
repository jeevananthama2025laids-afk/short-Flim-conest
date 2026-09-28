package com.example.filmjury.exception;

public class DuplicateScoreCardException extends RuntimeException {
    public DuplicateScoreCardException(String message) { super(message); }
}