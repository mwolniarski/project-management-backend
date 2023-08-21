package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import pl.wolniarskim.project_management.models.DTO.TaskHistoryReadModel;
import pl.wolniarskim.project_management.models.TaskHistory;

@Mapper(componentModel = "spring")
public interface TaskHistoryMapper {

    TaskHistoryReadModel fromTask(TaskHistory taskHistory);
}
