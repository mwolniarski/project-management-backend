package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.models.TaskGroup;

@Mapper(uses = TaskMapper.class)
public interface TaskGroupMapper {

    TaskGroupMapper INSTANCE = Mappers.getMapper(TaskGroupMapper.class);

    TaskGroupReadModel toReadModel(TaskGroup taskGroup);

    TaskGroup toTaskGroup(TaskGroupWriteModel taskGroup);
}
