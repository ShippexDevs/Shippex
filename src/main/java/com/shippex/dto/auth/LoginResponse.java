package com.shippex.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;

    private String tokenType;

    private String username;

    private String name;
}
