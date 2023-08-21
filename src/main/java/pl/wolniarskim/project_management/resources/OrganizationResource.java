package pl.wolniarskim.project_management.resources;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.OrganizationWriteModel;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;
import pl.wolniarskim.project_management.models.DTO.UserWriteModel;
import pl.wolniarskim.project_management.services.OrganizationService;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationResource {

    private final OrganizationService organizationService;

    @PutMapping("/{organizationId}")
    public ResponseEntity<Void> updateOrganizationDetails(@RequestBody OrganizationWriteModel organizationWriteModel, @PathVariable long organizationId) {
        organizationService.updateOrganization(organizationWriteModel, organizationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganizationDetails(@PathVariable long organizationId) {
        organizationService.removeOrganization(organizationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users")
    public ResponseEntity<Void> createUserForOrganization(@RequestBody UserWriteModel userWriteModel) {
        organizationService.createAccountForUser(userWriteModel);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUserFromOrganization(@RequestParam String email) {
        organizationService.deleteAccountFromOrganization(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public List<UserReadModel> getOrganizationUsers() {
        return organizationService.getAllUsers();
    }
}
