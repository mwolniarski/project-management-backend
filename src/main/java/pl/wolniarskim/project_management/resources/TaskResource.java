package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.TaskHistory;
import pl.wolniarskim.project_management.models.TaskTimeEntry;
import pl.wolniarskim.project_management.services.CommentService;
import pl.wolniarskim.project_management.services.TaskHistoryService;
import pl.wolniarskim.project_management.services.TaskService;
import pl.wolniarskim.project_management.services.TaskTimeEntryService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskResource {
    private final TaskService taskService;
    private final CommentService commentService;
    private final TaskHistoryService taskHistoryService;
    private final TaskTimeEntryService taskTimeEntryService;

    @PostMapping("/create/{taskGroupId}")
    public TaskReadModel createTask(@RequestBody TaskWriteModel taskWriteModel,
                                    @PathVariable("taskGroupId") long taskGroupId){
        return taskService.createTask(taskGroupId, taskWriteModel, getLoggedUser());
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable("taskId") long taskId){
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{taskId}")
    public TaskReadModel updateTask(@RequestBody TaskWriteModel taskWriteModel,
                                              @PathVariable("taskId") long taskId){
        return taskService.updateTask(taskId, taskWriteModel);
    }

    @GetMapping("/comments/{taskId}")
    public List<CommentReadModel> getComments(@PathVariable("taskId") long taskId){
        return commentService.getCommentsForTask(taskId);
    }

    @GetMapping("/history/{taskId}")
    public List<TaskHistoryReadModel> getHistories(@PathVariable("taskId") long taskId){
        return taskHistoryService.getHistories(taskId);
    }

    @PostMapping("/timesheet/{taskId}")
    public TaskTimeEntryReadModel addTimeEntry(@PathVariable("taskId") long taskId, @RequestBody TaskTimeEntryWriteModel taskTimeEntryWriteModel){
        return taskTimeEntryService.addTimeEntry(taskTimeEntryWriteModel, taskId);
    }

    @GetMapping("/timesheet/{taskId}")
    public List<TaskTimeEntryReadModel> getTimeEntries(@PathVariable("taskId") long taskId){
        return taskTimeEntryService.getTimeEntriesForTask(taskId);
    }

    @DeleteMapping("/timesheet/{timeEntryId}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable("timeEntryId") long timeEntryId){
        taskTimeEntryService.deleteTimeEntry(timeEntryId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/timesheet/export/{projectId}")
    public void exportTimeEntries(@PathVariable("projectId") long projectId, HttpServletResponse response) throws IOException {
        taskTimeEntryService.exportTimeEntry(response, projectId);
    }
}
