package com.shippex.dto.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerResponse {
    private String id;
    private String name;
    private String username;
    private String email;
    private String whatsappContactNo;
    private String designation;
    private String shipName;
    private String shipIMONumber;
}
