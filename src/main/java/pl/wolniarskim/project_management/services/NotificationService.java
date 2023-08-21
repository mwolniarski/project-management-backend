package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.mappers.NotificationMapper;
import pl.wolniarskim.project_management.models.DTO.NotificationReadModel;
import pl.wolniarskim.project_management.models.Notification;
import pl.wolniarskim.project_management.models.NotificationStatus;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.NotificationRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationReadModel> getAllNotifications(User user){
        return notificationRepository.getAllByRelatedTo(user).stream()
                .map(NotificationMapper.INSTANCE::toReadModel)
                .collect(Collectors.toList());
    }

    public void createNotification(User user, String notificationContent, NotificationStatus status){
        Notification notification = new Notification();
        notification.setRelatedTo(user);
        notification.setNotificationContent(notificationContent);
        notification.setStatus(status);
        notificationRepository.save(notification);
    }

    public void updateNotificationStatus(long notificationId, User user){
        notificationRepository.findById(notificationId).ifPresent(
                notification -> {
                    if(notification.getRelatedTo().getId() != user.getId()){
                        throw new PermissionDeniedException();
                    }
                    notification.setStatus(NotificationStatus.READ);

                    notificationRepository.save(notification);
                }
        );
    }
}
