package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskByPriorityMetadata {
    private long numberOfTasksWithLowPriority;
    private long numberOfTasksWithNormalPriority;
    private long numberOfTasksWithHighPriority;
    private long numberOfTasksWithUrgentPriority;
}
