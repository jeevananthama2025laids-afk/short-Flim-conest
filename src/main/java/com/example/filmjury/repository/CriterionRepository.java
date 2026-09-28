package com.example.filmjury.repository;

import com.example.filmjury.entity.Criterion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterionRepository extends JpaRepository<Criterion, Long> {
    boolean existsByName(String name);
    List<Criterion> findByNameIn(Collection<String> names);
}