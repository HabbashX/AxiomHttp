package com.habbashx.axiomhttp.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a base URL to be prepended to all request paths defined on the annotated interface.
 *
 * <p>Annotate any interface that represents an HTTP API client. Every method annotated
 * with {@link com.habbashx.axiomhttp.annotation.Request @Request} on that interface will
 * have its {@code url} value treated as a relative path and concatenated with this base URL
 * at proxy-creation time.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * @BaseUrl("https://api.example.com/v1")
 * public interface UserService {
 *
 *     @Request(url = "/users", method = "GET")
 *     List<User> getUsers();
 *
 *     @Request(url = "/users/{id}", method = "GET")
 *     User getUserById(@PathParam("id") int id);
 * }
 * }</pre>
 *
 * <p><b>URL concatenation rules:</b>
 * <ul>
 *   <li>{@code "https://api.example.com/v1"} + {@code "/users"} → {@code "https://api.example.com/v1/users"}</li>
 *   <li>Double slashes at the join point are automatically collapsed.</li>
 *   <li>If {@code @BaseUrl} is absent, the {@code url} in {@code @Request} must be a full absolute URL.</li>
 * </ul>
 *
 * <p><b>Validation:</b> The base URL value is validated at proxy-creation time by
 * {@link com.habbashx.axiomhttp.validation.UrlValidator#validateBaseUrl(String)}.
 * A blank, malformed, or non-HTTP/HTTPS value will throw
 * {@link com.habbashx.axiomhttp.exception.InvalidUrlException} immediately rather than
 * at request time.
 *
 * @see com.habbashx.axiomhttp.annotation.Request
 * @see com.habbashx.axiomhttp.validation.UrlValidator
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BaseUrl {

    /**
     * The base URL to prepend to all relative request paths on this interface.
     *
     * <p>Must be a valid absolute URL starting with {@code http://} or {@code https://}.
     * Must not be blank. Trailing slashes are handled automatically during concatenation.
     *
     * @return the base URL string
     */
    String value();
}
