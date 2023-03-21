package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.TaskReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskWriteModel;
import pl.wolniarskim.project_management.models.Task;

@Mapper(uses = UserMapper.class)
public interface TaskMapper {

    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);
    TaskReadModel toReadModel(Task task);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "dueDate", source = "dueDate")
    Task toTask(TaskWriteModel taskWriteModel);
}
