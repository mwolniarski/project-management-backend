package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.TaskGroupMapper;
import pl.wolniarskim.project_management.mappers.TaskMapper;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.models.DTO.TaskReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskWriteModel;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.TaskGroupRepository;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskHistoryService taskHistoryService;
    private final NotificationService notificationService;

    @Transactional
    public TaskReadModel createTask(long taskGroupId, TaskWriteModel taskWriteModel, User user){
        TaskGroup taskGroup = taskGroupRepository.findById(taskGroupId).orElseThrow();

        Project project = projectRepository.findById(taskGroup.getProject().getId()).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_CREATE);

        Task toSave = TaskMapper.INSTANCE.toTask(taskWriteModel);
        toSave.setTaskGroup(taskGroup);
        if(Objects.nonNull(taskWriteModel.getOwner())){
            User user1 = new User();
            user1.setId(taskWriteModel.getOwner().getId());
            toSave.setTaskOwner(user1);
        }

        Task savedTask = taskRepository.save(toSave);
        if(Objects.nonNull(taskWriteModel.getOwner())){
            Optional<User> byId = userRepository.findById(taskWriteModel.getOwner().getId());
            savedTask.setTaskOwner(byId.get());
        }

        taskHistoryService.addHistoryEntry("Task created with values: \n" + savedTask, savedTask.getId());

        return TaskMapper.INSTANCE.toReadModel(savedTask);
    }

    @Transactional
    public void deleteTask(long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_DELETE);

        taskRepository.deleteById(taskId);
    }

    @Transactional
    public TaskReadModel updateTask(long taskId, TaskWriteModel taskWriteModel){
        Task task = taskRepository.findById(taskId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_UPDATE);
        Task toSave = TaskMapper.INSTANCE.toTask(taskWriteModel);
        toSave.setTaskGroup(task.getTaskGroup());
        toSave.setId(task.getId());

        if(Objects.nonNull(taskWriteModel.getOwner())){
            User user1 = new User();
            user1.setId(taskWriteModel.getOwner().getId());
            toSave.setTaskOwner(user1);
            if(task.getStatus() != taskWriteModel.getStatus()){
                String message = String.format("Status of your task was changed from %s to %s", task.getStatus(), taskWriteModel.getStatus());
                notificationService.createNotification(user1, message, NotificationStatus.UNREAD);
            }
        }
        String taskValue = task.toString();
        Task savedTask = taskRepository.save(toSave);

        taskHistoryService.addHistoryEntry("Task updated from value: \n" + taskValue + "\n to: \n" + savedTask, savedTask.getId());
        return TaskMapper.INSTANCE.toReadModel(savedTask);
    }
}
