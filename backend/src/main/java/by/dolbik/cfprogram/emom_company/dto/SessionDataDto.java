package by.dolbik.cfprogram.emom_company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SessionDataDto {
    @JsonProperty("saved_workout")
    private SavedWorkout savedWorkout;

    @Data
    public static class SavedWorkout {
        private String instruction;
        private List<WorkoutSet> workoutSets;
    }

    @Data
    public static class WorkoutSet {
        private String title;
        private String instruction;
    }
}
