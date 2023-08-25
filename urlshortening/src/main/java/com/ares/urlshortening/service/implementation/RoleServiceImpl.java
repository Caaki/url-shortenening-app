package com.ares.urlshortening.service.implementation;

import com.ares.urlshortening.domain.Role;
import com.ares.urlshortening.repository.RoleRepository;
import com.ares.urlshortening.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository<Role> roleRepository;

    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepository.getRoleByUserId(id);
    }
}
