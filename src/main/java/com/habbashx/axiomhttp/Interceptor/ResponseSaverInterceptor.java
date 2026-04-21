package com.habbashx.axiomhttp.Interceptor;

import com.habbashx.axiomhttp.annotation.SaveResponse;
import com.habbashx.axiomhttp.method.cache.MethodCache;
import com.habbashx.axiomhttp.method.meta.MethodMeta;
import com.habbashx.axiomhttp.proxy.MethodContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interceptor that saves HTTP responses to files for methods annotated with {@link SaveResponse}.
 *
 * <p>Built-in interceptor — always in the pipeline, zero cost on methods that carry
 * no {@code @SaveResponse} annotation. No manual registration needed; just annotate
 * the methods you want saved and the framework handles the rest.
 *
 * <p>File name generation follows two strategies:
 * <ul>
 *   <li><b>Custom</b> — if {@link SaveResponse#fileName()} is non-empty, the file is
 *       saved as {@code {path}/{fileName}.{ext}}.</li>
 *   <li><b>Auto</b> — if {@link SaveResponse#fileName()} is empty, the name is generated
 *       from the HTTP method, sanitized URL path, and a timestamp:
 *       {@code {path}/{METHOD}_{urlPath}_{timestamp}.{ext}}.</li>
 * </ul>
 *
 * <p>Usage — just annotate the methods you want saved:
 * <pre>{@code
 * // saved as responses/users.json
 * @SaveResponse(path = "responses", format = SaveFormat.JSON, fileName = "users")
 * @Request(uri = "https://api.example.com/users/{id}", method = "GET")
 * User getUser(@Path("id") long id);
 *
 * // saved as responses/GET_posts_20260418_153042.txt
 * @SaveResponse(path = "responses", format = SaveFormat.TXT)
 * @Request(uri = "https://api.example.com/posts", method = "GET")
 * List<Post> getPosts();
 * }</pre>
 *
 * <p>If saving fails the error is printed to stderr and the response is returned
 * normally — a disk failure never breaks the request pipeline.
 */
public class ResponseSaverInterceptor implements Interceptor {

    /**
     * The file format used when saving a response.
     */
    public enum SaveFormat {
        /** Wraps the response in a JSON envelope with metadata. */
        JSON,
        /** Writes a plain-text report with metadata and the response body. */
        TXT
    }

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Cache used to retrieve per-method annotation metadata. */
    private final MethodCache<MethodMeta> methodCache;

    /**
     * @param methodCache shared cache of per-method annotation metadata
     */
    public ResponseSaverInterceptor(MethodCache<MethodMeta> methodCache) {
        this.methodCache = methodCache;
    }

    /**
     * No-op — this interceptor only acts on responses.
     *
     * @param ctx the current request context
     * @return the context unchanged
     */
    @Override
    public MethodContext before(MethodContext ctx) {
        return ctx; // nothing needed here
    }

    /**
     * Saves the response to a file if the invoked method carries {@link SaveResponse}.
     * Methods without the annotation are silently skipped.
     *
     * @param response the deserialized response object
     * @param ctx      the request context carrying the URL and HTTP method
     * @return the response unchanged
     */
    @Override
    public Object after(Object response, MethodContext ctx) {
        SaveResponse annotation = methodCache
                .get(ctx.getReflectionMethod())
                .getSaveResponseAnnotation();

        if (annotation == null) return response;

        try {
            Path outputDir = Path.of(annotation.path());
            ensureOutputDir(outputDir);

            Path file = outputDir.resolve(buildFileName(annotation, ctx));
            Files.writeString(file, buildContent(response, annotation.format(), ctx),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.err.println("[ResponseSaverInterceptor] Failed to save response: " + e.getMessage());
        }

        return response;
    }

    /**
     * Creates the output directory and any missing parents if it does not exist.
     *
     * @param outputDir the directory to create
     * @throws IOException if the directory cannot be created
     */
    private void ensureOutputDir(Path outputDir) throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    /**
     * Resolves the file name using the configured strategy.
     *
     * <ul>
     *   <li>Non-empty {@link SaveResponse#fileName()} → {@code {fileName}.{ext}}</li>
     *   <li>Empty {@link SaveResponse#fileName()} → {@code {METHOD}_{sanitizedUrl}_{timestamp}.{ext}}</li>
     * </ul>
     *
     * @param annotation the {@code @SaveResponse} annotation on the method
     * @param ctx        the request context
     * @return the resolved file name including extension
     */
    private String buildFileName(SaveResponse annotation, MethodContext ctx) {
        String extension = annotation.format() == SaveFormat.JSON ? "json" : "txt";

        if (!annotation.fileName().isEmpty()) {
            return annotation.fileName() + "." + extension;
        }

        String method    = ctx.getMethod().toUpperCase();
        String urlPart   = sanitizeUrl(ctx.getUrl());
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return method + "_" + urlPart + "_" + timestamp + "." + extension;
    }

    /**
     * Strips the scheme and domain, drops the query string, and replaces
     * non-alphanumeric characters with underscores to produce a safe file name segment.
     *
     * <p>Example: {@code https://api.example.com/users/42?page=1} → {@code users_42}
     *
     * @param url the full request URL
     * @return a sanitized string safe for use in a file name
     */
    private String sanitizeUrl(String url) {
        String path = url.replaceAll("https?://[^/]+", "")
                .replaceAll("\\?.*", "")
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return path.isEmpty() ? "root" : path;
    }

    /**
     * Formats the response as a JSON envelope or plain-text report.
     *
     * @param response the response object; {@code toString()} is used for the body
     * @param format   the output format
     * @param ctx      the request context
     * @return the formatted string to write to the file
     */
    private String buildContent(Object response, SaveFormat format, MethodContext ctx) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String body      = response != null ? response.toString() : "null";

        return switch (format) {
            case JSON -> """
                    {
                      "timestamp": "%s",
                      "method": "%s",
                      "url": "%s",
                      "response": %s
                    }
                    """.formatted(
                    timestamp,
                    ctx.getMethod().toUpperCase(),
                    ctx.getUrl(),
                    isJsonLike(body) ? body : "\"" + escapeJson(body) + "\""
            );
            case TXT -> """
                    Timestamp : %s
                    Method    : %s
                    URL       : %s
                    Response  :
                    %s
                    """.formatted(
                    timestamp,
                    ctx.getMethod().toUpperCase(),
                    ctx.getUrl(),
                    body
            );
        };
    }

    /**
     * Returns {@code true} if the string looks like a raw JSON value so it can be
     * embedded directly without quoting.
     *
     * @param value the string to check
     * @return {@code true} if the value should be embedded as-is
     */
    private boolean isJsonLike(String value) {
        if (value == null || value.isEmpty()) return false;
        char first = value.charAt(0);
        return first == '{' || first == '[' || first == '"'
                || value.equals("true") || value.equals("false")
                || value.equals("null")
                || value.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Escapes characters illegal inside a JSON string literal.
     *
     * @param value the raw string
     * @return the escaped string
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}