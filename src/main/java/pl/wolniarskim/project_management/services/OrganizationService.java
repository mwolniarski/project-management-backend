package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.EmailAlreadyTakenException;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.exceptions.TokenExpiredException;
import pl.wolniarskim.project_management.mappers.OrganizationMapper;
import pl.wolniarskim.project_management.mappers.UserMapper;
import pl.wolniarskim.project_management.models.DTO.OrganizationWriteModel;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;
import pl.wolniarskim.project_management.models.DTO.UserWriteModel;
import pl.wolniarskim.project_management.models.Organization;
import pl.wolniarskim.project_management.models.Role;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.OrganizationRepository;
import pl.wolniarskim.project_management.repositories.RoleRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.transaction.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Organization.OrgStatus.ACTIVE;
import static pl.wolniarskim.project_management.models.Organization.OrgStatus.DELETED;
import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUserId;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final UserService userService;
    public Organization createOrganization(Organization organization){
        organization.setOrgStatus(ACTIVE);
        return organizationRepository.save(organization);
    }

    public List<UserReadModel> getAllUsers(){

        SecurityUtil.checkUserPermission(ORGANIZATION_READ_USERS);

        return userRepository.getUserByOrganization_OrgId(SecurityUtil.getLoggedUser().getOrganization().getOrgId())
                .stream().map(UserMapper.INSTANCE::toReadModel)
                .collect(Collectors.toList());
    }

    public void updateOrganization(OrganizationWriteModel organization, long organizationId){

        SecurityUtil.checkIfUserIsPartOfOrganization(organizationId);
        SecurityUtil.checkUserPermission(ORGANIZATION_UPDATE);

        Organization mappedOrganization = organizationMapper.fromOrganizationWriteModel(organization);
        mappedOrganization.setOrgId(organizationId);
        organizationRepository.save(mappedOrganization);
    }

    public void createAccountForUser(UserWriteModel createdUser){
        SecurityUtil.checkUserPermission(ORGANIZATION_ADD_USER);

        Optional<Role> byId = roleRepository.findById(createdUser.getRoleId());

        if(byId.isEmpty()){
            //todo: exception bad request
            throw new TokenExpiredException();
        }

        SecurityUtil.checkIfUserIsPartOfOrganization(byId.get().getOrganization().getOrgId());

        Optional<User> byEmail = userRepository.findByEmail(createdUser.getEmail());

        if(byEmail.isPresent()){
            throw new EmailAlreadyTakenException();
        }

        String userPassword = UUID.randomUUID().toString();

        User user = new User();
        user.setFirstName(createdUser.getFirstName());
        user.setLastName(createdUser.getLastName());
        user.setEmail(createdUser.getEmail());
        user.setNick(createdUser.getEmail().substring(0, createdUser.getEmail().indexOf('@')));
        user.setEnabled(true);
        user.setMainRole(byId.get());
        user.setPassword(bCryptPasswordEncoder.encode(userPassword));
        user.setOrganization(getLoggedUser().getOrganization());
        userRepository.save(user);

        userService.createResetToken(createdUser.getEmail());
    }

    public void deleteAccountFromOrganization(String email){
        Optional<User> byEmail = userRepository.findByEmail(email);

        if(byEmail.isEmpty()){
            throw new TokenExpiredException();
        }
        if(byEmail.get().getEmail().equals(getLoggedUser().getEmail())){
            throw new PermissionDeniedException();
        }


        SecurityUtil.checkIfUserIsPartOfOrganization(byEmail.get().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(ORGANIZATION_DELETE_USER);

        userRepository.deleteById(byEmail.get().getId());
    }

    @Transactional
    public void removeOrganization(long organizationId){

        SecurityUtil.checkIfUserIsPartOfOrganization(organizationId);
        SecurityUtil.checkUserPermission(ORGANIZATION_DELETE);

        organizationRepository.findById(organizationId).ifPresent(organization -> {
            organization.setOrgStatus(DELETED);
            organizationRepository.save(organization);

            // do poprawy
            userRepository.getUserByOrganization_OrgId(organizationId).forEach(user -> {
                user.setEnabled(false);
                userRepository.save(user);
            });
        });
    }
}
