package com.ares.urlshortening.service.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.dto.dtomapper.UserDTOMapper;
import com.ares.urlshortening.forms.ResetPasswordForm;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.repository.UserRepository;
import com.ares.urlshortening.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


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

    private UserDTO mapToUserDTO(User user){
        return UserDTOMapper.toDTO(user, roleRepository.getRoleByUserId(user.getId()));
    }
}
