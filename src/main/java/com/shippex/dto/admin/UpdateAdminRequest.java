package com.shippex.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAdminRequest {
    private String name;
    private Boolean mfaEnabled;
}
