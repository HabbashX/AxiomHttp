package com.habbashx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method parameter to a named placeholder in the URI template.
 *
 * <p>At runtime, {@code PathQueryInterceptor} replaces every occurrence of
 * {@code {name}} in the URL with the parameter's {@code toString()} value.
 *
 * <p>Example:
 * <pre>{@code
 * @Request(uri = "https://api.example.com/users/{id}", method = "GET")
 * User getUser(@Path("id") long id);
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    /**
     * The placeholder name as it appears in the URI template, without curly braces.
     *
     * @return the path variable name
     */
    String value();
}
