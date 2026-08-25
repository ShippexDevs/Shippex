package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryDestination {
    private String shipName;
    private String imoNumber;
    private String berthNumber;
    private String portName;
}
