package com.habbashx.axiomhttp.exception;

/**
 * Unchecked exception thrown when JSON serialization or deserialization fails.
 *
 * <p>Wraps exceptions from the underlying {@code JsonSerializer} implementation (e.g. Jackson's
 * {@code JsonProcessingException}) and surfaces them as a framework-level runtime exception.
 */
public class JsonException extends RuntimeException {

    /**
     * Creates a {@code JsonException} with a descriptive message and the root cause.
     *
     * @param message a human-readable description of what failed (e.g. target type name)
     * @param e       the underlying exception from the serializer
     */
    public JsonException(String message, Throwable e) {
        super(message, e);
    }
}
