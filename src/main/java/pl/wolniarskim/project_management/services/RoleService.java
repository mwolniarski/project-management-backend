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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.ROLE_CREATE;
import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.ROLE_DELETE;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;


@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

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
        Role manager = new Role("MANAGER", Arrays.asList(new Permission(Permission.PermissionEnum.ALLOW_ALL)));
        Role user = new Role("USER", Arrays.asList(new Permission(Permission.PermissionEnum.ALLOW_ALL)));
        Role superAdmin = new Role("SUPER_ADMIN", Arrays.asList(new Permission(Permission.PermissionEnum.ALLOW_ALL)));
        manager.setOrganization(organization);
        user.setOrganization(organization);
        superAdmin.setOrganization(organization);

        roleRepository.saveAll(List.of(user, manager));
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
