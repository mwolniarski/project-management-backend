package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.ProjectStatus;
import pl.wolniarskim.project_management.models.ProjectUser;
import pl.wolniarskim.project_management.models.TaskGroup;
import pl.wolniarskim.project_management.models.User;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProjectReadModel {
    private long id;
    private String name;
    private ProjectStatus status;
    private LocalDate startTime;
    private LocalDate endTime;
    private String description;
    private UserReadModel owner;
    private Set<UserReadModel> users = new HashSet<>();
    private Set<TaskGroupReadModel> taskGroups = new HashSet<>();
}
