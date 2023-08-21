package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "organizations")
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orgId;
    private String name;
    @Enumerated(value = EnumType.STRING)
    private OrgStatus orgStatus;

    public enum OrgStatus{ACTIVE, DELETED};
}
