package pl.wolniarskim.project_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.models.UserProfileImage;

import java.util.Optional;

@Repository
public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {
    Optional<UserProfileImage> findUserProfileImageByUser(User user);
}
