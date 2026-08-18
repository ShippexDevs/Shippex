package com.shippex.dto.admin;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AdminLoginResponse {
    private String accessToken;

    private String tokenType;

    private String username;

    private String role;

    private Boolean firstLogin;
}
