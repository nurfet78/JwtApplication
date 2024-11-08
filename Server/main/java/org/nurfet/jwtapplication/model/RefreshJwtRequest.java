package org.nurfet.jwtapplication.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshJwtRequest {

    private String refreshToken;

    private String accessToken;
}
