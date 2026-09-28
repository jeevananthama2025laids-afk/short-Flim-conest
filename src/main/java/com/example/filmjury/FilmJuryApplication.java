package com.example.filmjury;

import com.example.filmjury.entity.Criterion;
import com.example.filmjury.repository.CriterionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FilmJuryApplication {

    private static final String[] CRITERIA = {"Story", "Direction", "Acting", "Cinematography"};

    public static void main(String[] args) {
        SpringApplication.run(FilmJuryApplication.class, args);
    }

    @Bean
    CommandLineRunner seedCriteria(CriterionRepository criterionRepository) {
        return args -> {
            for (String name : CRITERIA) {
                if (!criterionRepository.existsByName(name)) {
                    criterionRepository.save(new Criterion(name));
                }
            }
        };
    }
}