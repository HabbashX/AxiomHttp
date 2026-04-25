package com.habbashx.axiomhttp.exception;

import com.habbashx.axiomhttp.validation.UrlValidator;

/**
 * Thrown when a URL or relative path fails structural validation in AxiomHttp.
 *
 * <p>This exception is thrown by {@link UrlValidator} during proxy-creation time
 * when a URL declared in {@link com.habbashx.axiomhttp.annotation.BaseUrl @BaseUrl} or
 * {@link com.habbashx.axiomhttp.annotation.Request @Request} is malformed, blank, uses
 * an unsupported scheme, contains an out-of-range port, or contains unresolved
 * {@code {placeholder}} segments after argument substitution.
 *
 * <p>Because validation happens at proxy-creation time rather than per-request,
 * this exception surfaces configuration errors at application startup rather than
 * silently failing during a live request.
 *
 * <p><b>Common causes:</b>
 * <ul>
 *   <li>A blank or null URL value in {@code @BaseUrl} or {@code @Request}.</li>
 *   <li>A URL that does not start with {@code http://} or {@code https://}.</li>
 *   <li>A port number outside the valid range of 1–65535.</li>
 *   <li>A relative path used where a full absolute URL is required.</li>
 *   <li>An unresolved {@code {paramName}} placeholder remaining in the final URL
 *       after all {@code @PathParam} arguments have been substituted.</li>
 * </ul>
 *
 * <p><b>Example trigger:</b>
 * <pre>{@code
 * @BaseUrl("not-a-valid-url")   // throws InvalidUrlException at proxy creation
 * public interface UserService { ... }
 * }</pre>
 *
 * @see UrlValidator
 * @see com.habbashx.axiomhttp.annotation.BaseUrl
 * @see com.habbashx.axiomhttp.annotation.Request
 */
public class InvalidUrlException extends RuntimeException {


    /**
     * Constructs a new {@code InvalidUrlException} with the given detail message.
     *
     * @param message a human-readable description of why the URL is invalid
     */
    public InvalidUrlException(String message) {
        super(message);
    }
}
