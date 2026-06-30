package com.shippex.dto;

import com.shippex.constants.Designation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAppUserResponse {

    private String username;
    private String name;
    private String email;
    private String whatsappContactNo;
    private Designation designation;
    private String shipName;
    private String shipIMONumber;
    private String message;
}
