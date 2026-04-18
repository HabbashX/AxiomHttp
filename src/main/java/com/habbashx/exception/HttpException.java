package com.habbashx.exception;

/**
 * Unchecked exception thrown when an HTTP request fails at the transport layer.
 *
 * <p>Wraps the underlying cause (e.g. {@code IOException}, connection timeout) and enriches
 * the message with the HTTP method and URL that triggered the failure, making stack traces
 * easier to diagnose.
 */
public class HttpException extends RuntimeException {

    /**
     * Creates an {@code HttpException} with a descriptive message and the root cause.
     *
     * @param message a human-readable description including the method and URL that failed
     * @param e       the underlying exception that caused the failure
     */
    public HttpException(String message, Throwable e) {
        super(message, e);
    }
}
