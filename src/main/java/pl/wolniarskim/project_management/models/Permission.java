package pl.wolniarskim.project_management.models;

import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    private String name;

    public PermissionEnum getName() {
        return PermissionEnum.valueOf(name);
    }

    public void setName(PermissionEnum permissionEnum) {
        this.name = permissionEnum.name();
    }

    public Permission(PermissionEnum permissionEnum){
        this.name = permissionEnum.name();
    }

    public enum PermissionEnum{
        ORGANIZATION_DELETE,
        ORGANIZATION_UPDATE,
        ROLE_CREATE,
        ROLE_DELETE,
        ALLOW_ALL,
        TASK_UPDATE,
        TASK_DELETE,
        TASK_CREATE,
        TASK_GROUP_CREATE,
        TASK_GROUP_DELETE,
        TASK_GROUP_UPDATE,
        PROJECT_CREATE,
        PROJECT_DELETE,
        PROJECT_UPDATE,
        PROJECT_READ,
        PROJECT_ADD_USER,
        PROJECT_REMOVE_USER,
        ORGANIZATION_ADD_USER,
        ORGANIZATION_READ_USERS,
        ORGANIZATION_DELETE_USER,
        TASK_ADD_COMMENT,
        TASK_EDIT_COMMENT,
        TASK_DELETE_COMMENT,
        TIME_ENTRY_ADD,
        TIME_ENTRY_REMOVE,
        TIME_ENTRY_READ_ALL,
        TASK_HISTORY_READ
    }
}
