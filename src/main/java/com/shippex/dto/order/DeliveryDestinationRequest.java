package com.shippex.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryDestinationRequest {
    @NotBlank
    private String shipName;
    private String imoNumber;
    private String berthNumber;
    @NotBlank
    private String portName;
}
