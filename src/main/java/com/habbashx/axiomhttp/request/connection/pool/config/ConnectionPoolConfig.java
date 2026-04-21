package com.habbashx.axiomhttp.request.connection.pool.config;

import com.habbashx.axiomhttp.proxy.factory.RequestFactory;

import java.time.Duration;

/**
 * Configuration for the HTTP connection pool used by {@link RequestFactory}.
 *
 * <p>Controls how the underlying {@link java.net.http.HttpClient} is created and how
 * many concurrent connections are allowed. Two modes are available:
 *
 * <ul>
 *   <li>{@link ClientMode#SIMPLE} — a plain {@code HttpClient} backed by a
 *       virtual-thread executor with no concurrency cap. This is the default.</li>
 *   <li>{@link ClientMode#POOLED} — the same executor wrapped in a {@link java.util.concurrent.Semaphore}
 *       that blocks new requests once {@code maxConnections} are in-flight, releasing
 *       a permit when each request completes.</li>
 * </ul>
 *
 * <p>Use the static {@link #builder()} to create an instance:
 * <pre>{@code
 * ConnectionPoolConfig config = ConnectionPoolConfig.builder()
 *         .mode(ClientMode.POOLED)
 *         .maxConnections(50)
 *         .connectTimeout(Duration.ofSeconds(5))
 *         .build();
 *
 * ApiService service = RequestFactory.builder()
 *         .connectionPoolConfig(config)
 *         .buildRequest(ApiService.class);
 * }</pre>
 */
public class ConnectionPoolConfig {

    /**
     * Determines how the HTTP client's executor is configured.
     */
    public enum ClientMode {
        /**
         * Plain {@code HttpClient} with a virtual-thread executor and no concurrency cap.
         * Suitable for most use-cases.
         */
        SIMPLE,
        /**
         * Semaphore-bounded executor that limits the number of concurrent in-flight requests
         * to {@link ConnectionPoolConfig#getMaxConnections()}.
         */
        POOLED
    }

    /** The selected client mode. */
    private final ClientMode clientMode;

    /** Maximum concurrent connections; only meaningful in {@link ClientMode#POOLED} mode. */
    private final int maxConnections;

    /** Timeout applied when establishing a TCP connection to the server. */
    private final Duration connectionTimeout;

    /**
     * Creates a {@code ConnectionPoolConfig} from the given builder.
     *
     * @param builder the configured builder; must not be {@code null}
     */
    public ConnectionPoolConfig(Builder builder) {
        this.clientMode        = builder.mode;
        this.maxConnections    = builder.maxConnections;
        this.connectionTimeout = builder.connectionTimeout;
    }

    /**
     * Returns the client mode (SIMPLE or POOLED).
     *
     * @return the configured {@link ClientMode}
     */
    public ClientMode getMode() {
        return clientMode;
    }

    /**
     * Returns the maximum number of concurrent in-flight connections.
     * Only used when the mode is {@link ClientMode#POOLED}.
     *
     * @return the connection cap; default is {@code 20}
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Returns the TCP connection timeout applied when opening new connections.
     *
     * @return the connect timeout; default is {@code 10 seconds}
     */
    public Duration getConnectTimeout() {
        return connectionTimeout;
    }

    /**
     * Returns a new {@link Builder} with default values pre-set:
     * {@code mode = SIMPLE}, {@code maxConnections = 20}, {@code connectTimeout = 10s}.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ConnectionPoolConfig}.
     */
    public static class Builder {

        private ClientMode mode            = ClientMode.SIMPLE;
        private int        maxConnections  = 20;
        private Duration   connectionTimeout = Duration.ofSeconds(10);

        /**
         * Sets the client mode.
         *
         * @param val the desired mode
         * @return this builder
         */
        public Builder mode(ClientMode val) {
            this.mode = val;
            return this;
        }

        /**
         * Sets the maximum number of concurrent connections.
         * Only relevant when mode is {@link ClientMode#POOLED}.
         *
         * @param val the connection cap; must be positive
         * @return this builder
         */
        public Builder maxConnections(int val) {
            this.maxConnections = val;
            return this;
        }

        /**
         * Sets the TCP connection timeout.
         *
         * @param val the timeout duration; must be positive
         * @return this builder
         */
        public Builder connectTimeout(Duration val) {
            this.connectionTimeout = val;
            return this;
        }

        /**
         * Builds and returns the {@link ConnectionPoolConfig}.
         *
         * @return a new immutable {@code ConnectionPoolConfig}
         */
        public ConnectionPoolConfig build() {
            return new ConnectionPoolConfig(this);
        }
    }
}
