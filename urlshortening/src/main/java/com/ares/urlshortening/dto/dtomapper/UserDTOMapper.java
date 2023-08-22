package com.ares.urlshortening.dto.dtomapper;

import com.ares.urlshortening.domain.User;
import com.ares.urlshortening.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {

    public static UserDTO toDTO(User user){
        UserDTO userDto = new UserDTO();
        BeanUtils.copyProperties(user,userDto);
        return userDto;
    }

    public static User fromDTO(UserDTO userDto){
        User user = new User();
        BeanUtils.copyProperties(userDto,user);
        return user;
    }

}
