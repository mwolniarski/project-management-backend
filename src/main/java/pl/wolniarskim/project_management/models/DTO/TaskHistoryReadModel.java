package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TaskHistoryReadModel {

    private LocalDateTime createdAt;

    private String description;
}
