package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
