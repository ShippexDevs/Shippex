package com.shippex.dto;

import com.shippex.constants.Designation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterAppUserRequest {
    private String name;
    private String username;
    private String password;
    private String email;
    private String whatsappContactNo;
    private Designation designation;
    private String shipName;
    private String shipIMONumber;
}
