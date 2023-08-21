package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTimeEntryWriteModel {
    private double hoursSpent;
    private String description;
}
