package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.TaskGroupMapper;
import pl.wolniarskim.project_management.mappers.TaskMapper;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.models.DTO.TaskReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskWriteModel;
import pl.wolniarskim.project_management.models.Project;
import pl.wolniarskim.project_management.models.Task;
import pl.wolniarskim.project_management.models.TaskGroup;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.TaskGroupRepository;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public TaskReadModel createTask(long taskGroupId, TaskWriteModel taskWriteModel, User user){
        TaskGroup taskGroup = taskGroupRepository.findById(taskGroupId).orElseThrow();

        Project project = projectRepository.findById(taskGroup.getProject().getId()).orElseThrow();

        SecurityUtil.checkPermission(project, user.getId());

        Task toSave = TaskMapper.INSTANCE.toTask(taskWriteModel);
        toSave.setTaskGroup(taskGroup);

        Task savedTask = taskRepository.save(toSave);

        return TaskMapper.INSTANCE.toReadModel(savedTask);
    }

    @Transactional
    public void deleteTask(long taskId, User user){
        Task task = taskRepository.findById(taskId).orElseThrow();

        SecurityUtil.checkPermission(task.getTaskGroup().getProject(), user.getId());

        taskRepository.deleteTaskById(taskId);
    }

    @Transactional
    public TaskReadModel updateTask(long taskId, TaskWriteModel taskWriteModel, User user){
        Task task = taskRepository.findById(taskId).orElseThrow();

        SecurityUtil.checkPermission(task.getTaskGroup().getProject(), user.getId());
        Task toSave = TaskMapper.INSTANCE.toTask(taskWriteModel);
        toSave.setTaskGroup(task.getTaskGroup());
        toSave.setId(task.getId());

        Task savedTask = taskRepository.save(toSave);
        return TaskMapper.INSTANCE.toReadModel(savedTask);
    }
}
