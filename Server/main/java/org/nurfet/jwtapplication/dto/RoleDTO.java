package org.nurfet.jwtapplication.dto;

import lombok.Getter;
import lombok.Setter;
import org.nurfet.jwtapplication.model.Role;

@Getter
@Setter
public class RoleDTO {

    private Long id;
    private String authority;

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.authority = role.getAuthority();
    }
}
