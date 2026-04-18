package com.habbashx.request.registry;

import com.habbashx.request.HttpMethod;
import com.habbashx.request.RequestMethod;
import com.habbashx.request.strategy.BuildStrategy;

import java.net.http.HttpClient;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Registry that maps each {@link HttpMethod} to its {@link BuildStrategy} and dispatches
 * HTTP requests both synchronously and asynchronously.
 *
 * <p>On construction, the four built-in strategies (GET, POST, PUT, DELETE) are registered
 * from {@link RequestMethod}. {@link com.habbashx.executors.HttpExecutor} calls this class
 * to send requests without needing to know which strategy is used for which verb.
 *
 * <p>Uses an {@link EnumMap} for O(1) strategy lookup with minimal memory overhead.
 */
public class RequestRegistry {

    /** Maps each HTTP method enum constant to its corresponding build strategy. */
    private final Map<HttpMethod, BuildStrategy> strategies = new EnumMap<>(HttpMethod.class);

    /** Shared HTTP client passed to every assembled {@link RequestMethod}. */
    private final HttpClient client;

    /**
     * Creates a registry and registers the four default HTTP method strategies.
     *
     * @param client the {@link HttpClient} to use for all dispatched requests
     */
    public RequestRegistry(HttpClient client) {
        this.client = client;
        strategies.put(HttpMethod.GET,    RequestMethod.GET);
        strategies.put(HttpMethod.POST,   RequestMethod.POST);
        strategies.put(HttpMethod.PUT,    RequestMethod.PUT);
        strategies.put(HttpMethod.DELETE, RequestMethod.DELETE);
    }

    /**
     * Sends an HTTP request synchronously and returns the response body as a string.
     *
     * @param method  the HTTP method to use
     * @param url     the fully-resolved request URL
     * @param body    the request body; may be empty for GET/DELETE
     * @param headers alternating header name/value pairs
     * @return the raw HTTP response body string
     * @throws Exception if the request fails at the transport layer
     * @throws IllegalArgumentException if no strategy is registered for {@code method}
     */
    public String execute(HttpMethod method, String url, String body, String[] headers) throws Exception {
        return assemble(method, url, body, headers).execute();
    }

    /**
     * Sends an HTTP request asynchronously and returns a future resolving to the response body.
     *
     * @param method  the HTTP method to use
     * @param url     the fully-resolved request URL
     * @param body    the request body; may be empty for GET/DELETE
     * @param headers alternating header name/value pairs
     * @return a {@link CompletableFuture} that completes with the raw response body string
     * @throws IllegalArgumentException if no strategy is registered for {@code method}
     */
    public CompletableFuture<String> executeAsync(HttpMethod method, String url, String body, String[] headers) {
        return assemble(method, url, body, headers).executeAsync();
    }

    /**
     * Looks up the strategy for {@code method} and assembles a {@link RequestMethod} instance.
     *
     * @param method  the HTTP method
     * @param url     the target URL
     * @param body    the request body
     * @param headers the HTTP headers
     * @return a configured {@link RequestMethod} ready to be executed
     * @throws IllegalArgumentException if {@code method} has no registered strategy
     */
    private RequestMethod assemble(HttpMethod method, String url, String body, String[] headers) {
        BuildStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for HTTP method: " + method);
        }
        return new RequestMethod(client, url, body, headers, strategy);
    }
}