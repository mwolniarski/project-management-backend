package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class TaskGroupReadModel {
    private long id;
    private String name;
    private Set<TaskReadModel> tasks = new HashSet<>();
}
