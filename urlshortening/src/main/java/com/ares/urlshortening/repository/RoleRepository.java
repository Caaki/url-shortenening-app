package com.ares.urlshortening.repository;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.exceptions.ApiException;


import java.util.Collection;

public interface RoleRepository<T extends Role> {

    T create(T data) throws ApiException;
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);

}
