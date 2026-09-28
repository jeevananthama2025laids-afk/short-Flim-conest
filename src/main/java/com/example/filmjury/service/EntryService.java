package com.example.filmjury.service;

import com.example.filmjury.dto.EntryRequest;
import com.example.filmjury.entity.Entry;
import com.example.filmjury.entity.Judge;
import com.example.filmjury.exception.ResourceNotFoundException;
import com.example.filmjury.repository.EntryRepository;
import com.example.filmjury.repository.JudgeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryService {

    private final EntryRepository entryRepository;
    private final JudgeRepository judgeRepository;

    public EntryService(EntryRepository entryRepository, JudgeRepository judgeRepository) {
        this.entryRepository = entryRepository;
        this.judgeRepository = judgeRepository;
    }

    public Entry create(EntryRequest request) {
        return entryRepository.save(new Entry(request.getTitle(), request.getGenre(), request.getVideoLink()));
    }

    public List<Entry> findAll() { return entryRepository.findAll(); }

    public Entry findById(Long id) {
        return entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id: " + id));
    }

    @Transactional
    public Entry update(Long id, EntryRequest request) {
        Entry entry = findById(id);
        entry.setTitle(request.getTitle());
        entry.setGenre(request.getGenre());
        entry.setVideoLink(request.getVideoLink());
        return entryRepository.save(entry);
    }

    @Transactional
    public void delete(Long id) {
        entryRepository.delete(findById(id));
    }

    @Transactional
    public void assignJudge(Long entryId, Long judgeId) {
        Entry entry = findById(entryId);
        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found with id: " + judgeId));
        entry.getJudges().add(judge);
        entryRepository.save(entry);
    }
}