package org.nurfet.jwtapplication.service;

import org.nurfet.jwtapplication.model.Role;

import java.util.List;

public interface RoleService {

    List<Role> findAllRoles();
}
