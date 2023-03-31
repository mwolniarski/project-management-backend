package pl.wolniarskim.project_management.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.models.Project;
import pl.wolniarskim.project_management.models.ProjectUser;
import pl.wolniarskim.project_management.models.ProjectUserRole;
import pl.wolniarskim.project_management.models.User;

public class SecurityUtil {

    public static User getLoggedUser(){
        return getUser();
    }

    public static long getLoggedUserId(){
        return getUser().getId();
    }

    private static boolean userHasWritePermission(Project project, long userId){
        return project.getProjectUserList().stream()
                .anyMatch(projectUser -> userHasWriteRole(projectUser, userId));
    }

    private static boolean userHasReadPermission(Project project, long userId){
        return project.getProjectUserList().stream()
                .anyMatch(projectUser -> userHasReadRole(projectUser, userId));
    }

    public static void checkWritePermission(Project project, long userId){
        if(!userHasWritePermission(project, userId)){
            throw new PermissionDeniedException();
        }
    }

    public static void checkReadPermission(Project project, long userId){
        if(!userHasReadPermission(project, userId)){
            throw new PermissionDeniedException();
        }
    }

    private static User getUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    private static boolean userHasWriteRole(ProjectUser projectUser, long userId){
        return projectUser.getUserRole() == ProjectUserRole.ADMIN &&
                projectUser.getUser().getId() == userId;
    }

    private static boolean userHasReadRole(ProjectUser projectUser, long userId){
        return projectUser.getUser().getId() == userId;
    }
}
