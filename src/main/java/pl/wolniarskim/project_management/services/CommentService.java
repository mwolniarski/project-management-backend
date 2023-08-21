package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.Mapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.mappers.CommentMapper;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.CommentReadModel;
import pl.wolniarskim.project_management.models.DTO.CommentWriteModel;
import pl.wolniarskim.project_management.repositories.CommentRepository;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentService {
    private static final String EMAIL_SPLIT_SIGN = "@@@@";
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TaskHistoryService taskHistoryService;

    public CommentReadModel addComment(CommentWriteModel commentWriteModel){
        Task task = taskRepository.findById(commentWriteModel.getTaskId()).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_ADD_COMMENT);

        User user = getLoggedUser();

        Comment comment = new Comment();
        comment.setContent(commentWriteModel.getComment());
        comment.setCreatedBy(user);
        comment.setCreatedTime(LocalDateTime.now());
        comment.setTask(task);

        createNotificationIfNeeded(comment);

        return CommentMapper.INSTANCE.toReadModel(commentRepository.save(comment));
    }

    public CommentReadModel editComment(long commentId, CommentWriteModel commentWriteModel, User user){
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(comment.getTask().getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_EDIT_COMMENT);

        if(comment.getCreatedBy().getId() != user.getId()){
            throw new PermissionDeniedException();
        }
        comment.setContent(commentWriteModel.getComment());

        return CommentMapper.INSTANCE.toReadModel(commentRepository.save(comment));
    }

    public void deleteComment(long commentId, User user){
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(comment.getTask().getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(Permission.PermissionEnum.TASK_DELETE_COMMENT);

        if(comment.getCreatedBy().getId() != user.getId()){
            throw new PermissionDeniedException();
        }

        commentRepository.deleteById(commentId);
    }

    public List<CommentReadModel> getCommentsForTask(long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();
        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        return commentRepository.getCommentByTask(task).stream()
                .map(CommentMapper.INSTANCE::toReadModel)
                .collect(Collectors.toList());
    }

    private void createNotificationIfNeeded(Comment comment){

        String commentContent = comment.getContent();
        while(commentContent.contains(EMAIL_SPLIT_SIGN)){
            int firstEmailSign = commentContent.indexOf(EMAIL_SPLIT_SIGN);
            String newString = commentContent.substring(firstEmailSign + 4);
            int secondEmailSign = newString.indexOf(EMAIL_SPLIT_SIGN);

            String email = newString.substring(0, secondEmailSign);
            log.info("Searching for user with email {}", email);
            User user = (User) userService.loadUserByUsername(email);

            SecurityUtil.checkIfUserIsPartOfOrganization(user.getOrganization().getOrgId());

            notificationService.createNotification(user, "You got mark in task comment -> " + comment.getTask().getId(), NotificationStatus.UNREAD);

            commentContent = newString.substring(0, secondEmailSign);
         }
    }
}
