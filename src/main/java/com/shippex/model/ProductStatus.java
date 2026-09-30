package com.shippex.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.shippex.serialization.EnumNameDeserializer;
import com.shippex.serialization.EnumNameSerializer;

@JsonSerialize(using = EnumNameSerializer.class)
@JsonDeserialize(using = EnumNameDeserializer.class)
public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED,
    COMING_SOON
}
