package com.shippex.dto.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryDestinationResponse {
    private String shipName;
    private String imoNumber;
    private String berthNumber;
    private String portName;
}
