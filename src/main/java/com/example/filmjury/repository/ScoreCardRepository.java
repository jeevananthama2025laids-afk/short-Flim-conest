package com.example.filmjury.repository;

import com.example.filmjury.entity.ScoreCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreCardRepository extends JpaRepository<ScoreCard, Long> {
    boolean existsByEntry_IdAndJudge_Id(Long entryId, Long judgeId);
    List<ScoreCard> findAllByEntry_Id(Long entryId);
}