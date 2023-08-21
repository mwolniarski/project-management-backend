package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import pl.wolniarskim.project_management.models.Organization;
import pl.wolniarskim.project_management.models.Permission;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
public class RoleReadModel {

    private long id;
    private String name;
    private List<Permission> permissions;
}
