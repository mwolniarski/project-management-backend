package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentReadModel {

    private long id;

    private String content;

    private UserReadModel createdBy;

    private LocalDateTime createdTime;
}
