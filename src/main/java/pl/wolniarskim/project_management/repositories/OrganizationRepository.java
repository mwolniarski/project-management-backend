package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
