package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderUserResponse {
    private String id;
    private String name;
    private String username;
    private String email;
    private String whatsappContactNo;
    private String shipName;
    private String shipIMONumber;
}
