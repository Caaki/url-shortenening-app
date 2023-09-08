package com.ares.urlshortening.service;

import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.forms.SettingsForm;
import com.ares.urlshortening.forms.UpdateForm;
import com.ares.urlshortening.forms.UpdatePasswordForm;
import com.ares.urlshortening.repository.UserRepository;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
    void sendVerificationCode(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO verifyCode(String email, String code);
    void resetPassword(String email);
    UserDTO verifyPasswordKey(String key);
    void renewPassword(String key, ResetPasswordForm form);
    UserDTO verifyAccountKey(String key);
    UserDTO updateUserDetails(UpdateForm user,Long id);
    void updateUserPassword(UpdatePasswordForm form, Long subject);
    UserDTO updateUserSettings(SettingsForm form, Long id);
    UserDTO toggleMfa(Long id);
}
