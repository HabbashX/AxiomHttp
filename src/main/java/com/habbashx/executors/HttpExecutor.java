package com.habbashx.executors;

import com.habbashx.exception.HttpException;
import com.habbashx.json.serializer.JsonSerializer;
import com.habbashx.proxy.MethodContext;
import com.habbashx.request.HttpMethod;
import com.habbashx.request.registry.RequestRegistry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Concrete {@link Executor} that dispatches HTTP requests via {@link RequestRegistry} and
 * deserializes the raw JSON response using a {@link JsonSerializer}.
 *
 * <p>Supports both synchronous and asynchronous execution:
 * <ul>
 *   <li>If {@code returnType} is {@code CompletableFuture<T>}, the request is sent asynchronously
 *       and the inner type {@code T} is used for deserialization.</li>
 *   <li>Otherwise, the request is sent synchronously and the full {@code returnType} is used.</li>
 * </ul>
 *
 * <p>Any exception from the synchronous path is wrapped in a {@link HttpException}.
 */
public class HttpExecutor extends Executor {

    /** Registry that maps HTTP methods to their build strategies and dispatches requests. */
    private final RequestRegistry registry;

    /** Serializer used to convert the raw JSON response string into the expected return type. */
    private final JsonSerializer serializer;

    /**
     * @param registry   the request registry responsible for HTTP dispatch
     * @param serializer the JSON serializer used to deserialize the response body
     */
    public HttpExecutor(RequestRegistry registry, JsonSerializer serializer) {
        this.registry   = registry;
        this.serializer = serializer;
    }

    /**
     * Executes the HTTP request and returns a deserialized response.
     *
     * <p>If {@code returnType} is {@code CompletableFuture<T>}, the request is sent
     * asynchronously. Otherwise it is sent synchronously and any failure is wrapped
     * in a {@link HttpException}.
     *
     * @param context    fully-resolved request context from the interceptor pipeline
     * @param returnType the target type for deserialization; may be parameterized or a future
     * @return the deserialized object, or a {@code CompletableFuture} wrapping it for async calls
     * @throws HttpException if the synchronous HTTP call fails
     */
    @Override
    public Object execute(MethodContext context, Type returnType) {
        HttpMethod method   = HttpMethod.valueOf(context.getMethod().toUpperCase());
        String url          = context.getUrl();
        String body         = context.getBody();
        String[] headers    = context.getHeaders();

        if (isCompletableFuture(returnType)) {
            Type innerType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            return registry
                    .executeAsync(method, url, body, headers)
                    .thenApply(raw -> serializer.deserialize(raw, innerType));
        }

        try {
            String raw = registry.execute(method, url, body, headers);
            return serializer.deserialize(raw, returnType);
        } catch (Exception e) {
            throw new HttpException("HTTP execution failed for ["
                    + context.getMethod() + " " + context.getUrl() + "]", e);
        }
    }

    /**
     * Checks whether the given type is a {@code CompletableFuture<?>}.
     *
     * @param type the type to inspect
     * @return {@code true} if {@code type} is a parameterized {@code CompletableFuture}
     */
    private boolean isCompletableFuture(Type type) {
        return type instanceof ParameterizedType pt && pt.getRawType() == CompletableFuture.class;
    }
}
