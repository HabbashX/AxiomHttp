# AxiomHttp

A lightweight, annotation-driven Java HTTP client library built on ByteBuddy runtime proxies, an interceptor pipeline, and a method metadata caching system.

Define your HTTP API as a plain Java class. Annotate the methods. AxiomHttp handles everything else — request building, URL resolution, header injection, execution, and JSON deserialization.

---

## New Features
- NoCookies Annotation , with NoCookies annotation you cannot send a Cookies to the server
- CleanResponse return clean response from the endpoint , it only supports String 
- ApiService class that return all the endpoint properties and headers 
- ConnectionPoolConfig makes you to configure connection pool with different configuration depends on what you want
- RequestFactory provide a builtin Connection config with different properties
---

## Requirements

- Java 21+
- Maven

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| `byte-buddy` | 1.14.18 | Runtime proxy generation |
| `jackson-databind` | 2.21.2 | JSON serialization / deserialization |
| `jetbrains-annotations` | 26.0.2 | `@NotNull` / nullability hints |

---

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.habbashx</groupId>
    <artifactId>AxiomHttp</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start

```java
public interface ApiService {

    @Request(uri = "https://jsonplaceholder.typicode.com/posts/1", method = "GET")
    String getPost();
}

public class Main {
    public static void main(String[] args) {
        ApiService service = RequestFactory.create(ApiService.class);
        String response = service.getPost();
        System.out.println(response);
    }
}
```

That's it. No configuration, no boilerplate.

---

## Annotations

### `@Request`

Marks a method as an HTTP endpoint. Required on every method that should make a network call.

```java
@Request(uri = "https://api.example.com/users/1", method = "GET")
String getUser();
```

| Field | Type | Required | Description |
|---|---|---|---|
| `uri` | `String` | Yes | Full URL or URL template with `{placeholder}` segments |
| `method` | `String` | Yes | HTTP verb — `GET`, `POST`, `PUT`, `DELETE` (case-insensitive) |
| `body` | `String` | No | Static request body string, defaults to empty |

---

### `@Path`

Binds a method parameter to a `{placeholder}` in the URI template.

```java
@Request(uri = "https://api.example.com/users/{id}/posts/{postId}", method = "GET")
String getUserPost(@Path("id") long userId, @Path("postId") long postId);
```

At runtime `{id}` and `{postId}` are replaced with the argument values:
```
https://api.example.com/users/42/posts/7
```

---

### `@Query`

Binds a method parameter to a URL query string key.

```java
@Request(uri = "https://api.example.com/search", method = "GET")
String search(@Query("q") String term, @Query("page") int page);
```

Multiple `@Query` parameters are joined in declaration order:
```
https://api.example.com/search?q=java&page=2
```

---

### `@Headers`

Declares static HTTP headers sent with the request.
Values are alternating name/value pairs passed directly to `HttpRequest.Builder#headers()`.

```java
@Request(uri = "https://api.example.com/private", method = "GET")
@Headers({"Authorization", "Bearer my-token", "Accept", "application/json"})
String getPrivateData();
```

---

### `@SaveResponse`

Automatically saves the response to a file after the request completes.
Only methods carrying this annotation are affected — all other methods are untouched.

```java
// Custom file name — saves to responses/users.json
@SaveResponse(path = "responses", format = SaveFormat.JSON, fileName = "users")
@Request(uri = "https://api.example.com/users/{id}", method = "GET")
String getUser(@Path("id") long id);

// Auto-generated file name — saves to responses/GET_users_42_20260418_153042.txt
@SaveResponse(path = "responses", format = SaveFormat.TXT)
@Request(uri = "https://api.example.com/users/{id}", method = "GET")
String getUser(@Path("id") long id);
```

| Field | Type | Required | Description |
|---|---|---|---|
| `path` | `String` | Yes | Output directory, created automatically if missing |
| `format` | `SaveFormat` | Yes | `SaveFormat.JSON` or `SaveFormat.TXT` |
| `fileName` | `String` | No | Custom file name without extension. Auto-generated if empty |

**File name strategies:**

| `fileName` value | Result |
|---|---|
| `"users"` | `responses/users.json` |
| `""` (default) | `responses/GET_users_42_20260418_153042.json` |

**JSON output:**
```json
{
  "timestamp": "2026-04-18T15:30:42",
  "method": "GET",
  "url": "https://api.example.com/users/42",
  "response": { "id": 42, "name": "John" }
}
```

**TXT output:**
```
Timestamp : 2026-04-18T15:30:42
Method    : GET
URL       : https://api.example.com/users/42
Response  :
{"id":42,"name":"John"}
```

---

## Usage Examples

### GET with path variable

```java
public interface UserApi {

    @Request(uri = "https://api.example.com/users/{id}", method = "GET")
    String getUser(@Path("id") long id);
}

UserApi api = RequestFactory.create(UserApi.class);
String user = api.getUser(42);
```

---

### GET with query parameters

```java
public interface SearchApi {

    @Request(uri = "https://api.example.com/search", method = "GET")
    String search(@Query("q") String term, @Query("page") int page);
}

SearchApi api = RequestFactory.create(SearchApi.class);
String results = api.search("java", 1);
// → https://api.example.com/search?q=java&page=1
```

---

### POST with static body

```java
public interface PostApi {

    @Request(
        uri    = "https://api.example.com/posts",
        method = "POST",
        body   = "{\"title\":\"Hello\",\"body\":\"World\"}"
    )
    String createPost();
}
```

---

### Headers

```java
public interface SecureApi {

    @Request(uri = "https://api.example.com/dashboard", method = "GET")
    @Headers({"Authorization", "Bearer token123", "Accept", "application/json"})
    String getDashboard();
}
```

