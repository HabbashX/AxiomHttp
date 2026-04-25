package com.habbashx.axiomhttp.validation;

import com.habbashx.axiomhttp.exception.InvalidUrlException;

import java.util.regex.Pattern;

/**
 * Validates URLs and relative paths used in AxiomHttp annotations.
 *
 * <p>All validation in this class is performed using pre-compiled {@link Pattern}
 * instances for efficiency. Validation is intended to run at proxy-creation time
 * so that configuration errors surface at application startup rather than during
 * live request handling.
 *
 * <p>The validator covers:
 * <ul>
 *   <li>Full absolute URLs including scheme, userinfo, IPv4, IPv6, hostname,
 *       port, path, query string, and fragment.</li>
 *   <li>Relative paths including path segments with {@code {placeholder}} syntax,
 *       query strings, and fragments.</li>
 *   <li>Port range enforcement (1–65535).</li>
 *   <li>Detection of unresolved {@code {placeholder}} segments after
 *       argument substitution.</li>
 * </ul>
 *
 * <p>All methods throw {@link InvalidUrlException} on validation failure.
 * All methods are stateless and thread-safe.
 *
 * @see InvalidUrlException
 * @see com.habbashx.axiomhttp.annotation.BaseUrl
 * @see com.habbashx.axiomhttp.annotation.Request
 */
public class UrlValidator {

    /**
     * Matches a full absolute URL supporting:
     * <ul>
     *   <li>Schemes: {@code http} and {@code https} only.</li>
     *   <li>Optional userinfo: {@code user:pass@}.</li>
     *   <li>Hosts: IPv4 ({@code 192.168.1.1}), IPv6 ({@code [::1]}),
     *       and fully-qualified hostnames with subdomain chains.</li>
     *   <li>Optional port: {@code :8080}.</li>
     *   <li>Optional path: including {@code {placeholder}} segments for {@code @PathParam}.</li>
     *   <li>Optional query string: {@code ?key=value&key2=value2}.</li>
     *   <li>Optional fragment: {@code #section}.</li>
     * </ul>
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?)://" +
                    "([\\w.-]+:[\\w.-]+@)?" +
                    "(" +
                    "(\\d{1,3}\\.){3}\\d{1,3}" +
                    "|\\[([0-9a-fA-F:]+)\\]" +
                    "|([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?" +
                    "(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)*)" +
                    ")" +
                    "(:\\d{1,5})?" +
                    "(/[\\w\\-._~:@!$&'()*+,;=%{}]*)?" +
                    "(\\?[\\w\\-._~:@!$&'()*+,;=%]+" +
                    "([=][\\w\\-._~:@!$&'()*+,;=%]*)?" +
                    "(&[\\w\\-._~:@!$&'()*+,;=%]+([=][\\w\\-._~:@!$&'()*+,;=%]*)?)*)?" +
                    "(#[\\w\\-._~:@!$&'()*+,;=%]*)?" +
                    "$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches a relative path for use with {@link com.habbashx.axiomhttp.annotation.BaseUrl @BaseUrl}.
     * Allows path segments containing {@code {placeholder}} syntax, an optional
     * query string, and an optional fragment. Does not allow a scheme or host.
     */
    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile(
            "^(/[\\w\\-._~:@!$&'()*+,;=%{}]*)?" +
                    "(\\?[\\w\\-._~:@!$&'()*+,;=%]+" +
                    "([=][\\w\\-._~:@!$&'()*+,;=%]*)?" +
                    "(&[\\w\\-._~:@!$&'()*+,;=%]+([=][\\w\\-._~:@!$&'()*+,;=%]*)?)*)?" +
                    "(#[\\w\\-._~:@!$&'()*+,;=%]*)?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches a {@code {placeholder}} segment within a URL path.
     * Used to detect unresolved placeholders after {@code @PathParam} substitution.
     * Placeholder names must start with a letter and contain only letters,
     * digits, and underscores.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "\\{([a-zA-Z][a-zA-Z0-9_]*)\\}"
    );

