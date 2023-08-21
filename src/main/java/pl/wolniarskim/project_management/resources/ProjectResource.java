package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.ProjectUserRole;
import pl.wolniarskim.project_management.services.ProjectService;
import pl.wolniarskim.project_management.services.TaskTimeEntryService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectResource {

    private final ProjectService projectService;
    private final TaskTimeEntryService taskTimeEntryService;
    @GetMapping
    public List<SimpleProjectReadModel> getAllProjects(){
        return projectService.getAllProject();
    }

    @GetMapping("/{projectId}")
    public ProjectReadModel getProjectById(@PathVariable("projectId") long projectId){
        return projectService.getProjectById(projectId);
    }

    @PostMapping("/create")
    public SimpleProjectReadModel createProject(@RequestBody ProjectWriteModel projectWriteModel){
        return projectService.createProject(projectWriteModel);
    }

    @PutMapping("/update/{projectId}")
    public SimpleProjectReadModel updateProject(@PathVariable("projectId") long projectId, @RequestBody ProjectWriteModel projectWriteModel){
        return projectService.updateProject(projectId, projectWriteModel);
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable("projectId") long projectId){
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/addUser/{projectId}")
    public ResponseEntity<String> addUserToProject(@PathVariable("projectId") long projectId,
                                                   @Param("email") String email,
                                                   @Param("role") ProjectUserRole role){
        projectService.addUserToProject(projectId, email, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteUser/{projectId}")
    public ResponseEntity<String> deleteUserFromProject(@PathVariable("projectId") long projectId,
                                                   @Param("email") String email){
        projectService.deleteUserFromProject(projectId, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/overview/{projectId}")
    public ResponseEntity<ProjectMetaData> getProjectMetadata(@PathVariable long projectId){
        return ResponseEntity.ok().body(projectService.getOverview(projectId));
    }

    @GetMapping("/timesheet/{projectId}")
    public List<TaskTimeEntryReadModel> getTimeEntries(@PathVariable("projectId") long projectId){
        return taskTimeEntryService.getTimeEntries(projectId);
    }
}
