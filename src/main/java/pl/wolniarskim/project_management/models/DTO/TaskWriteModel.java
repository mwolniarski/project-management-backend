package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.TaskPriority;
import pl.wolniarskim.project_management.models.TaskStatus;

import java.time.LocalDate;

@Getter
@Setter
public class TaskWriteModel {
    private String name;
    private TaskStatus status;
    private TaskPriority priority;
    private String description;
    private LocalDate dueDate;
    private double estimatedWorkTime;
    private OwnerWriteModel owner;
}
