package com.example.filmjury.controller;

import com.example.filmjury.dto.ScoreCardRequest;
import com.example.filmjury.dto.ScoreResponse;
import com.example.filmjury.service.ScoreCardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entries/{entryId}")
public class ScoreCardController {

    private final ScoreCardService scoreCardService;

    public ScoreCardController(ScoreCardService scoreCardService) { this.scoreCardService = scoreCardService; }

    @PostMapping("/judges/{judgeId}/score")
    public ScoreResponse submit(@PathVariable Long entryId, @PathVariable Long judgeId,
            @Valid @RequestBody ScoreCardRequest request) {
        return scoreCardService.submit(entryId, judgeId, request);
    }

    @GetMapping("/score")
    public ScoreResponse getEntryScore(@PathVariable Long entryId) {
        return scoreCardService.getEntryScore(entryId);
    }
}