package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.mappers.RoleMapper;
import pl.wolniarskim.project_management.models.DTO.RoleReadModel;
import pl.wolniarskim.project_management.models.DTO.RoleWriteModel;
import pl.wolniarskim.project_management.models.Organization;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.models.Role;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.PermissionRepository;
import pl.wolniarskim.project_management.repositories.RoleRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;


@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    private static final List<Permission> DEFAULT_USER_PERMISSION = new ArrayList<>(Arrays.asList(
            new Permission(TASK_EDIT_COMMENT),
            new Permission(TIME_ENTRY_REMOVE),
            new Permission(TASK_HISTORY_READ),
            new Permission(TIME_ENTRY_ADD),
            new Permission(TASK_ADD_COMMENT),
            new Permission(TASK_UPDATE),
            new Permission(PROJECT_READ)
    ));
    private static final List<Permission> DEFAULT_MANAGER_PERMISSION = new ArrayList<>(Arrays.asList(
            new Permission(TASK_DELETE),
            new Permission(TASK_GROUP_DELETE),
            new Permission(TASK_CREATE),
            new Permission(PROJECT_ADD_USER),
            new Permission(PROJECT_REMOVE_USER),
            new Permission(TIME_ENTRY_READ_ALL),
            new Permission(TASK_GROUP_CREATE),
            new Permission(ORGANIZATION_READ_USERS)
    ));
    private static final List<Permission> DEFAULT_ADMIN_PERMISSION = new ArrayList<>(Arrays.asList(
            new Permission(PROJECT_CREATE),
            new Permission(ORGANIZATION_ADD_USER),
            new Permission(ROLE_CREATE)
    ));
    private static final List<Permission> DEFAULT_SUPER_ADMIN_PERMISSION = new ArrayList<>(Arrays.asList(
            new Permission(ORGANIZATION_UPDATE),
            new Permission(ORGANIZATION_DELETE),
            new Permission(ALLOW_ALL)
    ));

    static {
        DEFAULT_MANAGER_PERMISSION.addAll(DEFAULT_USER_PERMISSION);
        DEFAULT_ADMIN_PERMISSION.addAll(DEFAULT_MANAGER_PERMISSION);
        DEFAULT_SUPER_ADMIN_PERMISSION.addAll(DEFAULT_ADMIN_PERMISSION);
    }

    public RoleReadModel addRole(RoleWriteModel roleWriteModel){

        User loggedUser = getLoggedUser();

        SecurityUtil.checkUserPermission(ROLE_CREATE);

        Role role = new Role();
        role.setName(roleWriteModel.getName());
        role.setOrganization(loggedUser.getOrganization());
        role.setPermissions(roleWriteModel.getPermissions());
        return roleMapper.fromRole(roleRepository.save(role));
    }

    public void deleteRole(long roleId){
        Optional<Role> byId = roleRepository.findById(roleId);
        if(byId.isEmpty()){
            throw new PermissionDeniedException();
        }
        SecurityUtil.checkIfUserIsPartOfOrganization(byId.get().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(ROLE_DELETE);
        roleRepository.deleteById(roleId);
    }

    public Role addDefaultRoles(Organization organization){
        Role manager = new Role("MANAGER", DEFAULT_MANAGER_PERMISSION);
        Role user = new Role("USER", DEFAULT_USER_PERMISSION);
        Role admin = new Role("ADMIN", DEFAULT_ADMIN_PERMISSION);
        Role superAdmin = new Role("SUPER_ADMIN", DEFAULT_SUPER_ADMIN_PERMISSION);
        manager.setOrganization(organization);
        user.setOrganization(organization);
        admin.setOrganization(organization);
        superAdmin.setOrganization(organization);

        roleRepository.saveAll(List.of(user, manager, admin));
        return roleRepository.save(superAdmin);
    }

    public List<Permission> getAllPermissions(){
        return permissionRepository.findAll();
    }

    public List<RoleReadModel> getAllRoles(){
        return roleRepository.findAllByOrganization_OrgId(getLoggedUser().getOrganization().getOrgId()).stream()
                .map(roleMapper::fromRole)
                .collect(Collectors.toList());
    }
}
