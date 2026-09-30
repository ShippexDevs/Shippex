package com.shippex.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.shippex.constants.Designation;
import com.shippex.model.OrderStatus;
import com.shippex.model.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumNameDeserializerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void acceptsUnderscoresAndSpacesForOrderStatus() throws Exception {
        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                mapper.readValue("\"OUT_FOR_DELIVERY\"", OrderStatus.class));
        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                mapper.readValue("\"OUT FOR DELIVERY\"", OrderStatus.class));
    }

    @Test
    void acceptsMixedCaseAndRepeatedWhitespaceForProductStatus() throws Exception {
        assertEquals(ProductStatus.OUT_OF_STOCK,
                mapper.readValue("\" out   of\\tstock \"", ProductStatus.class));
    }

    @Test
    void acceptsDesignationInEitherFormat() throws Exception {
        assertEquals(Designation.CHIEF_OFFICER,
                mapper.readValue("\"CHIEF_OFFICER\"", Designation.class));
        assertEquals(Designation.CHIEF_OFFICER,
                mapper.readValue("\"CHIEF OFFICER\"", Designation.class));
    }

    @Test
    void rejectsUnknownEmptyAndWhitespaceOnlyValues() {
        assertThrows(JsonProcessingException.class, () -> mapper.readValue("\"NOT_A_STATUS\"", OrderStatus.class));
        assertThrows(JsonProcessingException.class, () -> mapper.readValue("\"\"", ProductStatus.class));
        assertThrows(JsonProcessingException.class, () -> mapper.readValue("\"   \"", Designation.class));
    }

    @Test
    void preservesJsonNullAndAcceptsPaddedSingleWordValues() throws Exception {
        assertNull(mapper.readValue("null", OrderStatus.class));
        assertEquals(ProductStatus.ACTIVE,
                mapper.readValue("\"  active  \"", ProductStatus.class));
    }
}
