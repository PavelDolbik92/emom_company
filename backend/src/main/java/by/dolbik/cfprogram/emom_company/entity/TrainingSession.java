package by.dolbik.cfprogram.emom_company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "TrainingSession")
@Table(name = "T_TRAINING_SESSION")
@Builder
public class TrainingSession {
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "SESSION_DATE")
    private LocalDate sessionDate;

    @Column(name = "DATA")
    private String sessionData;
}
