package com.habbashx.axiomhttp.Interceptor;

import com.habbashx.axiomhttp.annotation.Headers;
import com.habbashx.axiomhttp.method.cache.MethodCache;
import com.habbashx.axiomhttp.method.meta.MethodMeta;
import com.habbashx.axiomhttp.parser.HeaderParser;
import com.habbashx.axiomhttp.proxy.MethodContext;

import java.util.List;
import java.util.Map;

/**
 * Interceptor that reads the {@link Headers} annotation on the invoked method and
 * injects the declared header strings into the {@link MethodContext} before the request is sent.
 *
 * <p>If the method carries no {@code @Headers} annotation, or the annotation's value array is
 * empty, the context is returned unmodified.
 *
 * <p>Header strings must follow the {@code "Name: value"} format expected by
 * {@code java.net.http.HttpRequest.Builder#headers(String...)}.
 */
public class HeaderInterceptor implements Interceptor {

    /** Cache used to retrieve pre-parsed annotation metadata for the invoked method. */
    private final MethodCache<MethodMeta> methodCache;

    /**
     * @param methodCache shared cache of per-method annotation metadata
     */
    public HeaderInterceptor(MethodCache<MethodMeta> methodCache) {
        this.methodCache = methodCache;
    }

    /**
     * Populates {@link MethodContext#setHeaders(Map)} from the method's {@code @Headers} annotation.
     *
     * @param ctx the current request context
     * @return the same context, with headers set if the annotation was present and non-empty
     */
    @Override
    public MethodContext before(MethodContext ctx) {
        Headers headers = methodCache.get(ctx.getReflectionMethod()).getHeadersAnnotation();
        if (headers != null && headers.value().length > 0) {
            ctx.setHeaders(HeaderParser.parseHeaders(headers.value()));
        }
        return ctx;
    }

    /**
     * No-op — this interceptor does not modify the response.
     *
     * @param response the deserialized response
     * @param ctx      the request context
     * @return the response unchanged
     */
    @Override
    public Object after(Object response, MethodContext ctx) {
        return response;
    }
}

