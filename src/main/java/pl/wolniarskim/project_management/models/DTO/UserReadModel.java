package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.ProjectUserRole;

@Getter
@Setter
public class UserReadModel {

    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private ProjectUserRole role;
}
