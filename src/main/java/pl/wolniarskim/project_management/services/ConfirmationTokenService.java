package pl.wolniarskim.project_management.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.models.ConfirmationToken;
import pl.wolniarskim.project_management.repositories.ConfirmationTokenRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token){
        confirmationTokenRepository.save(token);
    }

    public ConfirmationToken getToken(String token){
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));
    }

    public void setConfirmed(String token){
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
    }
}
