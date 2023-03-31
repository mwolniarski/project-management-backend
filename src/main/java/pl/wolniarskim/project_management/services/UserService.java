package pl.wolniarskim.project_management.services;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.wolniarskim.project_management.exceptions.EmailAlreadyTakenException;
import pl.wolniarskim.project_management.models.ConfirmationToken;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.models.UserProfileImage;
import pl.wolniarskim.project_management.repositories.UserProfileImageRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.utils.ImageUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUser;


@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND = "User with email %s not found";
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProfileImageRepository userProfileImageRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(USER_NOT_FOUND, email)
                ));
    }

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

    public void enableUser(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void uploadProfileImage(MultipartFile file) throws IOException {
        User loggedUser = getLoggedUser();
        UserProfileImage userProfileImage = new UserProfileImage();
        userProfileImage.setUser(loggedUser);
        userProfileImage.setImageData(ImageUtil.compressImage(file.getBytes()));

        userProfileImageRepository.save(userProfileImage);
    }

    public byte[] getUserProfileImage() {
        User loggedUser = getLoggedUser();

        Optional<UserProfileImage> dbImage = userProfileImageRepository.findUserProfileImageByUser(loggedUser);
        byte[] image = new byte[1];
        if(dbImage.isPresent()){
            image = ImageUtil.decompressImage(dbImage.get().getImageData());
        }
        return image;
    }
}
