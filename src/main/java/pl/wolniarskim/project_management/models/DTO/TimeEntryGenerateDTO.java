package pl.wolniarskim.project_management.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimeEntryGenerateDTO {
    private double hoursSpent;
    private String description;
    private String email;
    private String taskName;
}
