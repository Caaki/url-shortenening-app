package com.ares.urlshortening.utils;



import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.dto.UserDTO;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static UserDTO getAuthenticatedUser(Authentication authentication){
        return ((UserDTO) authentication.getPrincipal());
    }
    public static UserDTO getLoggedInUser(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
