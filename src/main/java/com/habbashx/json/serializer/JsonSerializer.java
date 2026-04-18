package com.habbashx.json.serializer;

import java.lang.reflect.Type;
/**
 * Strategy interface for JSON serialization and deserialization.
 *
 * <p>Decouples the framework from any specific JSON library. The default implementation,
 * {@link JacksonSerializer}, is backed by Jackson's {@code ObjectMapper}.
 * Alternative implementations (e.g. Gson, Moshi) can be supplied to {@code HttpExecutor}
 * to swap the library transparently.
 */
public interface JsonSerializer {

    /**
     * Deserializes a JSON string into an object of the specified type.
     *
     * <p>The {@code type} parameter may be a plain {@link Class}, a {@link java.lang.reflect.ParameterizedType}
     * (e.g. {@code List<User>}), or any other {@link java.lang.reflect.Type} supported by the
     * underlying library.
     *
     * @param <T>  the expected result type
     * @param json the raw JSON string to parse
     * @param type the target type; must not be {@code null}
     * @return the deserialized object
     * @throws com.habbashx.exception.JsonException if parsing fails
     */
    <T> T deserialize(String json, Type type);

    /**
     * Serializes an object to its JSON string representation.
     *
     * @param object the object to serialize; must not be {@code null}
     * @return a JSON string representation of {@code object}
     * @throws com.habbashx.exception.JsonException if serialization fails
     */
    String serialize(Object object);
}