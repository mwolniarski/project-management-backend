package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWriteModel {
    private String firstName;
    private String lastName;
    private String email;
    private long roleId;
}
