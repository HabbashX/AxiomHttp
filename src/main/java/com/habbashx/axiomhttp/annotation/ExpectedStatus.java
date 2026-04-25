package com.habbashx.axiomhttp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the HTTP status code(s) that a request method considers successful.
 *
 * <p>By default, AxiomHttp treats any 2xx response as successful. Annotate a method
 * with {@code @ExpectedStatus} to override this on a per-method basis. If the actual
 * HTTP response status code does not match any of the declared values, an
 * {@link com.habbashx.axiomhttp.exception.UnExpectedStatusException} is thrown.
 *
 * <p><b>Example — single expected status:</b>
 * <pre>{@code
 * @Request(url = "/users", method = "POST")
 * @ExpectedStatus(201)
 * User createUser(@Body User user);
 * }</pre>
 * <p>If this annotation is not present on a method, the default validation policy applies:
 * any status code in the range 200–299 is treated as success, and anything outside
 * that range throws {@link com.habbashx.axiomhttp.exception.UnExpectedStatusException}.
 *
 * @see com.habbashx.axiomhttp.exception.UnExpectedStatusException
 * @see com.habbashx.axiomhttp.annotation.Request
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectedStatus {

    /**
     * One or more HTTP status codes that this method considers a successful response.
     *
     * <p>Must contain at least one value. Values should be valid HTTP status codes
     * in the range 100–599. Providing an empty array will cause all responses to be
     * treated as failures.
     *
     * @return the integer of acceptable HTTP status codes
     */
    int value();
}
