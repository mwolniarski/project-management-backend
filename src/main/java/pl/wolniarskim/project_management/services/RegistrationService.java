package pl.wolniarskim.project_management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.exceptions.EmailAlreadyConfirmedException;
import pl.wolniarskim.project_management.exceptions.TokenExpiredException;
import pl.wolniarskim.project_management.models.ConfirmationToken;
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.models.UserRole;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
public class RegistrationService {

    @Value("${enable-email-confirmation}")
    boolean enableEmailConfirmation;

    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public RegistrationService(UserService userService, ConfirmationTokenService confirmationTokenService, EmailService emailService) {
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    public String register(RegistrationRequest request){
        String token = userService.signUpUser(
                new User(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getEmail().equals("wolniar5@gmail.com") ? UserRole.ROLE_USER : UserRole.ROLE_ADMIN
                )
        );
        if(!enableEmailConfirmation){
            userService.enableUser(request.getEmail());
            return "Account enabled!";
        }
        emailService.sendConfirmation(request.getEmail(), token);
        return token;
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
