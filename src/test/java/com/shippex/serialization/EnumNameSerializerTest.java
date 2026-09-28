package com.shippex.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shippex.model.OrderStatus;
import com.shippex.model.ProductStatus;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumNameSerializerTest {
    private final EnumNameSerializer serializer = new EnumNameSerializer();

    @Test
    void serializesUnderscoredEnumNameAsReadableWords() throws Exception {
        StringWriter output = new StringWriter();
        try (var generator = new JsonFactory().createGenerator(output)) {
            serializer.serialize(OrderStatus.OUT_FOR_DELIVERY, generator, new ObjectMapper().getSerializerProvider());
        }
        assertEquals("\"OUT FOR DELIVERY\"", output.toString());
    }

    @Test
    void serializesSingleWordEnumWithoutChangingItsName() throws Exception {
        StringWriter output = new StringWriter();
        try (var generator = new JsonFactory().createGenerator(output)) {
            serializer.serialize(ProductStatus.ACTIVE, generator, new ObjectMapper().getSerializerProvider());
        }
        assertEquals("\"ACTIVE\"", output.toString());
    }
}
