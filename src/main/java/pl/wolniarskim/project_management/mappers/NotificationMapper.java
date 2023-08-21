package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.NotificationReadModel;
import pl.wolniarskim.project_management.models.Notification;

@Mapper
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    NotificationReadModel toReadModel(Notification notification);
}
