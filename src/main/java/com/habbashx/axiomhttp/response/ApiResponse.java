package com.habbashx.axiomhttp.response;

import java.util.List;
import java.util.Map;

/**
 * Generic wrapper that enriches a deserialized API response with HTTP metadata.
 *
 * <p>When a method's return type is declared as {@code ApiResponse<T>}, the
 * {@link com.habbashx.axiomhttp.executor.HttpExecutor} wraps the deserialized body
 * together with the HTTP status code and response headers, giving callers full
 * visibility into the transport layer without sacrificing type safety.
 *
 * <p>Usage in an API interface:
 * <pre>{@code
 * @Request(uri = "https://api.example.com/users", method = "GET")
 * ApiResponse<List<User>> getUsers();
 * }</pre>
 *
 * <p>Reading the response:
 * <pre>{@code
 * ApiResponse<List<User>> resp = service.getUsers();
 * System.out.println(resp.getStatusCode());   // 200
 * System.out.println(resp.getBody().size());  // number of users
 * resp.getHeaders().forEach((k,v) -> System.out.println(k + ": " + v));
 * }</pre>
 *
 * @param <T> the type of the deserialized response body
 */
public class ApiResponse<T> {

    /** The HTTP status code returned by the server (e.g. 200, 404). */
    private final int statusCode;

    /**
     * The response headers as a multi-value map.
     * Keys are lower-cased header names; values are lists to support repeated headers.
     */
    private final Map<String, List<String>> headers;

    /** The deserialized response body of type {@code T}. */
    private final T body;

    /**
     * Creates a new {@code ApiResponse} with all fields set.
     *
     * @param statusCode the HTTP status code
     * @param headers    the response headers
     * @param body       the deserialized response body
     */
    public ApiResponse(int statusCode, Map<String, List<String>> headers, T body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the status code (e.g. {@code 200} for success, {@code 404} for not-found)
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the response headers as a multi-value map.
     *
     * @return an unmodifiable view of the response headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Returns the deserialized response body.
     *
     * @return the body of type {@code T}; may be {@code null} if the server returned no content
     */
    public T getBody() {
        return body;
    }
}
