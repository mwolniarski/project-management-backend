package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.services.TaskGroupService;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@RestController
@RequestMapping("/api/taskGroups")
@RequiredArgsConstructor
public class TaskGroupResource {


    private final TaskGroupService taskGroupService;

    @PostMapping("/create/{projectId}")
    public TaskGroupReadModel createTaskGroup(@RequestBody TaskGroupWriteModel taskGroupWriteModel,
                                              @PathVariable("projectId") long projectId){
        return taskGroupService.createTaskGroup(projectId, taskGroupWriteModel, getLoggedUser());
    }

    @DeleteMapping("/delete/{taskGroupId}")
    public ResponseEntity<String> deleteTaskGroup(@PathVariable("taskGroupId") long taskGroupId){
        taskGroupService.deleteTaskGroup(taskGroupId, getLoggedUser());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{taskGroupId}")
    public TaskGroupReadModel updateTaskGroup(@RequestBody TaskGroupWriteModel taskGroupWriteModel,
                                              @PathVariable("taskGroupId") long taskGroupId){
        return taskGroupService.updateTaskGroup(taskGroupId, taskGroupWriteModel, getLoggedUser());
    }
}
