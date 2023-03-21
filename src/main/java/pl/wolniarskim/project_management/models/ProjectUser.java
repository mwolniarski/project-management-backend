package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "projects_users")
public class ProjectUser {

    @EmbeddedId
    private ProjectUserId id = new ProjectUserId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(value = EnumType.STRING)
    private ProjectUserRole userRole;

    public ProjectUser(Project project, User user, ProjectUserRole userRole) {
        this.project = project;
        this.user = user;
        this.userRole = userRole;
    }
}
