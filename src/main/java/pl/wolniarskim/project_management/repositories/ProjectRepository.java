package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(nativeQuery = true, value = "select p.* from projects p inner join projects_users pu on p.id = pu.project_id where pu.user_id = ?1")
    List<Project> findAllByUserId(long userId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from projects where id = ?1")
    void deleteProjectById(long projectId);
}
