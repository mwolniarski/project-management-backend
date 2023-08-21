package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
