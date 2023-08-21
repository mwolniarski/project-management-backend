package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMetaData {

    private long numberOfAllTasks;
    private long numberOfCompletedTasks;

    private TaskByPriorityMetadata taskByPriority;
    private TimesheetMetadata timesheetMetadata;
}
