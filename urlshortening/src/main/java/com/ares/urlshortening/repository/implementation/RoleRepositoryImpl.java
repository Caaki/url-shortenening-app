package com.ares.urlshortening.repository.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.exceptions.ApiException;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.ares.urlshortening.enumeration.RoleType.ROLE_USER;
import static com.ares.urlshortening.query.RoleQuery.*;
import static java.util.Objects.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) throws ApiException {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        try{
            Role role = jdbc.queryForObject(
                    SELECT_ROLE_BY_NAME_QUERY,
                    Map.of("name",roleName), new RoleRowMapper());

            jdbc.update(INSERT_ROLE_TO_USER_QUERY,Map.of("userId",userId,"roleId", requireNonNull(role).getId()));

        }catch (EmptyResultDataAccessException e){
            throw new ApiException("No role found by name: "+ ROLE_USER.name());
        }catch (Exception e){
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        try{
           Role role = jdbc.queryForObject(
                    SELECT_ROLE_BY_USER_ID_QUERY,
                    Map.of("userId",userId), new RoleRowMapper());
           return role;

        }catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No role found by name: "+ ROLE_USER.name());
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
