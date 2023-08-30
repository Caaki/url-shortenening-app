package com.ares.urlshortening.controler;

import com.ares.urlshortening.configuration.provider.TokenProvider;
import com.ares.urlshortening.domain.HttpResponse;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.forms.LoginForm;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.service.RoleService;
import com.ares.urlshortening.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static com.ares.urlshortening.utils.ExceptionUtils.processError;
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
    private final HttpServletRequest request;
    private final HttpServletResponse response;


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
        System.out.println(authentication.getPrincipal());
        System.out.println(authentication.getName());
        UserDTO userDto = userService.getUserById(Long.valueOf(authentication.getName()));
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDto))
                        .message("Profile works")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email){

        userService.resetPassword(email);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent, check your email to reset your password.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key){
        UserDTO userDTO =  userService.verifyPasswordKey(key);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Please enter a new password.")
                        .status(HttpStatus.OK)
                        .data(Map.of("user", userDTO))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/resetpassword/{key}")
    public ResponseEntity<HttpResponse> renewPassword(@PathVariable("key") String key, @RequestBody ResetPasswordForm form){
            userService.renewPassword(key,form);
            return ResponseEntity.created(getUri()).body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Password has been reset")
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            );
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        Authentication authentication = authenticate(loginForm.getEmail(), loginForm.getPassword());
        UserDTO userDTO = getAuthenticatedUsed(authentication);
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

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request){

        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a "+ request.getMethod() + " request on this path")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()
        );
    }


    private UserDTO getAuthenticatedUsed(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    private Authentication authenticate(String email, String password) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
            return authentication;
        }catch (Exception e){
            processError(request,response,e);
            throw new ApiException(e.getMessage());
        }
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
                roleService.getRoleByUserId(userDTO.getId())
        );
    }
}
