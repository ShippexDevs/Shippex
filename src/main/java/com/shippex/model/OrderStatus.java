package com.shippex.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shippex.serialization.EnumNameSerializer;

@JsonSerialize(using = EnumNameSerializer.class)
public enum OrderStatus {

    PLACED,
    CONFIRMED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}