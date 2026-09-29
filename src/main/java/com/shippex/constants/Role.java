package com.shippex.constants;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shippex.serialization.EnumNameSerializer;

@JsonSerialize(using = EnumNameSerializer.class)
public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN
}
