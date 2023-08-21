package pl.wolniarskim.project_management.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.models.DTO.TokensResponse;
import pl.wolniarskim.project_management.models.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String ROLE_CLAIM_NAME = "roles";
    private static final String ORG_ID_CLAIM_NAME = "orgId";
    private final UserDetailsService userDetailsService;
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationTime}")
    private long expirationTime;

    public TokensResponse createTokens(HttpServletRequest request, User user){
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withIssuer(request.getRequestURL().toString())
                .sign(Algorithm.HMAC256(secret));
        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime * 20))
                .withIssuer(request.getRequestURL().toString())
                .sign(Algorithm.HMAC256(secret));
        return new TokensResponse(accessToken,
                expirationTime,
                refreshToken,
                (expirationTime * 20));
    }

    public TokensResponse refreshTokens(HttpServletRequest request) throws IOException {
        String authorization = request.getHeader(TOKEN_HEADER);
        if (authorization != null && authorization.startsWith(TOKEN_PREFIX)) {
            String userName = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(authorization.replace(TOKEN_PREFIX, ""))
                    .getSubject();
            if (userName != null) {
                User user = (User) userDetailsService.loadUserByUsername(userName);
                return createTokens(request, user);
            }
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken verifyToken(HttpServletRequest request){
        String token = request.getHeader(TOKEN_HEADER);
        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            String userName = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();
            if (userName != null) {
                User user = (User) userDetailsService.loadUserByUsername(userName);
                return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            }
        }
        return null;
    }
}
