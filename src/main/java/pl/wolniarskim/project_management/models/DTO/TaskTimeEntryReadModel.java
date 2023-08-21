package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTimeEntryReadModel {
    private long id;
    private double hoursSpent;
    private String description;
}
