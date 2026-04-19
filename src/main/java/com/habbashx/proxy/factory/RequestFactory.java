package com.habbashx.proxy.factory;

import com.habbashx.Interceptor.*;
import com.habbashx.cache.method.MethodCache;
import com.habbashx.cache.method.meta.MethodMeta;
import com.habbashx.executors.Executor;
import com.habbashx.executors.HttpExecutor;
import com.habbashx.json.serializer.JacksonSerializer;
import com.habbashx.json.serializer.JsonSerializer;
import com.habbashx.proxy.engine.RequestProxyEngine;
import com.habbashx.request.registry.RequestRegistry;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static net.bytebuddy.matcher.ElementMatchers.any;

/**
 * Public entry point of the framework — assembles all components and produces a live proxy
 * for a given API class.
 *
 * <p>Two usage modes:
 *
 * <p><b>Simple</b> — zero configuration, uses all defaults:
 * <pre>{@code
 * ApiService service = RequestFactory.create(ApiService.class);
 * }</pre>
 *
 * <p><b>Custom</b> — supply a custom HTTP client, serializer, executor, or interceptors
 * via the builder:
 * <pre>{@code
 * ApiService service = RequestFactory.builder()
 *         .client(customHttpClient)
 *         .serializer(new GsonSerializer())
 *         .interceptors(List.of(new LoggingInterceptor(), new AuthInterceptor()))
 *         .buildRequest(ApiService.class);
 * }</pre>
 *
 * <p>This class is not instantiable; use the static {@link #create(Class)} method or
 * {@link #builder()} for custom configuration.
 */
@SuppressWarnings("unchecked")
public final class RequestFactory {

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    /** Utility class — no instances. */
    private RequestFactory() {}

    /**
     * Proxy class cache — ByteBuddy subclass generation is expensive and the result
     * is immutable, so it is safe to share across all calls for the same API class.
     */
    private static final Map<Class<?>, Class<?>> PROXY_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a proxy for {@code apiClass} using the default configuration:
     * a plain {@link HttpClient}, {@link JacksonSerializer}, and the built-in
     * {@link HeaderInterceptor} + {@link PathQueryInterceptor} pipeline.
     *
     * @param <T>      the API type
     * @param apiClass the class whose methods should be proxied
     * @return a fully wired proxy instance ready to make HTTP calls
     */
    public static <T> @NotNull T create(Class<T> apiClass) {
        return create(
                apiClass,
                SHARED_CLIENT,
                new CopyOnWriteArrayList<>(),
                null,
                new JacksonSerializer()
        );
    }

    /**
     * Returns a new {@link RequestFactoryBuilder} for custom configuration.
     *
     * @return a fresh builder
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull RequestFactoryBuilder builder() {
        return new RequestFactoryBuilder();
    }

    /**
     * Core wiring method shared by {@link #create(Class)} and
     * {@link RequestFactoryBuilder#buildRequest(Class)}.
     *
     * <p>Builds the full object graph for a single proxy:
     * <ol>
     *   <li>Creates a {@link MethodCache} for lazy annotation metadata parsing.</li>
     *   <li>Prepends the built-in interceptors ({@link PathQueryInterceptor},
     *       {@link HeaderInterceptor}) before any user-supplied ones.</li>
     *   <li>Wires {@link RequestRegistry}, {@link Executor}, {@link InterceptorHierarchy},
     *       and {@link RequestProxyEngine}.</li>
     *   <li>Generates (or retrieves from cache) a ByteBuddy proxy subclass and
     *       instantiates it.</li>
     * </ol>
     *
     * @param clazz        the API class to proxy
     * @param client       the HTTP client to use for dispatch
     * @param interceptors user-supplied interceptors; built-in ones are prepended
     * @param executor     custom executor, or {@code null} to use {@link HttpExecutor}
     * @param serializer   the JSON serializer
     * @param <T>          the API type
     * @return a proxy instance
     */
    private static <T> @NotNull T create(Class<T> clazz,
                                         HttpClient client,
                                         List<Interceptor> interceptors,
                                         Executor executor,
                                         JsonSerializer serializer) {
        try {
            MethodCache<MethodMeta> methodCache = new MethodCache<>(MethodMeta::new);

            RequestRegistry registry = new RequestRegistry(client);

            if (executor == null) {
                executor = new HttpExecutor(registry, serializer);
            }

            List<Interceptor> all = new ArrayList<>();
            all.add(new PathQueryInterceptor(methodCache));
            all.add(new HeaderInterceptor(methodCache));
            all.add(new ResponseSaverInterceptor(methodCache));
            all.addAll(interceptors);

            RequestProxyEngine engine = new RequestProxyEngine(
                    new InterceptorHierarchy(all), executor, methodCache);

            Class<?> proxyClass = PROXY_CACHE.computeIfAbsent(clazz, cls ->
                    {
                        try {
                            return new ByteBuddy()
                                    .subclass(cls)
                                    .method(any())
                                    .intercept(MethodDelegation.to(engine))
                                    .make()
                                    .load(cls.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.privateLookupIn(cls, MethodHandles.lookup())))
                                    .getLoaded();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            return (T) proxyClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fluent builder for custom {@code RequestFactory} configuration.
     *
     * <p>All fields are optional — any field left unset falls back to its default.
     */
    public static final class RequestFactoryBuilder {

        private HttpClient     client;
        private List<Interceptor> interceptors;
        private Executor       executor;
        private JsonSerializer serializer;

        private RequestFactoryBuilder() {}

        /**
         * Sets a custom {@link HttpClient} (timeouts, SSL, proxy settings, etc.).
         * Defaults to {@link HttpClient#newHttpClient()} if not set.
         *
         * @param client the HTTP client to use; {@code null} restores the default
         * @return this builder
         */
        public RequestFactoryBuilder client(HttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the user-supplied interceptor list. The built-in
         * {@link PathQueryInterceptor} and {@link HeaderInterceptor} are always
         * prepended and cannot be removed.
         *
         * @param interceptors interceptors to add after the built-in ones;
         *                     {@code null} is treated as an empty list
         * @return this builder
         */
        public RequestFactoryBuilder interceptors(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        /**
         * Sets a custom {@link Executor} to replace {@link HttpExecutor}.
         * Useful for testing or alternative transport mechanisms.
         * Defaults to {@link HttpExecutor} if not set.
         *
         * @param executor the executor to use; {@code null} restores the default
         * @return this builder
         */
        public RequestFactoryBuilder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Sets a custom {@link JsonSerializer} (e.g. Gson, Moshi).
         * Defaults to {@link JacksonSerializer} if not set.
         *
         * @param serializer the serializer to use; {@code null} restores the default
         * @return this builder
         */
        public RequestFactoryBuilder serializer(JsonSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Builds and returns a proxy for the given API class using this builder's
         * configuration, falling back to defaults for any unset field.
         *
         * @param <T>   the API type
         * @param clazz the class to proxy
         * @return a fully wired proxy instance
         */
        public <T> @NotNull T buildRequest(Class<?> clazz) {
            return (T) RequestFactory.create(
                    clazz,
                    client != null ? client : HttpClient.newHttpClient(),
                    interceptors != null ? interceptors : new CopyOnWriteArrayList<>(),
                    executor,
                    serializer != null ? serializer : new JacksonSerializer()
            );
        }
    }
}