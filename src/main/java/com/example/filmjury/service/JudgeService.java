package com.example.filmjury.service;

import com.example.filmjury.dto.JudgeRequest;
import com.example.filmjury.entity.Judge;
import com.example.filmjury.exception.ResourceNotFoundException;
import com.example.filmjury.repository.JudgeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    private final JudgeRepository judgeRepository;

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public Judge create(JudgeRequest request) {
        return judgeRepository.save(new Judge(request.getName(), request.getEmail()));
    }

    public List<Judge> findAll() { return judgeRepository.findAll(); }

    public Judge findById(Long id) {
        return judgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found with id: " + id));
    }
}