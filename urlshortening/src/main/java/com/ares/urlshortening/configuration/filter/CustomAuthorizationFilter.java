package com.ares.urlshortening.configuration.filter;

import com.ares.urlshortening.configuration.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.ares.urlshortening.constants.Constants.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try{
            String token = getToken(request);
            Long userId = getUserId(request);
            if (tokenProvider.isTokenValid(userId,token)){
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
                Authentication authentication = tokenProvider.getAuthentication(userId,authorities,request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request,response);
        }catch (Exception e ){
            log.error(e.getMessage());
            //processError(request,request,e);
        }

    }

    private Long getUserId(HttpServletRequest request) {
        return tokenProvider.getSubject(getToken(request),request);
    }

    private String getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION)).filter(header-> header.startsWith(TOKEN_PREFIX))
                .map(token ->token.replace(TOKEN_PREFIX, EMPTY)).get();

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return  request.getHeader(AUTHORIZATION) == null ||
                !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                request.getMethod().equalsIgnoreCase(HTTP_METHOD_OPTIONS) ||
                asList(PUBLIC_ROUTES).contains(request.getRequestURI());

    }

}
