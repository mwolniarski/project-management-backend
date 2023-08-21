package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.RoleReadModel;
import pl.wolniarskim.project_management.models.DTO.RoleWriteModel;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.services.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleResource {

    private final RoleService roleService;

    @GetMapping("/permissions")
    public List<Permission> getPermissions(){
        return roleService.getAllPermissions();
    }

    @GetMapping
    public List<RoleReadModel> getRoles(){
        return roleService.getAllRoles();
    }

    @PostMapping
    public RoleReadModel addRole(@RequestBody RoleWriteModel roleWriteModel){
        return roleService.addRole(roleWriteModel);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable long roleId){
        roleService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }
}
