package com.habbashx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one or more static HTTP headers to be sent with a request.
 *
 * <p>Each string in {@link #value()} must follow the {@code "Header-Name: value"} format
 * required by {@code java.net.http.HttpRequest.Builder#headers(String...)}.
 * Headers are injected by {@code HeaderInterceptor} before the request is dispatched.
 *
 * <p>Example:
 * <pre>{@code
 * @Headers({"Authorization","myToken"})
 * @Request(uri = "https://api.example.com/profile", method = "GET")
 * Profile getProfile();
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Headers {

    /**
     * The header strings to inject, each in {@code "Name: value"} format.
     *
     * @return array of header strings; defaults to empty (no headers injected)
     */
    String[] value() default {};
}
