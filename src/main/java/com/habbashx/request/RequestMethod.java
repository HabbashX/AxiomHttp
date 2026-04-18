package com.habbashx.request;

import com.habbashx.request.strategy.BuildStrategy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Encapsulates a single HTTP request — its target URI, body, headers, and the
 * {@link BuildStrategy} that determines the HTTP verb — and provides both synchronous
 * and asynchronous dispatch via Java's {@link HttpClient}.
 *
 * <p>Instances are created by {@link com.habbashx.request.registry.RequestRegistry#assemble}
 * and are not intended for direct use by application code.
 *
 * <h3>Built-in strategies</h3>
 * The four static constants below implement {@link BuildStrategy} for each supported verb:
 * <ul>
 *   <li>{@link #GET}    — no body</li>
 *   <li>{@link #POST}   — body as string</li>
 *   <li>{@link #PUT}    — body as string</li>
 *   <li>{@link #DELETE} — no body</li>
 * </ul>
 */
public class RequestMethod {

    /** Strategy for HTTP GET — ignores the body and calls {@code builder.GET().build()}. */
    public static final BuildStrategy GET    = (builder, body) -> builder.GET().build();

    /** Strategy for HTTP POST — publishes the body string via {@code BodyPublishers.ofString()}. */
    public static final BuildStrategy POST   = (builder, body) -> builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();

    /** Strategy for HTTP PUT — publishes the body string via {@code BodyPublishers.ofString()}. */
    public static final BuildStrategy PUT    = (builder, body) -> builder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();

    /** Strategy for HTTP DELETE — ignores the body and calls {@code builder.DELETE().build()}. */
    public static final BuildStrategy DELETE = (builder, body) -> builder.DELETE().build();

    /** The {@link HttpClient} used to send the request. */
    private final HttpClient client;

    /** The fully-resolved request URI string. */
    private final String uri;

    /** The request body; defaults to an empty string when not applicable. */
    private final String body;

    /**
     * HTTP header strings in alternating {@code name, value} format.
     * Applied to the builder only when non-null and non-empty.
     */
    private final String[] headers;

    /** The build strategy that applies the HTTP verb (and body if relevant) to the builder. */
    private final BuildStrategy strategy;

    /**
     * @param client   the HTTP client to send with
     * @param uri      the fully-resolved request URL
     * @param body     the request body; {@code null} is treated as empty string
     * @param headers  alternating header name/value pairs; may be {@code null} or empty
     * @param strategy the verb-specific build strategy
     */
    public RequestMethod(HttpClient client, String uri, String body,
                         String[] headers, BuildStrategy strategy) {
        this.client   = client;
        this.uri      = uri;
        this.body     = body != null ? body : "";
        this.headers  = headers;
        this.strategy = strategy;
    }

    /**
     * Sends the request synchronously and returns the response body as a string.
     *
     * @return the HTTP response body
     * @throws Exception if the request fails (connection error, timeout, etc.)
     */
    public String execute() throws Exception {
        return client
                .send(strategy.apply(freshBuilder(), body), HttpResponse.BodyHandlers.ofString())
                .body();
    }

    /**
     * Sends the request asynchronously and returns a future that resolves to the response body.
     *
     * @return a {@link CompletableFuture} that completes with the HTTP response body string
     */
    public CompletableFuture<String> executeAsync() {
        return client
                .sendAsync(strategy.apply(freshBuilder(), body), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Creates a fresh {@link HttpRequest.Builder} with the URI and headers pre-configured.
     * Called once per {@link #execute()} or {@link #executeAsync()} invocation to ensure
     * the builder is not reused across calls.
     *
     * @return a new builder ready to have the HTTP verb applied by the {@link BuildStrategy}
     */
    private HttpRequest.Builder freshBuilder() {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(uri));
        if (headers != null && headers.length > 0) {
            builder.headers(headers);
        }
        return builder;
    }
}