package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.NotificationStatus;

@Getter
@Setter
public class NotificationReadModel {

    private long id;
    private NotificationStatus status;
    private String notificationContent;
}
