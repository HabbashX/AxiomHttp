package com.habbashx.axiomhttp.request.strategy;

import com.habbashx.axiomhttp.request.registry.RequestRegistry;
import com.habbashx.axiomhttp.request.HttpMethod;
import com.habbashx.axiomhttp.request.RequestMethod;

import java.net.http.HttpRequest;

/**
 * Functional interface that builds a fully configured {@link HttpRequest} from a
 * pre-populated {@link HttpRequest.Builder} and an optional request body string.
 *
 * <p>Each HTTP verb (GET, POST, PUT, DELETE) has its own implementation declared as a
 * static constant in {@link RequestMethod}. The strategy is selected
 * at runtime by {@link RequestRegistry} based on the
 * {@link HttpMethod} enum value.
 *
 * <p>Example — the GET strategy ignores the body and calls {@code builder.GET().build()}:
 * <pre>{@code
 * BuildStrategy GET = (builder, body) -> builder.GET().build();
 * }</pre>
 */
@FunctionalInterface
public interface BuildStrategy {

    /**
     * Applies the HTTP method (and optional body) to the given builder and returns the request.
     *
     * @param builder a pre-configured builder with URI and headers already set
     * @param body    the request body string; may be empty for methods that do not send a body
     * @return the finalized {@link HttpRequest} ready to be sent
     */
    HttpRequest apply(HttpRequest.Builder builder, String body);
}
