package com.habbashx.axiomhttp.proxy;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Mutable bag of state representing a single in-flight HTTP request as it travels through
 * the interceptor pipeline.
 *
 * <p>Created by {@code RequestProxyEngine} from the method's {@code @Request} annotation, then
 * passed to each interceptor's {@code before()} method so it can be enriched (e.g. path variables
 * resolved, query params appended, headers injected) before being handed to the executor.
 *
 * <p>This class intentionally carries only transport-level concerns (URL, method, body, headers,
 * runtime args, and the reflected method). Deserialization metadata is an executor concern and is
 * not stored here.
 *
 * <p>Use {@link #builder()} for a fluent construction style.
 */
public class MethodContext {

    /** The fully-resolved request URL; may initially contain {@code {placeholder}} segments. */
    private String url;

    /** The HTTP verb string (e.g. {@code "GET"}, {@code "POST"}). */
    private String method;

    /** The request body; may be empty for methods that do not send a body. */
    private String body;

    /**
     * HTTP header strings in alternating {@code name, value} format as required by
     * {@code HttpRequest.Builder#headers(String...)}.
     */
    private Map<String, List<String>> headers;

    /** The runtime argument values passed to the proxy method invocation. */
    private Object[] args;

    /** The reflected proxy method, used by interceptors to look up annotation metadata. */
    private Method reflectionMethod;

    /**
     * Full constructor. Prefer {@link #builder()} for readability.
     *
     * @param url              the initial URL or URL template
     * @param method           the HTTP verb
     * @param body             the request body (may be empty)
     * @param headers          header name/value pairs
     * @param args             runtime arguments passed to the proxy method
     * @param reflectionMethod the reflected proxy method
     */
    public MethodContext(String url, String method, String body,
                         Map<String, List<String>> headers, Object[] args, Method reflectionMethod) {
        this.url              = url;
        this.method           = method;
        this.body             = body;
        this.headers          = headers;
        this.args             = args;
        this.reflectionMethod = reflectionMethod;
    }

    /** @return the current URL (may still contain unresolved placeholders if interceptors have not run yet) */
    public String getUrl() { return url; }

    /** @param url the resolved or partially-resolved URL */
    public void setUrl(String url) { this.url = url; }

    /** @return the request body string */
    public String getBody() { return body; }

    /** @param body the request body to use */
    public void setBody(String body) { this.body = body; }

    /** @return the HTTP verb string */
    public String getMethod() { return method; }

    /** @param method the HTTP verb string */
    public void setMethod(String method) { this.method = method; }

    /** @return the HTTP header strings in alternating name/value pairs */
    public Map<String, List<String>> getHeaders() { return headers; }

    /** @param headers header strings in alternating {@code "Name", "value"} format */
    public void setHeaders(Map<String, List<String>> headers) { this.headers = headers; }

    /** @return the runtime argument values passed to the proxy method */
    public Object[] getArgs() { return args; }

    /** @param args the runtime arguments */
    public void setArgs(Object[] args) { this.args = args; }

    /** @return the reflected proxy method used for annotation lookups */
    public Method getReflectionMethod() { return reflectionMethod; }

    /** @param reflectionMethod the reflected method */
    public void setReflectionMethod(Method reflectionMethod) { this.reflectionMethod = reflectionMethod; }

    /**
     * Returns a new {@link MethodContextBuilder} for fluent construction.
     *
     * @return a fresh builder
     */
    public static MethodContextBuilder builder() {
        return new MethodContextBuilder();
    }

    /**
     * Fluent builder for {@link MethodContext}.
     */
    public static class MethodContextBuilder {

        private String url;
        private String method;
        private String body;
        private Map<String,List<String>> headers;
        private Object[] args;
        private Method reflectionMethod;

        /** @param url the URL or URL template */
        public MethodContextBuilder url(String url) { this.url = url; return this; }

        /** @param method the HTTP verb */
        public MethodContextBuilder method(String method) { this.method = method; return this; }

        /** @param body the request body */
        public MethodContextBuilder body(String body) { this.body = body; return this; }

        /** @param headers alternating header name/value pairs */
        public MethodContextBuilder headers(Map<String,List<String>> headers) { this.headers = headers; return this; }

        /** @param args runtime arguments from the proxy invocation */
        public MethodContextBuilder args(Object[] args) { this.args = args; return this; }

        /** @param reflectionMethod the reflected proxy method */
        public MethodContextBuilder reflectionMethod(Method reflectionMethod) {
            this.reflectionMethod = reflectionMethod; return this;
        }

        /**
         * Builds and returns the {@link MethodContext}.
         *
         * @return a new {@code MethodContext} with the configured values
         */
        public MethodContext build() {
            return new MethodContext(url, method, body, headers, args, reflectionMethod);
        }
    }
}
