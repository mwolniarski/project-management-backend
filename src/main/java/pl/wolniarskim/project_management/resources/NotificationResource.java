package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.NotificationReadModel;
import pl.wolniarskim.project_management.services.NotificationService;

import java.util.List;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationReadModel> getAllNotifications(){
        return notificationService.getAllNotifications(getLoggedUser());
    }

    @PostMapping("/{notificationId}")
    public void updateNotificationStatus(@PathVariable("notificationId") long notificationId){
        notificationService.updateNotificationStatus(notificationId, getLoggedUser());
    }
}
