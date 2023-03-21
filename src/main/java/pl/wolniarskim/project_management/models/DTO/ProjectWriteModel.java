package pl.wolniarskim.project_management.models.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wolniarskim.project_management.models.ProjectStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProjectWriteModel {
    private String name;
    private ProjectStatus status;
    private LocalDate startTime;
    private LocalDate endTime;
    private String description;
}
