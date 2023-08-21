package pl.wolniarskim.project_management.services;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.wolniarskim.project_management.exceptions.EmailAlreadyTakenException;
import pl.wolniarskim.project_management.exceptions.PermissionDeniedException;
import pl.wolniarskim.project_management.exceptions.TokenExpiredException;
import pl.wolniarskim.project_management.mappers.OrganizationMapper;
import pl.wolniarskim.project_management.models.ConfirmationToken;
import pl.wolniarskim.project_management.models.DTO.ProfileDetails;
import pl.wolniarskim.project_management.models.DTO.ProfileDetailsWriteModel;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.models.ResetPasswordToken;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.ResetPasswordTokenRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;


@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND = "User with email %s not found";
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OrganizationMapper organizationMapper;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(USER_NOT_FOUND, email)
                ));
    }

    @Transactional
    public String signUpUser(User user){
        Optional<User> userFromRepo = userRepository.findByEmail(user.getEmail());
        if(userFromRepo.isPresent()){
            if(userFromRepo.get().isEnabled())
                throw new EmailAlreadyTakenException();
            else{
                user = userFromRepo.get();
            }
        }
        else{
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }


        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    @Transactional
    public void changePassword(String newPassword, String resetPasswordToken) {
        Optional<ResetPasswordToken> byToken = resetPasswordTokenRepository.findByToken(resetPasswordToken);
        if(byToken.isEmpty()){
            throw new PermissionDeniedException();
        }

        ResetPasswordToken resetToken = byToken.get();
        if(LocalDateTime.now().isAfter(resetToken.getExpiredAt())){
            throw new TokenExpiredException();
        }

        User user = resetToken.getUser();
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        resetPasswordTokenRepository.save(resetToken);
    }

    public void createResetToken(String email){
        Optional<User> byEmail = userRepository.findByEmail(email);
        if(byEmail.isPresent()){
            ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
            resetPasswordToken.setToken(UUID.randomUUID().toString());
            resetPasswordToken.setCreatedAt(LocalDateTime.now());
            resetPasswordToken.setExpiredAt(LocalDateTime.now().plusHours(1));
            resetPasswordToken.setUser(byEmail.get());
            resetPasswordTokenRepository.save(resetPasswordToken);

            emailService.sendResetPassword(email, resetPasswordToken.getToken());
        }
    }

    public void enableUser(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public ProfileDetails uploadProfileImage(MultipartFile file) throws IOException {
        User loggedUser = getLoggedUser();
        loggedUser.setProfileImage(Base64.getEncoder().encodeToString(file.getBytes()));

        userRepository.save(loggedUser);

        return ProfileDetails.builder()
                .profileImage(loggedUser.getProfileImage())
                .lastName(loggedUser.getLastName())
                .firstName(loggedUser.getFirstName())
                .nick(loggedUser.getNick())
                .build();
    }

    public ProfileDetails getProfileDetails() {
        User loggedUser = getLoggedUser();

        ProfileDetails.ProfileDetailsBuilder detailsBuilder = ProfileDetails.builder()
                .profileImage(loggedUser.getProfileImage())
                .lastName(loggedUser.getLastName())
                .firstName(loggedUser.getFirstName())
                .nick(loggedUser.getNick())
                .permissions(loggedUser.getMainRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toList()));

        if(Objects.nonNull(loggedUser.getOrganization())){
            detailsBuilder.organization(organizationMapper.toOrganizationReadModel(loggedUser.getOrganization()));
        }

        return detailsBuilder.build();
    }

    public void saveUserProfile(ProfileDetailsWriteModel profileDetailsWriteModel) {
        User loggedUser = getLoggedUser();

        loggedUser.setFirstName(profileDetailsWriteModel.getFirstName());
        loggedUser.setLastName(profileDetailsWriteModel.getLastName());

        userRepository.save(loggedUser);
    }

}
