package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class ProjectUserId implements Serializable {
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "user_id")
    private long userId;
}
