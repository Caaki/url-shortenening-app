package com.ares.urlshortening.configuration.security;

import com.ares.urlshortening.configuration.filter.CustomAuthorizationFilter;
import com.ares.urlshortening.handler.CustomAccessDeniedHandler;
import com.ares.urlshortening.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.ares.urlshortening.constants.Constants.PUBLIC_URLS;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final BCryptPasswordEncoder encoder;
    private final CustomAccessDeniedHandler deniedHandler;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorizationFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(request -> request.requestMatchers(PUBLIC_URLS).permitAll());
        http.authorizeHttpRequests(request-> request.requestMatchers(HttpMethod.DELETE,"/user/delete/**").hasAuthority("DELETE:USER"));
        http.authorizeHttpRequests(request-> request.requestMatchers(HttpMethod.DELETE,"/url/delete/**").hasAuthority("DELETE:URL"));
        http.exceptionHandling(exception ->exception.accessDeniedHandler(deniedHandler).authenticationEntryPoint(entryPoint));
        http.authorizeHttpRequests(request -> request.anyRequest().authenticated());
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(encoder);
        return new ProviderManager(authenticationProvider);

    }

}
