package com.habbashx.Interceptor;

import com.habbashx.proxy.MethodContext;

import java.util.List;

/**
 * Manages an ordered list of {@link Interceptor}s and applies them as a pipeline.
 *
 * <p>Before a request is executed, {@link #applyBefore(MethodContext)} runs each interceptor's
 * {@code before} method in list order, passing the (possibly mutated) context from one to the next.
 * After execution, {@link #applyAfter(Object, MethodContext)} runs each interceptor's {@code after}
 * method in the same order, allowing successive transformation of the response.
 */
public class InterceptorHierarchy {

    /** Ordered list of interceptors applied to every request. */
    private final List<Interceptor> interceptors;

    /**
     * Creates a hierarchy with the given interceptors.
     *
     * @param interceptors interceptors in the order they should be applied; must not be {@code null}
     */
    public InterceptorHierarchy(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Runs all {@code before} interceptors in order.
     *
     * @param ctx the initial request context
     * @return the context after all interceptors have had a chance to modify it
     */
    public MethodContext applyBefore(MethodContext ctx) {
        for (Interceptor i : interceptors) {
            ctx = i.before(ctx);
        }
        return ctx;
    }

    /**
     * Runs all {@code after} interceptors in order.
     *
     * @param response the raw deserialized response
     * @param ctx      the request context at the time the call was made
     * @return the response after all interceptors have had a chance to transform it
     */
    public Object applyAfter(Object response, MethodContext ctx) {
        Object result = response;
        for (Interceptor i : interceptors) {
            result = i.after(result, ctx);
        }
        return result;
    }
}