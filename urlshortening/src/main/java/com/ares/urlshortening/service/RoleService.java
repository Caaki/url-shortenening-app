package com.ares.urlshortening.service;

import com.ares.urlshortening.domain.Role;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


public interface RoleService {

    Role getRoleByUserId(Long id);



}
