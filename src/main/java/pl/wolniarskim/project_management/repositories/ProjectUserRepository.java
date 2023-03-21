package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.ProjectUser;
import pl.wolniarskim.project_management.models.ProjectUserId;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, ProjectUserId> {
}
