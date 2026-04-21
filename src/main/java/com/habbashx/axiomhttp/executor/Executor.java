package com.habbashx.axiomhttp.executor;

import com.habbashx.axiomhttp.proxy.MethodContext;

import java.lang.reflect.Type;

/**
 * Abstract base for all request executors.
 *
 * <p>An executor is responsible for the final step of the request pipeline: performing the
 * actual I/O and returning a deserialized response object. It receives a fully-resolved
 * {@link MethodContext} (after all interceptors have run) and the expected return type so it
 * can drive deserialization correctly.
 *
 * <p>Subclasses implement {@link #execute(MethodContext, Type)} to dispatch the request via
 * a specific transport mechanism (e.g. {@code java.net.http.HttpClient}).
 */
public abstract class Executor {

    /**
     * Executes the HTTP request described by {@code context} and returns a deserialized response.
     *
     * @param context    the fully-resolved request context produced by the interceptor pipeline
     * @param returnType the expected return type, used to guide JSON deserialization;
     *                   may be a {@code ParameterizedType} (e.g. {@code List<User>}) or
     *                   {@code CompletableFuture<T>} for async calls
     * @return the deserialized response object, or a {@code CompletableFuture} wrapping it
     */
    public abstract Object execute(MethodContext context, Type returnType);
}