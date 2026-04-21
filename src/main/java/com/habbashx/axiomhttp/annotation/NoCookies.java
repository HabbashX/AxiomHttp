package com.habbashx.axiomhttp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables cookie sending and storage for the annotated method.
 *
 * <p>By default the shared {@link java.net.CookieManager} attached to the HTTP client
 * will store cookies set by the server and re-send them on subsequent requests.
 * Placing this annotation on a method signals that the request should bypass the
 * cookie jar entirely — no cookies are sent with the request and no cookies returned
 * in the response are stored.
 *
 * <p>Typical use-cases include:
 * <ul>
 *   <li>Public read-only endpoints where session state must not be attached.</li>
 *   <li>Login or token-exchange calls where sending stale cookies would be incorrect.</li>
 * </ul>
 *
 * <p>Applies to methods only; retained at runtime so the proxy engine can detect it
 * via reflection.
 *
 * <p>Example:
 * <pre>{@code
 * @NoCookies
 * @Request(uri = "https://api.example.com/public/data", method = "GET")
 * List<Item> getPublicItems();
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoCookies {}
