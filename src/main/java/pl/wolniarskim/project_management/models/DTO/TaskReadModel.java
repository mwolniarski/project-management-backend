package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.TaskPriority;
import pl.wolniarskim.project_management.models.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TaskReadModel {
    private long id;
    private String name;
    private TaskStatus status;
    private TaskPriority priority;
    private String description;
    private LocalDate dueDate;
    private UserReadModel owner;
    private double estimatedWorkTime;
    private List<UserReadModel> watchers = new ArrayList<>();
}
