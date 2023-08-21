package pl.wolniarskim.project_management.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentWriteModel {
    private String comment;
    private long taskId;
}
