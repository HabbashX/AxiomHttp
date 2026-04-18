package com.habbashx.request;

import com.habbashx.request.registry.RequestRegistry;

/**
 * Enumeration of the HTTP methods supported by the framework.
 *
 * <p>Used by {@link RequestRegistry} to look up the corresponding {@link com.habbashx.request.strategy.BuildStrategy}
 * and by {@link com.habbashx.executors.HttpExecutor} to convert the string value from
 * {@code @Request(method = "...")} into a typed constant via {@link #valueOf(String)}.
 */
public enum HttpMethod {

    /** The HTTP POST method — submits data to be processed by the server. */
    POST,

    /** The HTTP PUT method — replaces the target resource with the request payload. */
    PUT,

    /** The HTTP DELETE method — removes the target resource. */
    DELETE,

    /** The HTTP GET method — retrieves a representation of the target resource. */
    GET
}
