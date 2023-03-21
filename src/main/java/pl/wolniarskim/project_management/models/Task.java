package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;
    @Enumerated(value = EnumType.STRING)
    private TaskPriority priority;
    private String description;
    private LocalDate dueDate;

    @ManyToOne
    private TaskGroup taskGroup;

    @ManyToOne
    private User taskOwner;

    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "users_tasks",
            joinColumns = { @JoinColumn(name = "task_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    private Set<User> watchers = new HashSet<>();
}
