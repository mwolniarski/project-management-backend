package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "task_groups")
public class TaskGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @ManyToOne
    private Project project;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "taskGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();
}
