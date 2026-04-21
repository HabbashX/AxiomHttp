package com.habbashx.axiomhttp.Interceptor;

import com.habbashx.axiomhttp.annotation.Path;
import com.habbashx.axiomhttp.annotation.Query;
import com.habbashx.axiomhttp.method.cache.MethodCache;
import com.habbashx.axiomhttp.method.meta.MethodMeta;
import com.habbashx.axiomhttp.proxy.MethodContext;

/**
 * Interceptor that resolves {@link Path} and {@link Query} annotations on method parameters,
 * producing the final URL before the HTTP request is dispatched.
 *
 * <p>For each parameter:
 * <ul>
 *   <li>If annotated with {@code @Path("name")}, the placeholder {@code {name}} in the URI
 *       template is replaced with the parameter's runtime value.</li>
 *   <li>If annotated with {@code @Query("key")}, a {@code key=value} pair is appended to the URL
 *       as a query string (first param prefixed with {@code ?}, subsequent params with {@code &}).</li>
 * </ul>
 *
 * <p>Parameters with neither annotation are ignored.
 */
public class PathQueryInterceptor implements Interceptor {

    /** Cache used to retrieve pre-parsed annotation metadata for the invoked method. */
    private final MethodCache<MethodMeta> methodCache;

    /**
     * @param methodCache shared cache of per-method annotation metadata
     */
    public PathQueryInterceptor(MethodCache<MethodMeta> methodCache) {
        this.methodCache = methodCache;
    }

    /**
     * Resolves path variables and builds the query string, then updates
     * {@link MethodContext#setUrl(String)} with the fully-formed URL.
     *
     * @param ctx the current request context whose URL may contain {@code {placeholder}} segments
     * @return the same context with the URL fully resolved
     */
    @Override
    public MethodContext before(MethodContext ctx) {
        MethodMeta meta = methodCache.get(ctx.getReflectionMethod());
        int paramCount = meta.getParameters().length;
        if (paramCount == 0) return ctx;

        Object[] args = ctx.getArgs();
        String url = ctx.getUrl();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < paramCount; i++) {
            Object value = args[i];

            Path path = meta.getPathAnnotation(i);
            if (path != null) {
                url = url.replace("{" + path.value() + "}", value.toString());
                continue;
            }

            Query q = meta.getQueryAnnotation(i);
            if (q != null) {
                query.append(query.isEmpty() ? "?" : "&")
                        .append(q.value()).append("=").append(value);
            }
        }

        ctx.setUrl(url + query);
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