package com.ares.urlshortening.controler;

import com.ares.urlshortening.configuration.provider.TokenProvider;
import com.ares.urlshortening.domain.HttpResponse;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.forms.LoginForm;
import com.ares.urlshortening.service.RoleService;
import com.ares.urlshortening.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.ares.urlshortening.utils.UserUtils.getAuthenticatedUser;
import static java.time.LocalDateTime.now;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final RoleService roleService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;


    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){
       UserDTO userDto = userService.createUser(user);
       return ResponseEntity.created(getUri()).body(
               HttpResponse.builder()
                       .timeStamp(now().toString())
                       .data(Map.of("user",userDto))
                       .message("New user created")
                       .status(HttpStatus.CREATED)
                       .statusCode(HttpStatus.CREATED.value())
                       .build()
       );
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> saveUser(Authentication authentication){
        System.out.println(authentication);
        UserDTO userDto = userService.getUserById(Long.valueOf(authentication.getName()));
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDto))
                        .message("Profile works")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
        UserDTO userDTO = userService.getUserByEmail(loginForm.getEmail());
        return userDTO.isUsingMfa()? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email,@PathVariable("code") String code){
        UserDTO userDto = userService.verifyCode(email,code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Login successfully")
                        .data(Map.of("user", userDto,
                                "access_token",tokenProvider.createAccessToken(getUserPrincipal(userDto)),
                                "refresh_token",tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }


    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("user/get/<userId>").toUriString());
    }


    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Verification code sent")
                        .data(Map.of("user", userDTO))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );

    }
    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Login successfully")
                        .data(Map.of("user", userDTO,
                                "access_token",tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "refresh_token",tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private UserPrincipal getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipal(
                UserDTOMapper.fromDTO(userService.getUserById(userDTO.getId())),
                roleService.getRoleByUserId(userDTO.getId()).getPermissions()
        );
    }
}
