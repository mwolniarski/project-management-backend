package pl.wolniarskim.project_management.models.DTO;

import lombok.Builder;
import lombok.Getter;
import pl.wolniarskim.project_management.models.Permission;

import java.util.List;

@Getter
@Builder
public class ProfileDetails {

    private String profileImage;
    private String firstName;
    private String lastName;
    private String nick;
    private OrganizationReadModel organization;
    private List<Permission.PermissionEnum> permissions;
}
