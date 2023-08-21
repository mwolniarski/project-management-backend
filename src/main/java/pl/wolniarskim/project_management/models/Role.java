package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    public Role(String name, List<Permission> permissions){
        this.name = name;
        this.permissions = permissions;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @ManyToOne
    private Organization organization;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "permissions_roles",
            joinColumns = { @JoinColumn(name = "role_id") },
            inverseJoinColumns = { @JoinColumn(name = "permission_name") }
    )
    private List<Permission> permissions;
}
