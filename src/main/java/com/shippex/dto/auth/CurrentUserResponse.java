package com.shippex.dto.auth;

import com.shippex.constants.Designation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {

    private String id;

    private String username;

    private String name;

    private String email;

    private String whatsappContactNo;

    private Designation designation;

    private String shipName;

    private String shipIMONumber;

    private Boolean verified;

}