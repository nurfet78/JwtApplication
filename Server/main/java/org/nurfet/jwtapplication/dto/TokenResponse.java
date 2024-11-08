package org.nurfet.jwtapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private UUID refreshTokenId;
    private Long id;
    private String username;
    private String email;
    private String role;
}
