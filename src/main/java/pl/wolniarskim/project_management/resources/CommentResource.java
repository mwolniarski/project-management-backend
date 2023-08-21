package pl.wolniarskim.project_management.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wolniarskim.project_management.models.DTO.CommentReadModel;
import pl.wolniarskim.project_management.models.DTO.CommentWriteModel;
import pl.wolniarskim.project_management.services.CommentService;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentResource {

    private final CommentService commentService;

    @PostMapping("/create")
    public CommentReadModel addComment(@RequestBody CommentWriteModel commentWriteModel){
        return commentService.addComment(commentWriteModel);
    }

    @PutMapping("/update/{commentId}")
    public CommentReadModel addComment(@RequestBody CommentWriteModel commentWriteModel,
                                       @PathVariable("commentId") long commentId){
        return commentService.editComment(commentId, commentWriteModel, getLoggedUser());
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> addComment(@PathVariable("commentId") long commentId){
        commentService.deleteComment(commentId, getLoggedUser());
        return ResponseEntity.ok().build();
    }
}
