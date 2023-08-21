package pl.wolniarskim.project_management.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reset_password_token")
public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String token;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime usedAt;
}
