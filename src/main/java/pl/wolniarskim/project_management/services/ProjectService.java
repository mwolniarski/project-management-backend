package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.ProjectMapper;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.ProjectUserRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;
import static pl.wolniarskim.project_management.models.TaskPriority.*;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<SimpleProjectReadModel> getAllProject(){
        SecurityUtil.checkUserPermission(PROJECT_READ);

        User loggedUser = getLoggedUser();
        if(loggedUser.getMainRole().getName().equals("SUPER_ADMIN")){
            return projectRepository.findByOrganizationOrgId(loggedUser.getOrganization().getOrgId()).stream()
                    .map(ProjectMapper.INSTANCE::fromProject)
                    .collect(Collectors.toList());
        }

        return projectRepository.findAllByUserId(loggedUser.getId()).stream()
                .map(ProjectMapper.INSTANCE::fromProject)
                .collect(Collectors.toList());
    }

    public ProjectReadModel getProjectById(long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(PROJECT_READ);

        return ProjectMapper.INSTANCE.toProjectReadModel(project);
    }

    @Transactional
    public SimpleProjectReadModel createProject(ProjectWriteModel projectWriteModel){
        User user = getLoggedUser();
        SecurityUtil.checkUserPermission(PROJECT_CREATE);

        Project toSave = ProjectMapper.INSTANCE.toProject(projectWriteModel);
        toSave.setOwner(user);
        toSave.setStatus(ProjectStatus.ACTIVE);
        toSave.setOrganization(user.getOrganization());

        Project project = projectRepository.save(toSave);
        ProjectUser projectUser = new ProjectUser(project, user, ProjectUserRole.INTERNAL);
        projectUserRepository.save(projectUser);
        return ProjectMapper.INSTANCE.fromProject(project);
    }

    public SimpleProjectReadModel updateProject(long id, ProjectWriteModel projectWriteModel){
        Project project = projectRepository.findById(id).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(PROJECT_UPDATE);

        Project projectToSave = ProjectMapper.INSTANCE.toProject(projectWriteModel);
        projectToSave.setId(id);
        Project projectSaved = projectRepository.save(projectToSave);
        return ProjectMapper.INSTANCE.fromProject(projectSaved);
    }

    public void addUserToProject(long projectId, String email, ProjectUserRole projectUserRole){
        Project project = projectRepository.findById(projectId).orElseThrow();
        User userToAdd = userRepository.findByEmail(email).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkIfUserIsPartOfOrganization(userToAdd.getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(PROJECT_ADD_USER);

        ProjectUser projectUser = new ProjectUser(project, userToAdd, projectUserRole);

        notificationService.createNotification(userToAdd, "You have been added to project", NotificationStatus.UNREAD);

        projectUserRepository.save(projectUser);
    }

    @Transactional
    public void deleteUserFromProject(long projectId, String email){
        Project project = projectRepository.findById(projectId).orElseThrow();

        ProjectUser projectUser = project.getProjectUserList().stream()
                .filter(user -> user.getUser().getEmail().equals(email)).findFirst().orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkIfUserIsPartOfOrganization(projectUser.getUser().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(PROJECT_REMOVE_USER);

        notificationService.createNotification(projectUser.getUser(), "You have been removed from project", NotificationStatus.UNREAD);

        projectUserRepository.deleteUserFromProject(project.getId(), projectUser.getUser().getId());
    }

    @Transactional
    public void deleteProject(long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(PROJECT_DELETE);

        projectRepository.deleteById(projectId);
    }


    @Transactional
    public ProjectMetaData getOverview(long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        List<Task> tasks = new ArrayList<>();
        project.getTaskGroups().forEach(
               taskGroup -> tasks.addAll(taskGroup.getTasks())
        );

        long countOfAllTasksWithDoneStatus = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        ProjectMetaData projectMetaData = new ProjectMetaData();
        projectMetaData.setNumberOfAllTasks(tasks.size());
        projectMetaData.setNumberOfCompletedTasks(countOfAllTasksWithDoneStatus);
        TaskByPriorityMetadata taskByPriorityMetadata = new TaskByPriorityMetadata();
        taskByPriorityMetadata.setNumberOfTasksWithLowPriority(countByPriority(tasks, LOW));
        taskByPriorityMetadata.setNumberOfTasksWithNormalPriority(countByPriority(tasks, NORMAL));
        taskByPriorityMetadata.setNumberOfTasksWithHighPriority(countByPriority(tasks, HIGH));
        taskByPriorityMetadata.setNumberOfTasksWithUrgentPriority(countByPriority(tasks, URGENT));
        projectMetaData.setTaskByPriority(taskByPriorityMetadata);
        return projectMetaData;
    }

    private long countByPriority(List<Task> tasks, TaskPriority taskPriority){
        return tasks.stream()
                .filter(task -> task.getPriority() == taskPriority)
                .count();
    }
}
