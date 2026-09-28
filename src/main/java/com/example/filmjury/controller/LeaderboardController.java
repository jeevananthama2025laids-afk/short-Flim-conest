package com.example.filmjury.controller;

import com.example.filmjury.dto.LeaderboardResponse;
import com.example.filmjury.service.LeaderboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) { this.leaderboardService = leaderboardService; }

    @GetMapping
    public List<LeaderboardResponse> getLeaderboard() { return leaderboardService.getLeaderboard(); }
}