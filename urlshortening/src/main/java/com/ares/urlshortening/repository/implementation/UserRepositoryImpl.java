package com.ares.urlshortening.repository.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.enumeration.VerificationType;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.forms.SettingsForm;
import com.ares.urlshortening.forms.UpdateForm;
import com.ares.urlshortening.forms.UpdatePasswordForm;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.repository.UserRepository;
import com.ares.urlshortening.rowmapper.UserRowMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.ares.urlshortening.enumeration.RoleType.ROLE_USER;
import static com.ares.urlshortening.enumeration.VerificationType.ACCOUNT;
import static com.ares.urlshortening.enumeration.VerificationType.PASSWORD;
import static com.ares.urlshortening.query.UserQuery.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HttpServletResponse response;
    private final HttpServletRequest request;


    @Override
    public User create(User user) throws ApiException {
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0) {
            throw new ApiException("Email already in use. Please use a different email.");
        }
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, requireNonNull(parameters), holder);
            user.setId(requireNonNull(holder.getKey()).longValue());
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

            jdbc.update(INSERT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), ("url"), verificationUrl));

            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(),verificationUrl,ACCOUNT);

            user.setEnabled(false);
            user.setNotLocked(true);

            return user;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }

    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, Map.of("userId", id), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error("No user found with given id");
            throw new ApiException("No user found with given id");
        } catch (Exception e) {
            log.error("An Error occurred");
            throw new ApiException("An Error occurred. Please try again.");
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            log.error("No user was found with the entered email.");
            throw new UsernameNotFoundException("No user was found with the entered email.");
        } else {
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error("Bad credentials!");
            throw new ApiException("Bad credentials!");
        } catch (Exception e) {
            log.error("An Error occurred");
            throw new ApiException("An Error occurred. Please try again.");
        }
    }

    @Override
    public void sendVerificationCode(UserDTO userDTO) {
        //String expirationDate = DateFormatUtils.format(addMinutes(new Date(), 15), DATE_FORMAT);
        String verificationCode = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
        try {
            jdbc.update(DELETE_2FA_BY_USER_ID_QUERY, Map.of("userId", userDTO.getId()));
            jdbc.update(INSERT_INTO_2FA_QUERY, Map.of("userId", userDTO.getId(), "code", verificationCode));
            //sendSms(user.getPhone(), "From: Url shortening \nVerification code\n"+verificationCode);
            log.info("Verification code is :" + verificationCode);
        } catch (Exception e) {
            log.error("An Error occurred in sendVerificationCode()");
            throw new ApiException("An Error occurred. Please try again.");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if (isVerificationCodeExpired(code)) {
            throw new ApiException("Code has expired, Please renew your code!");
        }
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY,
                    Map.of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY,
                    Map.of("email", email), new UserRowMapper());
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_2FA_BY_USER_ID_QUERY, Map.of("userId", userByCode.getId()));
                return userByEmail;
            } else {
                throw new ApiException("Code is not valid!");
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("An Error occurred in sendVerificationCode()");
            throw new ApiException("Could not find a record!");
        } catch (Exception e) {
            log.error("An Error occurred in sendVerificationCode()");
            throw new ApiException("An error occurred, Please try again");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) <= 0) {
            //processError(request,response,new ApiException("There is no account for the given email address."));
            throw new ApiException("There is no account for the given email address.");
        }
        try {
            //String expirationDate = DateFormatUtils.format(addMinutes(new Date(), 30), DATE_FORMAT);
            User user = getUserByEmail(email);
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
            jdbc.update(DELETE_RESETPASSWORD_BY_USER_ID_QUERY, Map.of("userId", user.getId()));
            jdbc.update(INSERT_INTO_RESETPASSWORD_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
            //sendSms(user.getPhone(), "From: Url shortening \nVerification code\n"+verificationCode);
            log.info("Verification URL: " + verificationUrl);
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if (isLinkExpired(key, PASSWORD)) {
            throw new ApiException("Link has expired. Please reset your password again");
        } else {
            try {
                User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
                return user;
            } catch (EmptyResultDataAccessException e) {
                log.error("in isLinkExpired() " + e.getMessage());
                throw new ApiException("This link is not valid. Please reset your password again.");
            } catch (Exception exception) {
                throw new ApiException("An error occurred. Please try again.");
            }
        }
    }

    @Override
    public void renewPassword(String key, ResetPasswordForm form) {
        if (!(form.getPassword().equals(form.getConfirmPassword()))) {
            throw new ApiException("Passwords do not match. Please try again.");
        }
        try {
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, Map.of(
                    "url", getVerificationUrl(key, PASSWORD.getType()),
                    "newPassword",passwordEncoder.encode(form.getPassword())
                    ));
            jdbc.update(DELETE_RESETPASSWORD_BY_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())));

        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyAccountKey(String key) {
        try{
            User user = jdbc.queryForObject(SELECT_USER_BY_VERIFY_URL_QUERY, Map.of("url",getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLE_ACCOUNT_QUERY, Map.of("enabled",true,"userId", requireNonNull(user).getId()));
            return user;
        }catch (EmptyResultDataAccessException e ){
            throw new ApiException("This is not a valid link.");
        }catch (Exception e ){
            throw new ApiException("An unknown error occurred, Please try again");
        }
    }

    @Override
    public User updateUserDetails(UpdateForm user, Long id) {
        try{
            SqlParameterSource params = getUserDetailsSqlParameterSource(user,id);
            jdbc.update(UPDATE_USER_DETAILS_QUERY, requireNonNull(params));
            return get(id);
        }catch (EmptyResultDataAccessException e ){
            throw new ApiException("No user found, the request wasn't valid");
        }catch (Exception e ){
            log.error(e.getMessage());
            throw new ApiException("An unknown error occurred, Please try again");
        }
    }

    @Override
    public void updateUserPassword(UpdatePasswordForm form, Long userId) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new ApiException("Passwords do not match");
        }
        User user = get(userId);
        if (passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            try {

                jdbc.update(UPDATE_USER_PASSWORD_QUERY, Map.of("userId",userId,"newPassword",passwordEncoder.encode(form.getNewPassword())));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new ApiException("An unknown error occurred, Please try again");
            }
        }else {
            throw new ApiException("Incorrect current password, please try again");
        }
    }

    @Override
    public User updateUserSettings(SettingsForm form, Long userId) {
        try {
            jdbc.update(UPDATE_USER_SETTINGS_QUERY, Map.of("userId",userId,"enabled",form.getEnabled(),"notLocked", form.getNotLocked()));
            return get(userId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An unknown error occurred, Please try again");
        }
    }

    @Override
    public User toggleMfa(Long id) {
        User user = get(id);
        if (isBlank(user.getPhone())){
            throw new ApiException("You need to add a phone number to enable Multi-factor authentication");
        }else{
            user.setUsingMfa(!user.isUsingMfa());
            try {
                jdbc.update(UPDATE_USER_MFA_QUERY, Map.of("userId",id,"isUsingMfa",user.isUsingMfa()));
                return get(id);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new ApiException("An unknown error occurred, Please try again");
            }
        }
    }

    @Override
    public void updateImage(UserDTO user, MultipartFile image) {
        user.setImageUrl(setUserImageUrl(user.getEmail()));
        saveImage(user.getEmail(),image);
        jdbc.update(UPDATE_USER_IMAGE_URL_QUERY, Map.of("imageUrl",user.getImageUrl(),"userId",user.getId()));
    }

    private void saveImage(String email, MultipartFile image) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/urlShorteningImages/").toAbsolutePath().normalize();
        if (!Files.exists(fileStorageLocation)){
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                log.error("Unable to create the specified directory!");
                throw new ApiException(e.getMessage());
            }
            log.info("Created directories "+fileStorageLocation);
        }
        try{
            Files.copy(image.getInputStream(),fileStorageLocation.resolve(email+".png"),REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }
        log.info("New profile image saved!");
    }

    private String setUserImageUrl(String email) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/"+ email+".png").toUriString();
    }


    private boolean isLinkExpired(String key, VerificationType verificationType) {
        try {
            return jdbc.queryForObject(IS_PASSWORD_RESET_CODE_EXPIRED_BY_URL, Map.of("url", getVerificationUrl(key, PASSWORD.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            log.error("in isLinkExpired() " + e.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    private boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(IS_VERIFICATION_CODE_EXPIRED_BY_CODE, Map.of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            log.error("Wrong code");
            throw new ApiException("That is not an existing code!");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }


    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("user/verify/" + type + "/" + key).toUriString();
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword()));
    }

    private SqlParameterSource getUserDetailsSqlParameterSource(UpdateForm user,Long id) {
        return new MapSqlParameterSource()
                .addValue("userId",id)
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone",user.getPhone())
                .addValue("bio",user.getBio());
    }


}

