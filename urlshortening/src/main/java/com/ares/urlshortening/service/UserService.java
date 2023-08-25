package com.ares.urlshortening.service;

import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import com.ares.urlshortening.repository.UserRepository;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO userDTO);

    UserDTO getUserById(Long id);

    UserDTO verifyCode(String email, String code);
}
