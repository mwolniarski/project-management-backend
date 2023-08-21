package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.ProjectUserRole;
import pl.wolniarskim.project_management.models.Role;

@Getter
@Setter
public class UserReadModel {

    private long id;
    private String email;
    private String profileImage;
    private String firstName;
    private String lastName;
    private String nick;
    private RoleReadModel role;
}
