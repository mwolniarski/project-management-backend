package pl.wolniarskim.project_management.models;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String content;
    @ManyToOne
    private User createdBy;

    @ManyToOne
    private Task task;
    private LocalDateTime createdTime;
}
