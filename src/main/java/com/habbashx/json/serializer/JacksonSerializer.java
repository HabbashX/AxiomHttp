package com.habbashx.json.serializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.exception.JsonException;

import java.lang.reflect.Type;

/**
 * {@link JsonSerializer} implementation backed by Jackson's {@link ObjectMapper}.
 *
 * <p>Handles all standard Jackson-supported types including generics (via {@link JavaType})
 * and plain {@code String} responses (returned as-is without parsing).
 *
 * <p>A single shared {@code ObjectMapper} instance is reused for all calls; Jackson's
 * {@code ObjectMapper} is thread-safe after configuration.
 */
public class JacksonSerializer implements JsonSerializer {

    /** Shared Jackson mapper. Thread-safe; reused for all serialize/deserialize calls. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Deserializes a JSON string into the given type using Jackson.
     *
     * <p>If {@code type} is {@code String.class}, the raw JSON string is returned directly
     * without any parsing.
     *
     * @param <T>  the expected result type
     * @param json the raw JSON string
     * @param type the target type; supports generic types via {@link JavaType}
     * @return the deserialized object
     * @throws JsonException if Jackson fails to parse the JSON
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String json, Type type) {
        try {
            if (type == String.class) {
                return (T) json;
            }
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new JsonException("Failed to deserialize JSON into " + type.getTypeName(), e);
        }
    }

    /**
     * Serializes an object to a JSON string using Jackson.
     *
     * @param object the object to serialize
     * @return the JSON string representation
     * @throws JsonException if Jackson fails to serialize the object
     */
    @Override
    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new JsonException("Failed to serialize object of type " + object.getClass().getName(), e);
        }
    }
}