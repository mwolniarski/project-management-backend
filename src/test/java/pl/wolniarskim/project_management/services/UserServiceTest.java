package pl.wolniarskim.project_management.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.wolniarskim.project_management.repositories.UserRepository;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
class UserServiceTest {

    @Mock
    ConfirmationTokenService confirmationTokenService;

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void shouldThrowIllegalStateExceptionWhileEnableUserWhichIsNotPresentInRepo() {
        //given
        MockitoAnnotations.initMocks(this);
        Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(Optional.empty());
        //when + then
        Assertions.assertThrows(IllegalStateException.class, () -> userService.enableUser(""));
    }
}