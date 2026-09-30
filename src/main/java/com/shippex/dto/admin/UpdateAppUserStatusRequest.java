package com.shippex.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAppUserStatusRequest {
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
