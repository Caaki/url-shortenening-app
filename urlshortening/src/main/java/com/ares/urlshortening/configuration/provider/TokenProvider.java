package com.ares.urlshortening.configuration.provider;

import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.relational.core.sql.In;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.ares.urlshortening.constants.Constants.*;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final UserService userService;

    @Value("${jwt.secret}")
    private String secret;

    public String createAccessToken(UserPrincipal userPrincipal){
        String[] claims = getClaimsFromUser(userPrincipal);

        return JWT.create().withIssuer(URL_SHORTENING).withAudience(MOTO)
                .withIssuedAt(new Date())
                .withSubject(String.valueOf(userPrincipal.getUser().getId()))
                .withArrayClaim(AUTHORITIES,claims)
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

public String createRefreshToken(UserPrincipal userPrincipal){
        String [] claim = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(URL_SHORTENING)
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME ))
                .withSubject(userPrincipal.getUser().getId().toString())
                .withIssuedAt(new Date())
                .withAudience(MOTO)
                .sign(Algorithm.HMAC512(secret.getBytes()));
}

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }


    private JWTVerifier getJwtVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(URL_SHORTENING).build();
        }catch (JWTVerificationException e){
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJwtVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    public List<GrantedAuthority> getAuthorities(String token){
        String [] claims = getClaimsFromToken(token);
        return stream(claims).map(String::new).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }


    public Authentication getAuthentication(Long userId, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(userService.getUserById(userId),null, authorities);
        usernamePasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthToken;
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    public boolean isTokenValid(Long userId, String token){
        JWTVerifier verifier = getJwtVerifier();
        return !Objects.isNull(userId) && !isTokenExpired(verifier, token);
    }

    public Long getSubject(String token, HttpServletRequest request){
        try{
            return Long.valueOf(getJwtVerifier().verify(token).getSubject());
        }catch (TokenExpiredException e){
            request.setAttribute("expiredMessage",e.getMessage());
            throw e;
        }catch (InvalidClaimException e){
            request.setAttribute("invalidClaim",e.getMessage());
            throw e;
        }catch (Exception e){
            throw e;
        }
    }


}
