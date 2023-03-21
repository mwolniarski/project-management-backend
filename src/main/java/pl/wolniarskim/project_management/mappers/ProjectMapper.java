package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.ProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.SimpleProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.ProjectWriteModel;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;
import pl.wolniarskim.project_management.models.Project;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {UserMapper.class, TaskGroupMapper.class})
public interface ProjectMapper {

    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    SimpleProjectReadModel fromProject(Project project);

    @Mapping(target = "users", source = "project", qualifiedBy = ProjectUsersMapper.class)
    ProjectReadModel toProjectReadModel(Project project);

    Project toProject(ProjectWriteModel project);


    @ProjectUsersMapper
    static Set<UserReadModel> toProjectUsers(Project project) {
        return project.getProjectUserList().stream()
                .map(projectUser -> {
                    UserReadModel readModel = UserMapper.INSTANCE.toReadModel(projectUser.getUser());
                    readModel.setRole(projectUser.getUserRole());
                    return readModel;
                }).collect(Collectors.toSet());
    }
}
