package com.shippex.dto.otp;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsernameRequest {
    @NotBlank(message = "Username is required.")
    private String username;
}