    /**
     * Validates a full absolute URL.
     *
     * <p>The URL must start with {@code http://} or {@code https://}, contain a valid
     * host, and conform to the structure defined by {@link #URL_PATTERN}. Port numbers,
     * if present, are validated to be in the range 1–65535.
     *
     * <p>This method is called automatically by {@code MethodMeta} at proxy-creation
     * time when no {@code @BaseUrl} is present on the interface.
     *
     * @param url the absolute URL string to validate; must not be {@code null} or blank
     * @throws InvalidUrlException if the URL is null, blank, structurally invalid,
     *                             or contains an out-of-range port
     */
    public static void validateFullUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL must not be null or blank");
        }
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new InvalidUrlException("Invalid URL format: " + url);
        }
        validatePort(url);
    }

    /**
     * Validates a relative path intended for use with {@code @BaseUrl}.
     *
     * <p>The path must not contain a scheme or host. It may contain path segments
     * with {@code {placeholder}} syntax, an optional query string, and an optional
     * fragment. An empty string is also valid and represents the root path.
     *
     * <p>This method is called automatically by {@code MethodMeta} at proxy-creation
     * time when {@code @BaseUrl} is present on the interface.
     *
     * @param path the relative path string to validate; must not be {@code null}
     * @throws InvalidUrlException if the path is null or structurally invalid
     */
    public static void validateRelativePath(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidUrlException("Path must not be null or blank");
        }
        if (!RELATIVE_PATH_PATTERN.matcher(path).matches()) {
            throw new InvalidUrlException("Invalid relative path format: " + path);
        }
    }

    /**
     * Validates that no unresolved {@code {placeholder}} segments remain in the
     * final URL after {@code @PathParam} argument substitution.
     *
     * <p>This method is called by {@code RequestProxyEngine} at request time,
     * after all method arguments have been substituted into the URL. A remaining
     * placeholder indicates that the caller did not supply a required argument.
     *
     * <p><b>Example of a URL that would fail this check:</b>
     * <pre>{@code
     * // method signature: User getUser(@PathParam("id") int id)
     * // url after partial substitution: "https://api.example.com/users/{id}"
     * // {id} was not replaced — throws InvalidUrlException
     * }</pre>
     *
     * @param url the fully-assembled URL string after argument substitution
     * @throws InvalidUrlException if any {@code {placeholder}} segment remains unresolved
     */
    public static void validateNoUnresolvedPlaceholders(String url) {
        var matcher = PLACEHOLDER_PATTERN.matcher(url);
        if (matcher.find()) {
            throw new InvalidUrlException(
                    "Unresolved placeholder '{" + matcher.group(1) + "}' in URL: " + url
            );
        }
    }

    /**
     * Validates that the value declared in {@code @BaseUrl} is a well-formed
     * absolute URL.
     *
     * <p>Applies the same rules as {@link #validateFullUrl(String)}. Exists as
     * a separate method to produce more specific error messages that reference
     * the {@code @BaseUrl} annotation by name.
     *
     * @param baseUrl the base URL string declared in {@code @BaseUrl}; must not be
     *                {@code null} or blank
     * @throws InvalidUrlException if the base URL is null, blank, or structurally invalid
     */
    public static void validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new InvalidUrlException("@BaseUrl value must not be null or blank");
        }
        if (!URL_PATTERN.matcher(baseUrl).matches()) {
            throw new InvalidUrlException("Invalid @BaseUrl format: " + baseUrl);
        }
    }

    /**
     * Validates that the port number embedded in the given URL, if present,
     * falls within the valid TCP port range of 1–65535.
     *
     * <p>This is a shared internal check called by both {@link #validateFullUrl(String)}
     * and {@link #validateBaseUrl(String)}. Port 0 is excluded as it is not a valid
     * destination port for HTTP traffic.
     *
     * @param url the URL string potentially containing a port number
     * @throws InvalidUrlException if a port is present and its value is outside 1–65535
     */
    private static void validatePort(String url) {
        var portMatcher = Pattern.compile(":(\\d{1,5})(?:/|\\?|#|$)").matcher(url);
        if (portMatcher.find()) {
            int port = Integer.parseInt(portMatcher.group(1));
            if (port < 1 || port > 65535) {
                throw new InvalidUrlException(
                        "Port " + port + " is out of valid range (1–65535) in URL: " + url
                );
            }
        }
    }
}