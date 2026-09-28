package com.example.filmjury.controller;

import com.example.filmjury.dto.JudgeRequest;
import com.example.filmjury.entity.Judge;
import com.example.filmjury.service.JudgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/judges")
public class JudgeController {

    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) { this.judgeService = judgeService; }

    @PostMapping
    public ResponseEntity<Judge> create(@Valid @RequestBody JudgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(judgeService.create(request));
    }

    @GetMapping
    public List<Judge> findAll() { return judgeService.findAll(); }

    @GetMapping("/{id}")
    public Judge findById(@PathVariable Long id) { return judgeService.findById(id); }
}