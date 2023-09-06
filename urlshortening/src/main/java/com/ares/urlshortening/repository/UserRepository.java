package com.ares.urlshortening.repository;

import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.forms.UpdateForm;

import java.util.Collection;

public interface UserRepository<T extends User> {

    T create(T data) throws ApiException;
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    User getUserByEmail(String email);
    void sendVerificationCode(UserDTO userDTO);
    T verifyCode(String email, String code);
    void resetPassword(String email);
    T verifyPasswordKey(String key);
    void renewPassword(String key, ResetPasswordForm form);
    T verifyAccountKey(String key);

    T updateUserDetails(UpdateForm user,Long id);
}
