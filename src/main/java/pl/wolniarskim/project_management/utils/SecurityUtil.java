package pl.wolniarskim.project_management.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.models.*;

import java.util.Objects;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.ALLOW_ALL;

public class SecurityUtil {

    public static User getLoggedUser(){
        return getUser();
    }

    public static long getLoggedUserId(){
        return getUser().getId();
    }

    private static User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static void checkUserPermission(Permission.PermissionEnum permissionEnum){
        User loggedUser = getLoggedUser();
        if(loggedUser.getMainRole().getPermissions().stream().noneMatch(permission -> permission.getName() == permissionEnum || permission.getName() == ALLOW_ALL)){
            throw new PermissionDeniedException();
        }
    }

    public static boolean isUserHavingPermission(Permission.PermissionEnum permissionEnum){
        return getLoggedUser().getMainRole().getPermissions().stream().anyMatch(permission -> permission.getName() == permissionEnum || permission.getName() == ALLOW_ALL);
    }

    public static void checkIfUserIsPartOfOrganization(long organizationId){
        User loggedUser = getLoggedUser();
        if(Objects.isNull(loggedUser.getOrganization()) || loggedUser.getOrganization().getOrgId() != organizationId){
            throw new PermissionDeniedException();
        }
    }

    //todo: dodać to do wszystkich edpointów poniżej projektu: projects, tasks, taskgroup, comment
    public static void checkIfUserIsPartOfProject(Project project){
        if(project.getProjectUserList().stream()
                .noneMatch(projectUser ->
                        projectUser.getUser().getEmail().equals(getLoggedUser().getEmail()
                        )
                )){
            throw new PermissionDeniedException();
        }
    }
}
