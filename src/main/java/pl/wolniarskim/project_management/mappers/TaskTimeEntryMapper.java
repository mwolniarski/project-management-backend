package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import pl.wolniarskim.project_management.models.DTO.TaskTimeEntryReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskTimeEntryWriteModel;
import pl.wolniarskim.project_management.models.TaskTimeEntry;

@Mapper(componentModel = "spring")
public interface TaskTimeEntryMapper {

    TaskTimeEntryReadModel fromTimeEntry(TaskTimeEntry taskTimeEntry);
    TaskTimeEntry toTaskTimeEntry(TaskTimeEntryWriteModel taskTimeEntryWriteModel);
}
