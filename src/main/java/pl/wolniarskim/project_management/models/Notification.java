package pl.wolniarskim.project_management.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(value = EnumType.STRING)
    private NotificationStatus status;
    private String notificationContent;
    @ManyToOne
    private User relatedTo;
}
