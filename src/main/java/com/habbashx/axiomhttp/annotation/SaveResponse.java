package com.habbashx.axiomhttp.annotation;

import com.habbashx.axiomhttp.Interceptor.ResponseSaverInterceptor;
import com.habbashx.axiomhttp.Interceptor.ResponseSaverInterceptor.SaveFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method's response to be automatically saved to a file after execution.
 *
 * <p>Only methods carrying this annotation will have their responses persisted —
 * unannotated methods are unaffected. Saving is handled transparently by
 * {@link ResponseSaverInterceptor} in the {@code after}
 * phase of the interceptor pipeline.
 *
 * <p>Example — custom file name:
 * <pre>{@code
 * @SaveResponse(path = "responses", format = SaveFormat.JSON, fileName = "users_response")
 * @Request(uri = "https://api.example.com/users/{id}", method = "GET")
 * User getUser(@Path("id") long id);
 * // → responses/users_response.json
 * }</pre>
 *
 * <p>Example — auto-generated file name:
 * <pre>{@code
 * @SaveResponse(path = "responses", format = SaveFormat.TXT)
 * @Request(uri = "https://api.example.com/users/{id}", method = "GET")
 * User getUser(@Path("id") long id);
 * // → responses/GET_users_id_20260418_153042.txt
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveResponse {

    /**
     * The directory path where the response file will be saved.
     * Created automatically if it does not exist.
     *
     * @return the output directory path
     */
    String path();

    /**
     * The file format to use — {@link SaveFormat#JSON} or {@link SaveFormat#TEXT}.
     *
     * @return the save format
     */
    SaveFormat format();

    /**
     * Optional custom file name without extension.
     * If left empty (the default), a name is auto-generated from the HTTP method,
     * URL path, and timestamp.
     *
     * @return the custom file name, or empty string to auto-generate
     */
    String fileName() default "";
}