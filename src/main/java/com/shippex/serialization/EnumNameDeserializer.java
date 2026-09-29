package com.shippex.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.Locale;

public class EnumNameDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {
    private final Class<?> enumClass;

    public EnumNameDeserializer() {
        this(null);
    }

    private EnumNameDeserializer(Class<?> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType type = property == null ? context.getContextualType() : property.getType();
        Class<?> targetClass = type == null ? null : type.getRawClass();
        return new EnumNameDeserializer(targetClass);
    }

    @Override
    public Enum<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null) {
            return (Enum<?>) context.handleUnexpectedToken(Enum.class, parser);
        }

        if (enumClass == null || !enumClass.isEnum()) {
            return (Enum<?>) context.handleUnexpectedToken(Enum.class, parser);
        }

        String enumName = value.trim().replaceAll("\\s+", "_").toUpperCase(Locale.ROOT);
        try {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Enum<?> result = Enum.valueOf((Class<? extends Enum>) enumClass, enumName);
            return result;
        } catch (IllegalArgumentException exception) {
            return (Enum<?>) context.handleWeirdStringValue(enumClass, value,
                    "Expected an enum value with spaces or underscores");
        }
    }
}
