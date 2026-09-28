package com.shippex.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shippex.serialization.EnumNameSerializer;

@JsonSerialize(using = EnumNameSerializer.class)
public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED,
    COMING_SOON
}
