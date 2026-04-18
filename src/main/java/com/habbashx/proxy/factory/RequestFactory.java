package com.habbashx.proxy.factory;

import com.habbashx.Interceptor.HeaderInterceptor;
import com.habbashx.Interceptor.InterceptorHierarchy;
import com.habbashx.Interceptor.PathQueryInterceptor;
import com.habbashx.cache.method.MethodCache;
import com.habbashx.cache.method.meta.MethodMeta;
import com.habbashx.executors.HttpExecutor;
import com.habbashx.json.serializer.JacksonSerializer;
import com.habbashx.proxy.engine.RequestProxyEngine;
import com.habbashx.request.registry.RequestRegistry;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.any;

/**
 * Public entry point of the framework — assembles all components and produces a live proxy
 * for a given API class.
 *
 * <p>Calling {@link #create(Class)} wires together the full object graph:
 * <ol>
 *   <li>A {@link MethodCache} for lazy annotation metadata parsing.</li>
 *   <li>A {@link java.net.http.HttpClient} and {@link RequestRegistry} for HTTP dispatch.</li>
 *   <li>A {@link JacksonSerializer} for JSON (de)serialization.</li>
 *   <li>An {@link InterceptorHierarchy} containing {@link HeaderInterceptor} and
 *       {@link PathQueryInterceptor}, applied in that order.</li>
 *   <li>A {@link RequestProxyEngine} as the Byte Buddy interception target.</li>
 *   <li>A Byte Buddy-generated subclass of {@code apiClass} that delegates every method
 *       call to the engine.</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>{@code
 * UserApi api = RequestFactory.create(UserApi.class);
 * User user   = api.getUser(42);
 * }</pre>
 *
 * <p>This class is not instantiable; use the static {@code create} method.
 */
public class RequestFactory {

    /** Utility class — no instances. */
    private RequestFactory() {}

    /**
     * Creates a proxy instance for the given API class with the default configuration.
     *
     * <p>The returned object is a Byte Buddy-generated subclass of {@code apiClass} loaded into
     * a child class loader via {@link ClassLoadingStrategy.Default#WRAPPER}. Every method call
     * on the returned instance is intercepted and routed through the request pipeline.
     *
     * @param <T>      the API type
     * @param apiClass the class (or abstract class) whose methods should be proxied;
     *                 must have a public no-arg constructor
     * @return a fully wired proxy instance ready to make HTTP calls
     * @throws ReflectiveOperationException if the proxy class cannot be instantiated
     */
    public static <T> @NotNull T create(Class<T> apiClass)
            throws ReflectiveOperationException {

        MethodCache<MethodMeta> methodCache = new MethodCache<>(MethodMeta::new);

        HttpClient httpClient    = HttpClient.newHttpClient();
        RequestRegistry registry = new RequestRegistry(httpClient);
        JacksonSerializer serial = new JacksonSerializer();

        HttpExecutor executor = new HttpExecutor(registry, serial);

        InterceptorHierarchy hierarchy = new InterceptorHierarchy(List.of(
                new HeaderInterceptor(methodCache),
                new PathQueryInterceptor(methodCache)
        ));

        RequestProxyEngine engine = new RequestProxyEngine(hierarchy, executor, methodCache);

        return new ByteBuddy()
                .subclass(apiClass)
                .method(any())
                .intercept(MethodDelegation.to(engine))
                .make()
                .load(apiClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }
}