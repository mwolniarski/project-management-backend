package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.Project;
import pl.wolniarskim.project_management.models.Role;

import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {UserMapper.class, TaskGroupMapper.class, RoleMapper.class})
public interface ProjectMapper {

    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    @Mapping(target = "owner", expression = "java(UserMapper.INSTANCE.toReadModel(project.getOwner()))")
    SimpleProjectReadModel fromProject(Project project);

    @Mapping(target = "users", source = "project", qualifiedBy = ProjectUsersMapper.class)
    ProjectReadModel toProjectReadModel(Project project);

    Project toProject(ProjectWriteModel project);

    @ProjectUsersMapper
    static Set<UserReadModel> toProjectUsers(Project project) {
        return project.getProjectUserList().stream()
                .map(projectUser -> {
                    UserReadModel readModel = UserMapper.INSTANCE.toReadModel(projectUser.getUser());
                    readModel.setRole(RoleMapper.INSTANCE.fromRole(projectUser.getUser().getMainRole()));
                    readModel.setProfileImage(projectUser.getUser().getProfileImage());
                    readModel.setNick(projectUser.getUser().getNick());
                    return readModel;
                }).collect(Collectors.toSet());
    }
}
