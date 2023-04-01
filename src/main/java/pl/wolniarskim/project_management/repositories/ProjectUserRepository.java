package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.ProjectUser;
import pl.wolniarskim.project_management.models.ProjectUserId;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, ProjectUserId> {

    @Modifying
    @Query(nativeQuery = true, value = "delete from projects_users where project_id = ?1 and user_id = ?2")
    void deleteUserFromProject(long projectId, long userId);
}
