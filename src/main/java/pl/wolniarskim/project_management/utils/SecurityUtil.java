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

    private static boolean userHasPermission(Project project, long userId){
        return project.getProjectUserList().stream()
                .anyMatch(projectUser -> userHasRole(projectUser, userId));
    }

    public static void checkPermission(Project project, long userId){
        if(!userHasPermission(project, userId)){
            throw new PermissionDeniedException();
        }
    }

    private static User getUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    private static boolean userHasRole(ProjectUser projectUser, long userId){
        return projectUser.getUserRole() == ProjectUserRole.ADMIN &&
                projectUser.getUser().getId() == userId;
    }
}
