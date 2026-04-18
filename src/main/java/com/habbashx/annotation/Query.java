package com.habbashx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method parameter to a URL query string key.
 *
 * <p>At runtime, {@code PathQueryInterceptor} appends {@code key=value} to the request URL.
 * Multiple query parameters are joined with {@code &} in declaration order.
 *
 * <p>Example:
 * <pre>{@code
 * // Produces: /search?q=java&page=2
 * @Request(uri = "https://api.example.com/search", method = "GET")
 * List<Result> search(@Query("q") String term, @Query("page") int page);
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * The query parameter key as it will appear in the URL.
     *
     * @return the query key name
     */
    String value();

}
