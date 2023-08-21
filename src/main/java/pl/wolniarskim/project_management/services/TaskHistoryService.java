package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.mappers.TaskHistoryMapper;
import pl.wolniarskim.project_management.models.DTO.TaskHistoryReadModel;
import pl.wolniarskim.project_management.models.Task;
import pl.wolniarskim.project_management.models.TaskHistory;
import pl.wolniarskim.project_management.repositories.TaskHistoryRepository;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.TASK_HISTORY_READ;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskHistoryMapper taskHistoryMapper;
    public List<TaskHistoryReadModel> getHistories(long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();
        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(TASK_HISTORY_READ);

        return taskHistoryRepository.findAllByTask_Id(taskId).stream()
                .map(taskHistoryMapper::fromTask)
                .collect(Collectors.toList());
    }

    public void addHistoryEntry(String description, long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();

        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTask(task);
        taskHistory.setDescription(description);
        taskHistory.setCreatedAt(LocalDateTime.now());

        taskHistoryRepository.save(taskHistory);
    }
}
