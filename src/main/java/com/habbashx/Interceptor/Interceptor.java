package com.habbashx.Interceptor;

import com.habbashx.proxy.MethodContext;

/**
 * Contract for all interceptors in the request pipeline.
 *
 * <p>An interceptor is invoked twice per request:
 * <ul>
 *   <li>{@link #before(MethodContext)} – called in registration order before the HTTP call is made.
 *       Implementations may enrich the {@link MethodContext} (e.g. resolve path variables, inject headers).</li>
 *   <li>{@link #after(Object, MethodContext)} – called in registration order after the response has been
 *       deserialized. Implementations may transform or inspect the result.</li>
 * </ul>
 *
 * <p>Interceptors are composed by {@link InterceptorHierarchy}.
 */
public interface Interceptor {

    /**
     * Invoked before the HTTP request is dispatched.
     *
     * @param ctx the mutable request context; implementations should modify and return it
     * @return the (potentially modified) context that will be passed to the next interceptor
     */
    MethodContext before(MethodContext ctx);

    /**
     * Invoked after the HTTP response has been deserialized.
     *
     * @param response the deserialized response object produced by the executor
     * @param ctx      the request context as it was when the call was made
     * @return the (potentially transformed) response that will be passed to the next interceptor
     */
    Object after(Object response, MethodContext ctx);
}
