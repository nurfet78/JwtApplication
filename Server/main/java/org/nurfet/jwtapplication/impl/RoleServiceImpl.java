package org.nurfet.jwtapplication.impl;

import lombok.RequiredArgsConstructor;
import org.nurfet.jwtapplication.model.Role;
import org.nurfet.jwtapplication.repository.RoleRepository;
import org.nurfet.jwtapplication.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
