package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.EmailAlreadyConfirmedException;
import pl.wolniarskim.project_management.exceptions.TokenExpiredException;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    @Value("${enable-email-confirmation}")
    private boolean enableEmailConfirmation;

    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;
    private final RoleService roleService;
    private final OrganizationService organizationService;

    public ResponseEntity<Void> register(RegistrationRequest request){
        Organization organization = new Organization();
        organization.setName(request.getOrganizationName());
        Organization savedOrganization = organizationService.createOrganization(organization);

        Role superAdminRole = roleService.addDefaultRoles(savedOrganization);

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail().substring(0, request.getEmail().indexOf('@')),
                request.getEmail(),
                request.getPassword(),
                superAdminRole
        );
        user.setOrganization(savedOrganization);

        String token = userService.signUpUser(user);
        if(!enableEmailConfirmation){
            userService.enableUser(request.getEmail());
            return ResponseEntity.ok().build();
        }
        emailService.sendConfirmation(request.getEmail(), token);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token);
        if(confirmationToken.getConfirmedAt() != null)
            throw new EmailAlreadyConfirmedException();
        LocalDateTime expiredAt = confirmationToken.getExpiredAt();

        if(expiredAt.isBefore(LocalDateTime.now()))
            throw new TokenExpiredException();

        confirmationTokenService.setConfirmed(token);
        userService.enableUser(confirmationToken.getUser().getEmail());

        return "confirmed";
    }
}
