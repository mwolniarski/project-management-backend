package pl.wolniarskim.project_management.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.wolniarskim.project_management.models.DTO.TokensResponse;
import pl.wolniarskim.project_management.models.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationTime}")
    private long expirationTime;

    public JwtService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public TokensResponse createTokens(HttpServletRequest request, User user){
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(Algorithm.HMAC256(secret));
        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime * 20))
                .withIssuer(request.getRequestURL().toString())
                .sign(Algorithm.HMAC256(secret));
        return new TokensResponse(accessToken, refreshToken);
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