---

### Async request

Declare `CompletableFuture<T>` as the return type — AxiomHttp detects it automatically
and dispatches the request asynchronously without blocking.

```java
public interface AsyncApi {

    @Request(uri = "https://api.example.com/users/{id}", method = "GET")
    CompletableFuture<String> getUserAsync(@Path("id") long id);
}

AsyncApi api = RequestFactory.create(AsyncApi.class);
api.getUserAsync(42).thenAccept(System.out::println);
```

---

### Save response to file

```java
public interface ReportApi {

    @SaveResponse(path = "reports", format = SaveFormat.JSON, fileName = "user_42")
    @Request(uri = "https://api.example.com/users/{id}", method = "GET")
    String getUser(@Path("id") long id);
}

ReportApi api = RequestFactory.create(ReportApi.class);
String user = api.getUser(42);
// response returned normally AND saved to reports/user_42.json
```

---

### Mixed parameters

```java
public interface PostApi {

    @Request(uri = "https://api.example.com/users/{id}/posts", method = "GET")
    String getUserPosts(@Path("id") String userId, @Query("limit") int limit);
}

// Produces: https://api.example.com/users/99/posts?limit=10
api.getUserPosts("99", 10);
```

---

## Custom Configuration

Use `RequestFactory.builder()` when you need to customize the HTTP client, JSON serializer,
executor, or add cross-cutting interceptors.

```java
ApiService service = RequestFactory.builder()
        .client(
            HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        )
        .serializer(new GsonSerializer())
        .interceptors(List.of(
            new LoggingInterceptor(),
            new AuthInterceptor("my-token")
        ))
        .buildRequest(ApiService.class);
```

All builder fields are optional — any field left unset falls back to its default.

| Builder method | Default | Description |
|---|---|---|
| `.client(HttpClient)` | `HttpClient.newHttpClient()` | Custom HTTP client — timeouts, SSL, proxy |
| `.serializer(JsonSerializer)` | `JacksonSerializer` | Swap Jackson for any other JSON library |
| `.executor(Executor)` | `HttpExecutor` | Custom execution layer for testing or alternate transports |
| `.interceptors(List)` | empty list | User-supplied interceptors, run after built-in ones |

---

## Interceptor System

Every request passes through an ordered interceptor pipeline. Each interceptor has two hooks:

```java
public interface Interceptor {
    MethodContext before(MethodContext ctx);           // runs before the HTTP call
    Object after(Object response, MethodContext ctx); // runs after deserialization
}
```

**Built-in interceptors** (always active, registered automatically):

| Interceptor | Trigger | What it does |
|---|---|---|
| `PathQueryInterceptor` | always | Resolves `{path}` variables and appends `?query=params` |
| `HeaderInterceptor` | `@Headers` present | Injects declared headers into the request |
| `ResponseSaverInterceptor` | `@SaveResponse` present | Saves the response to a file, no-op otherwise |

**Custom interceptors** run after the built-ins. Implement `Interceptor` and register via the builder:

```java
public class LoggingInterceptor implements Interceptor {

    @Override
    public MethodContext before(MethodContext ctx) {
        System.out.println("→ " + ctx.getMethod() + " " + ctx.getUrl());
        return ctx;
    }

    @Override
    public Object after(Object response, MethodContext ctx) {
        System.out.println("← response received");
        return response;
    }
}
```

```java
ApiService service = RequestFactory.builder()
        .interceptors(List.of(new LoggingInterceptor()))
        .buildRequest(ApiService.class);
```

---

## Architecture

```
Method call on proxy
        ↓
RequestProxyEngine.intercept()
        ↓
MethodCache → MethodMeta  (annotations parsed once, cached forever)
        ↓
InterceptorHierarchy.applyBefore()
  → PathQueryInterceptor      resolves {path} variables and ?query=params
  → HeaderInterceptor         injects @Headers values
  → [user interceptors]       custom before-logic
        ↓
HttpExecutor.execute()
  → RequestRegistry           selects build strategy (GET/POST/PUT/DELETE)
  → RequestMethod             fires JDK HttpClient request
  → JacksonSerializer         deserializes response body into return type
        ↓
InterceptorHierarchy.applyAfter()
  → ResponseSaverInterceptor  saves file if @SaveResponse present, skips otherwise
  → [user interceptors]       custom after-logic
        ↓
Return deserialized response to caller
```

---

## Design Principles

**Reflection runs once.** Every method's annotations are parsed on the first call and stored
in `MethodCache<MethodMeta>`. All subsequent calls read from the cache — zero reflection
overhead at steady state.

**Execution layer is annotation-free.** `HttpExecutor`, `RequestRegistry`, and `RequestMethod`
know nothing about annotations. They only see a fully-resolved `MethodContext`. Annotations
are a concern of the interceptors, not the executor.

**Built-in interceptors are zero-cost when inactive.** `ResponseSaverInterceptor` checks for
`@SaveResponse` and returns immediately if absent. No file I/O, no string building, no overhead
on unannotated methods. Same principle applies to `HeaderInterceptor` with no `@Headers`.

**Proxy classes are cached.** ByteBuddy subclass generation is expensive. Generated proxy
classes are stored in a `ConcurrentHashMap` and reused across all calls for the same API class.

**Disk failures never break requests.** `ResponseSaverInterceptor` catches `IOException`,
prints to stderr, and returns the response unchanged. A full disk never propagates an
exception to your application.

---

## VERSION
1.0.2

## License

MIT License — see [LICENSE](LICENSE) for details.