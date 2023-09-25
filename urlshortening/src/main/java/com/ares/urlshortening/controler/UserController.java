package com.ares.urlshortening.controler;

import com.ares.urlshortening.configuration.provider.TokenProvider;
import com.ares.urlshortening.domain.*;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.enumeration.EventType;
import com.ares.urlshortening.event.NewUserEvent;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.forms.*;
import com.ares.urlshortening.service.EventService;
import com.ares.urlshortening.service.RoleService;
import com.ares.urlshortening.service.UserService;
import com.ares.urlshortening.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.ares.urlshortening.constants.Constants.TOKEN_PREFIX;
import static com.ares.urlshortening.enumeration.EventType.*;
import static com.ares.urlshortening.utils.ExceptionUtils.processError;
import static com.ares.urlshortening.utils.UserUtils.*;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

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
    private final ApplicationEventPublisher publisher;
    private final EventService eventService;


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
        UserDTO userDto=userService.getUserById(UserUtils.getAuthenticatedUser(authentication).getId());
        Role role = roleService.getRoleByUserId(userDto.getId());
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDto,"role",role,"userEvents",eventService.getEventsByUserId(userDto.getId())))
                        .message("Profile works")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm user, HttpServletRequest request) throws InterruptedException {
        UserDTO updatedUser=userService.updateUserDetails(user,tokenProvider.getSubject(getToken(request),request));
        publisher.publishEvent(new NewUserEvent(updatedUser.getId(), PROFILE_UPDATE));
        Role role = roleService.getRoleByUserId(updatedUser.getId());
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",updatedUser,"role",role,"userEvents",eventService.getEventsByUserId(updatedUser.getId())))
                        .message("User updated")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updateUserPassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form) throws InterruptedException {
        UserDTO userDTO = getAuthenticatedUser(authentication);
        userService.updateUserPassword(form,userDTO.getId());
        Role role = roleService.getRoleByUserId(userDTO.getId());
        publisher.publishEvent(new NewUserEvent(userDTO.getId(), PASSWORD_UPDATE));
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password updated successfully") // hmmmmmmmmm
                        .data(Map.of("user",userDTO,"role",role,"userEvents",eventService.getEventsByUserId(userDTO.getId())))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateUserSettings(@RequestBody @Valid SettingsForm form, Authentication authentication) throws InterruptedException {
        UserDTO updatedUser = userService.updateUserSettings(form,getAuthenticatedUser(authentication).getId());
        publisher.publishEvent(new NewUserEvent(updatedUser.getId(),ACCOUNT_SETTINGS_UPDATE));
        Role role = roleService.getRoleByUserId(updatedUser.getId());
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",updatedUser,"role",role,"userEvents",eventService.getEventsByUserId(updatedUser.getId())))
                        .message("User settings updated successfully")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) throws InterruptedException {
        UserDTO updatedUser = userService.toggleMfa(getAuthenticatedUser(authentication).getId());
        publisher.publishEvent(new NewUserEvent(updatedUser.getId(),MFA_UPDATE));
        Role role = roleService.getRoleByUserId(updatedUser.getId());
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",updatedUser,"role",role,"userEvents",eventService.getEventsByUserId(updatedUser.getId())))
                        .message("MFA updated")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image")MultipartFile image) throws InterruptedException {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updateImage(user,image);
        Role role = roleService.getRoleByUserId(user.getId());
        publisher.publishEvent(new NewUserEvent(user.getId(), PROFILE_PICTURE_UPDATE));
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userService.getUserById(user.getId()),"role",role,"userEvents",eventService.getEventsByUserId(user.getId())))
                        .message("Image updated")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email){

        userService.resetPassword(email);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent, check your email to reset your password.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
    @GetMapping(value = "/image/{fileName}",produces = IMAGE_PNG_VALUE)
    public byte[] getImage(@PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes
                (Paths.get(System.getProperty("user.home")+"/Downloads/urlShorteningImages/"+fileName));
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key){
        UserDTO userDTO =  userService.verifyPasswordKey(key);
        return ResponseEntity.ok(
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
            return ResponseEntity.ok(
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
        UserDTO userDTO = authenticate(loginForm.getEmail(), loginForm.getPassword());
        return userDTO.isUsingMfa()? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCodeFor2FA(@PathVariable("email") String email,@PathVariable("code") String code){
        UserDTO userDto = userService.verifyCode(email,code);
        publisher.publishEvent(new NewUserEvent(userDto.getId(),LOGIN_ATTEMPT_SUCCESS));
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

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key){
        UserDTO userDto = userService.verifyAccountKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userDto.isEnabled()? "Account is already verified.":"Account verified.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request){
        log.info(request.getHeader(AUTHORIZATION));
        if (isHeaderAndTokenValid(request)){
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO userDTO = userService.getUserById(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(of("user", userDTO,
                                    "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                    "refresh_token", token))
                            .message("Token refreshed")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());
        }
        else {
            return new ResponseEntity<>(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .reason("Refresh token missing or invalid")
                            .status(BAD_REQUEST)
                            .statusCode(BAD_REQUEST.value())
                            .build(), BAD_REQUEST);
        }
    }



    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request){
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a "+ request.getMethod() + " request on this path")
                        .status(HttpStatus.NOT_FOUND)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build(), NOT_FOUND);
    }



    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        if (request.getHeader(AUTHORIZATION)== null ||
                !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)){
            return false;
        }
        String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
        Long userId = tokenProvider.getSubject(token,request);
        return tokenProvider.isTokenValid(userId,token);
    }


    private UserDTO authenticate(String email, String password) {
        UserDTO user = userService.getUserByEmail(email);
        try{
            if (user!=null){
                publisher.publishEvent(new NewUserEvent
                        (user.getId(), LOGIN_ATTEMPT));
            }
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
            UserDTO loggedInUser = getLoggedInUser(authentication);
            if (!loggedInUser.isUsingMfa()){
                publisher.publishEvent(new NewUserEvent
                        (loggedInUser.getId(), LOGIN_ATTEMPT_SUCCESS));
            }
            return loggedInUser;
        }catch (Exception e){
            if (user!=null){
                publisher.publishEvent(new NewUserEvent
                        (user.getId(), LOGIN_ATTEMPT_FAILURE));
            }
            //processError(request,response,e);
            throw new ApiException("Bad credentials!");
        }
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("user/get/<userId>").toUriString());
    }


    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        log.info(userDTO.toString());

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

    private String getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION)).filter(header-> header.startsWith(TOKEN_PREFIX))
                .map(token ->token.replace(TOKEN_PREFIX, EMPTY)).get();
    }

}
