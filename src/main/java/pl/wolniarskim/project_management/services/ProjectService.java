package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.ProjectMapper;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.ProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.SimpleProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.ProjectWriteModel;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.ProjectUserRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;

    public List<SimpleProjectReadModel> getAllProject(long userId){
        return projectRepository.findAllByUserId(userId).stream()
                .map(ProjectMapper.INSTANCE::fromProject)
                .collect(Collectors.toList());
    }

    public ProjectReadModel getProjectById(long projectId, User user){
        Project project = projectRepository.findById(projectId).orElseThrow();

        SecurityUtil.checkPermission(project, user.getId());

        return ProjectMapper.INSTANCE.toProjectReadModel(project);
    }

    @Transactional
    public SimpleProjectReadModel createProject(ProjectWriteModel projectWriteModel, User user){
        Project toSave = ProjectMapper.INSTANCE.toProject(projectWriteModel);
        toSave.setOwner(user);
        toSave.setStatus(ProjectStatus.ACTIVE);

        Project project = projectRepository.save(toSave);
        ProjectUser projectUser = new ProjectUser(project, user, ProjectUserRole.ADMIN);
        projectUserRepository.save(projectUser);
        return ProjectMapper.INSTANCE.fromProject(project);
    }

    public SimpleProjectReadModel updateProject(long id, ProjectWriteModel projectWriteModel, User user){
        Project project = projectRepository.findById(id).orElseThrow();

        SecurityUtil.checkPermission(project, user.getId());

        Project projectToSave = ProjectMapper.INSTANCE.toProject(projectWriteModel);
        projectToSave.setId(id);
        Project projectSaved = projectRepository.save(projectToSave);
        return ProjectMapper.INSTANCE.fromProject(projectSaved);
    }

    public void addUserToProject(long projectId, String email, ProjectUserRole projectUserRole, User loggedUser){
        Project project = projectRepository.findById(projectId).orElseThrow();
        User userToAdd = userRepository.findByEmail(email).orElseThrow();

        SecurityUtil.checkPermission(project, loggedUser.getId());

        ProjectUser projectUser = new ProjectUser(project, userToAdd, projectUserRole);
        projectUserRepository.save(projectUser);
    }

    //todo: usunięcie wszystkich użytkowników z projektu, zadań, grup zadań (musi być w transactional)

    @Transactional
    public void deleteProject(long projectId, User user){
        Project project = projectRepository.findById(projectId).orElseThrow();

        SecurityUtil.checkPermission(project, user.getId());

        projectRepository.deleteById(projectId);
    }
}
