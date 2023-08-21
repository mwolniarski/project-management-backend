package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.TaskTimeEntry;

import java.util.List;

@Repository
public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, Long> {

    @Query(nativeQuery = true, value = "select t.* from task_time_entry t inner join tasks tk on t.task_id = tk.id inner join task_groups tg on tk.task_group_id = tg.id where tg.project_id = ?1")
    List<TaskTimeEntry> findAllByProjectId(long projectId);

    @Query(nativeQuery = true, value = "select t.* from task_time_entry t inner join tasks tk on t.task_id = tk.id inner join task_groups tg on tk.task_group_id = tg.id where tg.project_id = ?2 and t.user_id = ?1")
    List<TaskTimeEntry> findAllByUserIdAndProjectId(long userId, long projectId);

    List<TaskTimeEntry> findAllByTask_Id(long taskId);
}
