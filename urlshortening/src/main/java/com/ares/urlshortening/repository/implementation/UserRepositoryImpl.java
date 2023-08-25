package com.ares.urlshortening.repository.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.UserPrincipal;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.repository.UserRepository;
import com.ares.urlshortening.rowmapper.UserRowMapper;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

import static com.ares.urlshortening.enumeration.RoleType.ROLE_USER;
import static com.ares.urlshortening.enumeration.VerificationType.ACCOUNT;
import static com.ares.urlshortening.query.UserQuery.*;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;


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
        }catch (EmptyResultDataAccessException e){
            log.error("No user found with given id");
            throw new ApiException("No user found with given id");
        }catch (Exception e){
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
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()).getPermissions());
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
        if(isVerificationCodeExpired(code)){
            throw new ApiException("Code has expired, Please renew your code!");
        }
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY,
                    Map.of("code",code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY,
                    Map.of("email",email), new UserRowMapper());
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())){
                jdbc.update(DELETE_2FA_BY_USER_ID_QUERY, Map.of("userId", userByCode.getId()));
                return userByEmail;
            }else{
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

    private boolean isVerificationCodeExpired(String code) {
        try{
            return jdbc.queryForObject(IS_VERIFICATION_CODE_EXPIRED_BY_CODE, Map.of("code", code), Boolean.class);
        }catch (EmptyResultDataAccessException e){
            log.error("Wrong code");
            throw new ApiException("That is not an existing code!");
        }catch (Exception exception) {
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
}

