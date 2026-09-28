package com.example.filmjury.repository;

import com.example.filmjury.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JudgeRepository extends JpaRepository<Judge, Long> {
}