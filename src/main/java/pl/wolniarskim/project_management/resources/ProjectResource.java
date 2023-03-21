package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.ProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.SimpleProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.ProjectWriteModel;
import pl.wolniarskim.project_management.models.ProjectUserRole;
import pl.wolniarskim.project_management.services.ProjectService;

import java.util.List;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUserId;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectResource {

    private final ProjectService projectService;

    @GetMapping
    public List<SimpleProjectReadModel> getAllProjects(){
        return projectService.getAllProject(getLoggedUserId());
    }

    @GetMapping("/{projectId}")
    public ProjectReadModel getProjectById(@PathVariable("projectId") long projectId){
        return projectService.getProjectById(projectId, getLoggedUser());
    }

    @PostMapping("/create")
    public SimpleProjectReadModel createProject(@RequestBody ProjectWriteModel projectWriteModel){
        return projectService.createProject(projectWriteModel, getLoggedUser());
    }

    @PutMapping("/update/{projectId}")
    public SimpleProjectReadModel updateProject(@PathVariable("projectId") long projectId, @RequestBody ProjectWriteModel projectWriteModel){
        return projectService.updateProject(projectId, projectWriteModel, getLoggedUser());
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable("projectId") long projectId){
        projectService.deleteProject(projectId, getLoggedUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/addUser/{projectId}")
    public ResponseEntity<String> addUserToProject(@PathVariable("projectId") long projectId,
                                                   @Param("email") String email,
                                                   @Param("role") ProjectUserRole role){
        projectService.addUserToProject(projectId, email, role, getLoggedUser());
        return ResponseEntity.ok().build();
    }
}
