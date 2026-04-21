package com.habbashx.axiomhttp.response;

import java.util.List;
import java.util.Map;

/**
 * Immutable transport-level snapshot of an HTTP response.
 *
 * <p>Produced by {@link com.habbashx.axiomhttp.request.RequestMethod#execute()} and
 * {@link com.habbashx.axiomhttp.request.RequestMethod#executeAsync()} and passed
 * to {@link com.habbashx.axiomhttp.executor.HttpExecutor} for deserialization.
 * This class carries the raw string body, response headers, and HTTP status code,
 * keeping transport concerns separate from the deserialized domain object.
 *
 * <p>Example:
 * <pre>{@code
 * ResponseContext rc = registry.execute(HttpMethod.GET, url, "", headers);
 * System.out.println(rc.getStatusCode()); // 200
 * System.out.println(rc.getBody());       // raw JSON string
 * }</pre>
 */
public class ResponseContext {

    /** The raw HTTP response body as a string (typically JSON). */
    private final String body;

    /**
     * The response headers as a multi-value map.
     * Keys are lower-cased header names; values are lists to support repeated headers.
     */
    private final Map<String, List<String>> headers;

    /** The HTTP status code (e.g. 200, 404, 500). */
    private final int statusCode;

    /**
     * Creates a new {@code ResponseContext} with all fields set.
     *
     * @param body       the raw response body string
     * @param headers    the response header map
     * @param statusCode the HTTP status code
     */
    public ResponseContext(String body,
                           Map<String, List<String>> headers,
                           int statusCode) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    /**
     * Returns the raw response body string (typically a JSON payload).
     *
     * @return the response body; may be empty but never {@code null} for a successful response
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns the response headers as a multi-value map.
     *
     * <p>Keys are lower-cased header names as returned by {@code java.net.http.HttpHeaders#map()}.
     *
     * @return an unmodifiable view of the response headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the status code (e.g. {@code 200}, {@code 404})
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns a human-readable representation of this context for debugging.
     *
     * @return a string containing the status code, headers, and body
     */
    @Override
    public String toString() {
        return "ResponseContext{" +
                "body='" + body + '\'' +
                ", headers=" + headers +
                ", statusCode=" + statusCode +
                '}';
    }
}
