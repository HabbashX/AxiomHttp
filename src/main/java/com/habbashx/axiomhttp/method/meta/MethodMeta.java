package com.habbashx.axiomhttp.method.meta;

import com.habbashx.axiomhttp.annotation.*;
import com.habbashx.axiomhttp.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * Immutable snapshot of all annotation metadata for a single API method.
 *
 * <p>Parsed once per method and stored in {@code MethodCache}. Holding all annotation data
 * upfront avoids repeated reflective lookups on every request invocation.
 *
 * <p>Instances are created by the {@code MethodCache} parser function: {@code MethodMeta::new}.
 */
public class MethodMeta {

    /** The underlying reflected method. */
    private final Method method;

    /** The generic return type used by the executor to drive deserialization. */
    private final Type returnType;

    /** The {@code @Request} annotation declaring the URI, HTTP method, and optional body. */
    private final Request requestAnnotation;

    /** The {@code @Headers} annotation declaring static headers, or {@code null} if absent. */
    private final Headers headersAnnotation;

    /** The method's declared parameters in order. */
    private final Parameter[] parameters;

    /** The {@code @SaveResponse} annotation, or {@code null} if the method does not save responses. */
    private final SaveResponse saveResponseAnnotation;

    /**
     * Per-parameter {@code @Path} annotations, indexed by parameter position.
     * {@code null} at index {@code i} means parameter {@code i} has no {@code @Path}.
     */
    private final Path[] pathAnnotations;

    /**
     * Per-parameter {@code @Query} annotations, indexed by parameter position.
     * {@code null} at index {@code i} means parameter {@code i} has no {@code @Query}.
     */
    private final Query[] queryAnnotations;

    private final NoCookies noCookiesAnnotation;

    /**
     * Parses and caches all annotation metadata from the given method.
     *
     * @param method the API interface method to inspect; must not be {@code null}
     */
    public MethodMeta(Method method) {
        this.method = method;
        this.returnType = method.getGenericReturnType();
        this.requestAnnotation = method.getAnnotation(Request.class);
        this.headersAnnotation = method.getAnnotation(Headers.class);
        this.parameters = method.getParameters();
        this.saveResponseAnnotation = method.getAnnotation(SaveResponse.class);
        this.noCookiesAnnotation = method.getAnnotation(NoCookies.class);

        this.pathAnnotations  = new Path[parameters.length];
        this.queryAnnotations = new Query[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            this.pathAnnotations[i]  = parameters[i].getAnnotation(Path.class);
            this.queryAnnotations[i] = parameters[i].getAnnotation(Query.class);
        }
    }

    /**
     * @return the underlying reflected method
     */
    public Method getMethod() { return method; }

    /**
     * @return the generic return type, used by the executor to deserialize the response
     */
    public Type getReturnType() { return returnType; }

    /**
     * @return the {@code @Request} annotation; never {@code null} for a valid API method
     */
    public Request getRequestAnnotation() { return requestAnnotation; }

    /**
     * @return the {@code @Headers} annotation, or {@code null} if the method has none
     */
    public Headers getHeadersAnnotation() { return headersAnnotation; }

    /**
     * @return the method's declared parameters in declaration order
     */
    public Parameter[] getParameters() { return parameters; }

    /**
     * @return the {@code @SaveResponse} annotation, or {@code null} if the method has none
     */
    public SaveResponse getSaveResponseAnnotation() {
        return saveResponseAnnotation;
    }

    /**
     * Returns the {@code @Path} annotation for the parameter at the given index.
     *
     * @param paramIndex zero-based parameter index
     * @return the {@code @Path} annotation, or {@code null} if the parameter is not annotated
     */
    public Path getPathAnnotation(int paramIndex) { return pathAnnotations[paramIndex]; }

    /**
     * Returns the {@code @Query} annotation for the parameter at the given index.
     *
     * @param paramIndex zero-based parameter index
     * @return the {@code @Query} annotation, or {@code null} if the parameter is not annotated
     */
    public Query getQueryAnnotation(int paramIndex) { return queryAnnotations[paramIndex]; }

    public NoCookies getNoCookiesAnnotation() {
        return noCookiesAnnotation;
    }
}
