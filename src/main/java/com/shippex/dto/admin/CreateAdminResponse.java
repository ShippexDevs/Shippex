package com.shippex.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdminResponse {
    private String id;
    private String username;
    private String message;
}
