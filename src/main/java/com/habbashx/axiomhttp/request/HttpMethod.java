package com.habbashx.axiomhttp.request;

import com.habbashx.axiomhttp.executor.HttpExecutor;
import com.habbashx.axiomhttp.request.strategy.BuildStrategy;
import com.habbashx.axiomhttp.request.registry.RequestRegistry;

/**
 * Enumeration of the HTTP methods supported by the framework.
 *
 * <p>Used by {@link RequestRegistry} to look up the corresponding {@link BuildStrategy}
 * and by {@link HttpExecutor} to convert the string value from
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
