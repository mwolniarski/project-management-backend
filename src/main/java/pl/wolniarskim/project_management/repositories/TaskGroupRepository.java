package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.TaskGroup;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Long> {

    @Modifying
    @Query(nativeQuery = true, value = "delete from task_groups where id = ?1")
    void deleteTaskGroupById(long taskGroup);
}
