package com.habbashx.axiomhttp.proxy.engine;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.habbashx.axiomhttp.Interceptor.InterceptorHierarchy;
import com.habbashx.axiomhttp.annotation.CleanResponse;
import com.habbashx.axiomhttp.annotation.Request;
import com.habbashx.axiomhttp.method.cache.MethodCache;
import com.habbashx.axiomhttp.method.meta.MethodMeta;
import com.habbashx.axiomhttp.executor.Executor;
import com.habbashx.axiomhttp.proxy.MethodContext;
import com.habbashx.axiomhttp.response.ApiResponse;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.jetbrains.annotations.NotNull;

import java.awt.desktop.PreferencesEvent;
import java.lang.reflect.Method;
import java.util.Map;

import static com.habbashx.axiomhttp.json.serializer.JacksonSerializer.toPrettyCode;

/**
 * Byte Buddy interception target that drives the entire request pipeline for every proxy method call.
 *
 * <p>When Byte Buddy intercepts a call on the generated proxy subclass, it delegates to
 * {@link #intercept(Method, Object[])}. This method:
 * <ol>
 *   <li>Retrieves cached annotation metadata ({@link MethodMeta}) for the invoked method.</li>
 *   <li>Builds an initial {@link MethodContext} from the {@code @Request} annotation values.</li>
 *   <li>Runs all {@code before} interceptors via {@link InterceptorHierarchy#applyBefore(MethodContext)},
 *       allowing them to resolve path/query params and inject headers.</li>
 *   <li>Delegates to the {@link Executor} to perform the HTTP call and deserialize the response.</li>
 *   <li>Runs all {@code after} interceptors via {@link InterceptorHierarchy#applyAfter(Object, MethodContext)}.</li>
 * </ol>
 */
public class RequestProxyEngine {

    /** The interceptor pipeline applied before and after each request. */
    private final InterceptorHierarchy hierarchy;

    /** The executor responsible for HTTP dispatch and response deserialization. */
    private final Executor executor;

    /** Cache of per-method annotation metadata to avoid repeated reflection on every call. */
    private final MethodCache<MethodMeta> methodCache;

    /**
     * @param hierarchy   the ordered interceptor pipeline
     * @param executor    the HTTP executor
     * @param methodCache the annotation metadata cache
     */
    public RequestProxyEngine(InterceptorHierarchy hierarchy,
                              Executor executor,
                              MethodCache<MethodMeta> methodCache) {
        this.hierarchy   = hierarchy;
        this.executor    = executor;
        this.methodCache = methodCache;
    }

    /**
     * Byte Buddy interception entry point — called for every method invocation on the proxy.
     *
     * <p>{@code @RuntimeType} tells Byte Buddy to perform an unchecked cast on the return value
     * so this single method can serve all return types. {@code @Origin} injects the reflected
     * {@link Method}, and {@code @AllArguments} injects the runtime argument array.
     *
     * @param method the intercepted proxy method
     * @param args   the runtime arguments passed by the caller; may be {@code null} for zero-arg methods
     * @return the deserialized response, or a {@code CompletableFuture} wrapping it for async methods
     */
    @RuntimeType
    public Object intercept(@Origin @NotNull Method method,
                            @AllArguments Object[] args) throws JsonProcessingException {

        MethodMeta meta = methodCache.get(method);
        Request req = meta.getRequestAnnotation();

        MethodContext ctx = MethodContext.builder()
                .url(req.uri())
                .method(req.method())
                .body(req.body())
                .headers(Map.of())
                .args(args != null ? args : new Object[0])
                .reflectionMethod(method)
                .build();

        ctx = hierarchy.applyBefore(ctx);
        Object response = executor.execute(ctx, meta.getReturnType());
        if (response instanceof String s) return toPrettyCode(s);

        return hierarchy.applyAfter(response, ctx);
    }
}