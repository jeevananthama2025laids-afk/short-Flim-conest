package com.example.filmjury.controller;

import com.example.filmjury.dto.EntryRequest;
import com.example.filmjury.entity.Entry;
import com.example.filmjury.service.EntryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private final EntryService entryService;

    public EntryController(EntryService entryService) { this.entryService = entryService; }

    @PostMapping
    public ResponseEntity<Entry> create(@Valid @RequestBody EntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entryService.create(request));
    }

    @GetMapping
    public List<Entry> findAll() { return entryService.findAll(); }

    @GetMapping("/{id}")
    public Entry findById(@PathVariable Long id) { return entryService.findById(id); }

    @PutMapping("/{id}")
    public Entry update(@PathVariable Long id, @Valid @RequestBody EntryRequest request) {
        return entryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        entryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{entryId}/judges/{judgeId}")
    public Map<String, String> assignJudge(@PathVariable Long entryId, @PathVariable Long judgeId) {
        entryService.assignJudge(entryId, judgeId);
        return Map.of("message", "Judge assigned to entry.");
    }
}