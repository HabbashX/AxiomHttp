package com.habbashx.axiomhttp.executor;

import com.habbashx.axiomhttp.exception.HttpException;
import com.habbashx.axiomhttp.json.serializer.JsonSerializer;
import com.habbashx.axiomhttp.proxy.MethodContext;
import com.habbashx.axiomhttp.request.HttpMethod;
import com.habbashx.axiomhttp.request.registry.RequestRegistry;
import com.habbashx.axiomhttp.response.ApiResponse;
import com.habbashx.axiomhttp.response.ResponseContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
        Map<String,List<String>> headers  = context.getHeaders();


        if (isCompletableFuture(returnType)) {
            if (!(returnType instanceof ParameterizedType pt)) {
                throw new RuntimeException("CompletableFuture must have a generic type");
            }
            Type innerType = pt.getActualTypeArguments()[0];
            return registry
                    .executeAsync(method, url, body, headers)
                    .thenApply(responseContext -> serializer.deserialize(responseContext.getBody(), innerType));
        }

        try {
            ResponseContext rc = registry.execute(method, url, body, headers);

            if (isApiResponse(returnType)) {

                Type innerType = getApiResponseInnerType(returnType);

                Object data = serializer.deserialize(rc.getBody(), innerType);

                return new ApiResponse<>(
                        rc.getStatusCode(),
                        rc.getHeaders(),
                        data
                );
            }

            return serializer.deserialize(rc.getBody(), returnType);
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

    private boolean isApiResponse(Type type) {

        if (!(type instanceof ParameterizedType pt)) {
            return false;
        }

        Type raw = pt.getRawType();

        return raw instanceof Class<?> cls &&
                cls == ApiResponse.class;
    }

    private Type getApiResponseInnerType(Type type) {

        if (!(type instanceof ParameterizedType pt)) {
            throw new RuntimeException("ApiResponse must be parameterized");
        }

        return pt.getActualTypeArguments()[0];
    }
}
