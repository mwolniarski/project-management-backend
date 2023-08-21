package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.Permission;

import java.util.List;

@Getter
@Setter
public class RoleWriteModel {
    private String name;
    private List<Permission> permissions;
}
