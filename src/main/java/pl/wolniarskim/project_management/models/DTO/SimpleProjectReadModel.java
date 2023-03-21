package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.ProjectStatus;

import java.time.LocalDate;

@Getter
@Setter
public class SimpleProjectReadModel {
    private long id;
    private String name;
    private ProjectStatus status;
    private LocalDate startTime;
    private LocalDate endTime;
    private String description;
}
