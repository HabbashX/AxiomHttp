package com.habbashx.axiomhttp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that instructs the framework to strip any extraneous wrapper
 * text from the raw HTTP response before returning it to the caller.
 *
 * <p>When this annotation is present on a method, the response body is cleaned of
 * leading/trailing whitespace, control characters, or other transport-level artefacts
 * that may surround the actual payload — useful when consuming APIs that embed the
 * JSON body inside additional envelope text.
 *
 * <p>Applies to methods only; retained at runtime so the proxy engine can detect it
 * via reflection.
 *
 * <p>Example:
 * <pre>{@code
 * @CleanResponse
 * @Request(uri = "https://api.example.com/data", method = "GET")
 * String getRawData();
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CleanResponse {
}
