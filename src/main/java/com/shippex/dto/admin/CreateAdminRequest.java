package com.shippex.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdminRequest {
    @NotBlank(message = "Name is required.")
    private String name;
    @NotBlank(message = "Username is required.")
    private String username;
    @Email(message = "Enter valid email.")
    @NotBlank(message = "Email is required.")
    private String email;
    private Boolean mfaEnabled = true;
}
