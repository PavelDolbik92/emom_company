package by.dolbik.cfprogram.emom_company.repository;

import by.dolbik.cfprogram.emom_company.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TrainingSessionRep extends JpaRepository<TrainingSession, Long> {
    TrainingSession findFirstBySessionDate(LocalDate date);
}
