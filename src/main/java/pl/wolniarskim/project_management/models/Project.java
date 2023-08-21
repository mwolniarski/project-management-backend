package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Enumerated(value = EnumType.STRING)
    private ProjectStatus status;
    private LocalDate startTime;
    private LocalDate endTime;
    private String description;
    @ManyToOne
    private Organization organization;
    @ManyToOne
    private User owner;
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectUser> projectUserList = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<TaskGroup> taskGroups = new HashSet<>();
}
