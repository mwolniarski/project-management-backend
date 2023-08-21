package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @ToString.Exclude
    private long id;

    private String name;
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;
    @Enumerated(value = EnumType.STRING)
    private TaskPriority priority;
    private String description;
    private LocalDate dueDate;
    private double estimatedWorkTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private TaskGroup taskGroup;

    @ManyToOne
    @ToString.Exclude
    private User taskOwner;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_tasks",
            joinColumns = { @JoinColumn(name = "task_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    private Set<User> watchers = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TaskHistory> taskHistories = new HashSet<>();
}
