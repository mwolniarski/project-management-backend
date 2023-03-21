package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.TaskGroupMapper;
import pl.wolniarskim.project_management.mappers.TaskMapper;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.models.Project;
import pl.wolniarskim.project_management.models.TaskGroup;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.TaskGroupRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public TaskGroupReadModel createTaskGroup(long projectId, TaskGroupWriteModel taskGroupWriteModel, User user){
        Project project = projectRepository.findById(projectId).orElseThrow();

        SecurityUtil.checkPermission(project, user.getId());

        TaskGroup toSave = TaskGroupMapper.INSTANCE.toTaskGroup(taskGroupWriteModel);
        toSave.setProject(project);

        TaskGroup savedTaskGroup = taskGroupRepository.save(toSave);

        return TaskGroupMapper.INSTANCE.toReadModel(savedTaskGroup);
    }

    @Transactional
    public void deleteTaskGroup(long taskGroupId, User user){
        TaskGroup taskGroup = taskGroupRepository.findById(taskGroupId).orElseThrow();

        SecurityUtil.checkPermission(taskGroup.getProject(), user.getId());

        taskGroupRepository.deleteTaskGroupById(taskGroupId);
    }

    @Transactional
    public TaskGroupReadModel updateTaskGroup(long taskGroupId, TaskGroupWriteModel taskGroupWriteModel, User user){
        TaskGroup taskGroup = taskGroupRepository.findById(taskGroupId).orElseThrow();

        SecurityUtil.checkPermission(taskGroup.getProject(), user.getId());

        TaskGroup toSave = TaskGroupMapper.INSTANCE.toTaskGroup(taskGroupWriteModel);
        toSave.setProject(taskGroup.getProject());
        toSave.setId(taskGroupId);

        TaskGroup savedTaskGroup = taskGroupRepository.save(toSave);
        return TaskGroupMapper.INSTANCE.toReadModel(savedTaskGroup);
    }
}
