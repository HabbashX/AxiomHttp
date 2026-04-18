package com.habbashx.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an HTTP endpoint declaration.
 *
 * <p>Every method on an API class that should trigger a network call must carry this annotation.
 * At runtime, {@code RequestProxyEngine} reads these values to build the initial
 * {@code MethodContext} before passing it through the interceptor pipeline.
 *
 * <p>Example:
 * <pre>{@code
 * @Request(uri = "https://api.example.com/users/{id}", method = "GET")
 * User getUser(@Path("id") long id);
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {

    /**
     * The full URL or URL template for this endpoint.
     * Use {@code {name}} placeholders for path segments resolved by {@code @Path} parameters.
     *
     * @return the URI string
     */
    String uri();

    /**
     * The HTTP method to use (e.g. {@code "GET"}, {@code "POST"}, {@code "PUT"}, {@code "DELETE"}).
     * The value is case-insensitive.
     *
     * @return the HTTP verb
     */
    String method();

    /**
     * An optional static request body string sent with the request.
     * For dynamic bodies, serialize the object before passing it; defaults to empty.
     *
     * @return the request body, or an empty string if not applicable
     */
    String body() default "";

}
