package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.TaskReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskWriteModel;
import pl.wolniarskim.project_management.services.TaskService;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskResource {
    private final TaskService taskService;

    @PostMapping("/create/{taskGroupId}")
    public TaskReadModel createTask(@RequestBody TaskWriteModel taskWriteModel,
                                    @PathVariable("taskGroupId") long taskGroupId){
        return taskService.createTask(taskGroupId, taskWriteModel, getLoggedUser());
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> deleteTaskGroup(@PathVariable("taskId") long taskId){
        taskService.deleteTask(taskId, getLoggedUser());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{taskId}")
    public TaskReadModel updateTaskGroup(@RequestBody TaskWriteModel taskWriteModel,
                                              @PathVariable("taskId") long taskId){
        return taskService.updateTask(taskId, taskWriteModel, getLoggedUser());
    }
}
