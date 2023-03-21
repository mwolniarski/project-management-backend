package pl.wolniarskim.project_management.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokensResponse {

    private String accessToken;
    private String refreshToken;
}
