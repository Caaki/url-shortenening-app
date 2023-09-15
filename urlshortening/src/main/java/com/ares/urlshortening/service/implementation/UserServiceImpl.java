package com.ares.urlshortening.service.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.forms.SettingsForm;
import com.ares.urlshortening.forms.UpdateForm;
import com.ares.urlshortening.forms.UpdatePasswordForm;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.repository.UserRepository;
import com.ares.urlshortening.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }
    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }
    @Override
    public void sendVerificationCode(UserDTO userDTO) {
        userRepository.sendVerificationCode(userDTO);
    }
    @Override
    public UserDTO getUserById(Long id) {
        return mapToUserDTO(userRepository.get(id));
    }
    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email,code));
    }

    @Override
    public void resetPassword(String email) {
        userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, ResetPasswordForm form) {
        userRepository.renewPassword(key,form);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDTO(userRepository.verifyAccountKey(key));
    }

    @Override
    public UserDTO updateUserDetails(UpdateForm user, Long id) {
        return mapToUserDTO(userRepository.updateUserDetails(user, id));
    }

    @Override
    public void updateUserPassword(UpdatePasswordForm form, Long userId) {
        userRepository.updateUserPassword(form,userId);
    }

    @Override
    public UserDTO updateUserSettings(SettingsForm form, Long userId) {
        return mapToUserDTO(userRepository.updateUserSettings(form, userId));
    }

    @Override
    public UserDTO toggleMfa(Long id) {
        return  mapToUserDTO(userRepository.toggleMfa(id));
    }

    @Override
    public void updateImage(UserDTO user, MultipartFile image) {
        userRepository.updateImage(user,image);
    }

    private UserDTO mapToUserDTO(User user){
        return UserDTOMapper.toDTO(user, roleRepository.getRoleByUserId(user.getId()));
    }
}
