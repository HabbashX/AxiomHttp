package com.habbashx.axiomhttp.method.cache;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe, lazy cache that maps a {@link Method} to a computed value of type {@code T}.
 *
 * <p>The first time a method is requested, the provided {@code parser} function is invoked to
 * produce the value, which is then stored. Subsequent requests for the same method return the
 * cached value without invoking the parser again.
 *
 * <p>Backed by {@link ConcurrentHashMap} for safe use across multiple threads.
 *
 * @param <T> the type of the cached value (typically {@code MethodMeta})
 */
public class MethodCache<T> {

    /** Internal store mapping each method to its computed metadata. */
    private final Map<Method, T> cache = new ConcurrentHashMap<>();

    /**
     * Function invoked once per method to compute and populate the cached value.
     * Typically {@code MethodMeta::new}.
     */
    private final Function<Method, T> parser;

    /**
     * Creates a new cache with the given parsing function.
     *
     * @param parser function that computes the cached value from a {@link Method}; must not be {@code null}
     */
    public MethodCache(Function<Method, T> parser) {
        this.parser = parser;
    }

    /**
     * Returns the cached value for the given method, computing it on first access.
     *
     * @param method the method whose metadata is requested
     * @return the cached (or freshly computed) value for {@code method}
     */
    public T get(Method method) {
        return cache.computeIfAbsent(method, parser);
    }
}