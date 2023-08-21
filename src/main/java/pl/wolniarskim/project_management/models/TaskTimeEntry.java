package pl.wolniarskim.project_management.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "task_time_entry")
public class TaskTimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Task task;
    private double hoursSpent;
    private String description;
}
