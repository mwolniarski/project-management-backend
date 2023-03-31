package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_profile_images")
public class UserProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;

    @Lob
    @Column(name = "image_data", length = 1000)
    private byte[] imageData;
}
