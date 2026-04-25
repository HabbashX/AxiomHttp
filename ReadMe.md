# AxiomHttp

A lightweight, annotation-driven Java HTTP client library built on ByteBuddy runtime proxies, an interceptor pipeline, and a method metadata caching system.

Define your HTTP API as a plain Java class. Annotate the methods. AxiomHttp handles everything else — request building, URL resolution, header injection, execution, and JSON deserialization.

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

# AxiomHttp — New Features

## Table of Contents

- [@BaseUrl](#baseurl)
- [@ExpectedStatus](#expectedstatus)
- [URL Validation](#url-validation)
- [InvalidUrlException](#invalidurlexception)
- [UnexpectedStatusException](#unexpectedstatusexception)

---

## @BaseUrl

**Package:** `com.habbashx.axiomhttp.annotation`

Declares a base URL to be prepended to all request paths defined on the annotated interface.
Instead of repeating the full URL on every `@Request` method, you declare it once at the
interface level and let each method carry only its relative path.

### Usage

```java
@BaseUrl("https://api.example.com/v1")
public interface UserService {

    @Request(url = "/users", method = "GET")
    List<User> getUsers();

    @Request(url = "/users/{id}", method = "GET")
    User getUserById(@PathParam("id") int id);

    @Request(url = "/users/{id}", method = "DELETE")
    void deleteUser(@PathParam("id") int id);
}
```

### URL Concatenation Rules

| Base URL | Relative Path | Result |
|---|---|---|
| `https://api.example.com/v1` | `/users` | `https://api.example.com/v1/users` |
| `https://api.example.com/v1/` | `/users` | `https://api.example.com/v1/users` |
| `https://api.example.com/v1` | `/users/42` | `https://api.example.com/v1/users/42` |

Double slashes at the join point are automatically collapsed — both
`"https://api.example.com/v1/"` and `"https://api.example.com/v1"` produce the same result.

### Without @BaseUrl

If `@BaseUrl` is absent, the `url` value in `@Request` must be a full absolute URL:

```java
// No @BaseUrl — each method carries the full URL
public interface UserService {

    @Request(url = "https://api.example.com/v1/users", method = "GET")
    List<User> getUsers();
}
```

### Validation

The base URL is validated at **proxy-creation time**, not per-request. A blank, malformed,
or non-HTTP/HTTPS value throws `InvalidUrlException` immediately when
`RequestFactory.create()` or `RequestFactory.builder().build()` is called.

```java
@BaseUrl("not-a-valid-url")   // throws InvalidUrlException at startup
public interface UserService { ... }
```

---

## @ExpectedStatus

**Package:** `com.habbashxaxiomhttp.annotation`

Declares the HTTP status code(s) that a request method considers successful. By default,
AxiomHttp treats any 2xx response as successful. Use `@ExpectedStatus` to override this
on a per-method basis.

If the actual response status does not match any declared value, an
`UnexpectedStatusException` is thrown carrying the status code, URL, and raw response body.

### Usage

**Single expected status:**

```java
@Request(url = "/users", method = "POST")
@ExpectedStatus(201)
User createUser(@Body User user);
```

**Multiple acceptable statuses:**

```java
@Request(url = "/users/{id}", method = "DELETE")
@ExpectedStatus({200, 204})
void deleteUser(@PathParam("id") int id);
```

**Accepting redirects:**

```java
@Request(url = "/resource", method = "GET")
@ExpectedStatus({200, 301, 302})
String getResource();
```

### Default Behaviour

If `@ExpectedStatus` is not present, the default policy applies:

| Status Range | Behaviour |
|---|---|
| 200–299 | Success — deserialize and return |
| 400–499 | Throws `UnexpectedStatusException` |
| 500–599 | Throws `UnexpectedStatusException` |

---

## URL Validation

**Package:** `com.habbashx.axiomhttp.validation`  
**Class:** `UrlValidator`

All URL validation in AxiomHttp is handled by `UrlValidator`, a stateless utility class
that runs pre-compiled regex checks at proxy-creation time. Errors surface at startup
rather than during live request handling.

### What Is Validated

| Check | When It Runs |
|---|---|
| Full absolute URL structure | Proxy creation — when no `@BaseUrl` is present |
| `@BaseUrl` value structure | Proxy creation — when `@BaseUrl` is present |
| Relative path structure | Proxy creation — when `@BaseUrl` is present |
| Port range (1–65535) | Proxy creation |
| Unresolved `{placeholder}` segments | Per-request — after `@PathParam` substitution |

### Supported URL Features

| Feature | Example |
|---|---|
| HTTP / HTTPS schemes | `https://api.example.com` |
| Optional userinfo | `https://user:pass@host.com` |
| IPv4 host | `http://192.168.1.1/path` |
| IPv6 host | `http://[::1]:8080/path` |
| Subdomain chains | `https://a.b.c.example.com` |
| Port number | `https://api.example.com:8443` |
| Path with `{placeholder}` | `/users/{id}/posts/{postId}` |
| Query string | `/search?q=hello&page=2` |
| Fragment | `/docs/api#section-3` |

### Unresolved Placeholder Detection

After `@PathParam` arguments are substituted into the URL at request time, `UrlValidator`
checks that no `{placeholder}` segments remain. This catches cases where a required
argument was not supplied.

```java
// method: User getUser(@PathParam("id") int id)
// if {id} is never substituted — throws InvalidUrlException:
// "Unresolved placeholder '{id}' in URL: https://api.example.com/v1/users/{id}"
```

### Using UrlValidator Directly

```java
// validate a full URL
UrlValidator.validateFullUrl("https://api.example.com/v1/users");

// validate a @BaseUrl value
UrlValidator.validateBaseUrl("https://api.example.com/v1");

// validate a relative path
UrlValidator.validateRelativePath("/users/{id}");

// check for unresolved placeholders after substitution
UrlValidator.validateNoUnresolvedPlaceholders("https://api.example.com/v1/users/42");
```

---

## InvalidUrlException

**Package:** `io.github.axiomhttp.validation`  
**Extends:** `RuntimeException`

Thrown when a URL or relative path fails structural validation. Carries the invalid URL
string alongside the standard exception message so callers can inspect the exact value
that caused the failure.

### Properties

| Property | Type | Description |
|---|---|---|
| `message` | `String` | Human-readable description of why the URL is invalid |
| `invalidUrl` | `String` | The URL or path that failed validation. May be `null` if the value was blank or null itself |

### Common Causes

- A blank or null URL value in `@BaseUrl` or `@Request`
- A URL that does not start with `http://` or `https://`
- A port number outside the valid range 1–65535
- A relative path used where a full absolute URL is required
- An unresolved `{paramName}` placeholder remaining in the final URL after `@PathParam` substitution

### Catching the Exception

```java
try {
    UserService service = RequestFactory.create(UserService.class);
} catch (InvalidUrlException e) {
    System.err.println("Bad URL: " + e.getInvalidUrl());
    System.err.println("Reason:  " + e.getMessage());
}
```

### When It Is Thrown

`InvalidUrlException` is thrown at **proxy-creation time** for structural problems, and at
**request time** for unresolved placeholders. Because proxy creation typically happens at
application startup, most configuration errors surface before any requests are made.

---

## UnexpectedStatusException

**Package:** `com.habbashx.axiomhttp.exception`  
**Extends:** `RuntimeException`

Thrown when an HTTP response carries a status code that does not match the expected status
declared on the request method. Carries the actual status code, the request URL, and the
raw response body so callers have full context without needing to make a second request.

### Properties

| Property | Type | Description |
|---|---|---|
| `actualStatus` | `int` | The HTTP status code returned by the server |
| `url` | `String` | The URL that was requested when the failure occurred |
| `responseBody` | `String` | The raw response body. Never `null`, may be empty |

### Catching the Exception

```java
try {
    User user = userService.getUserById(42);
} catch (UnexpectedStatusException e) {
    System.err.println("Request to " + e.getUrl() + " failed.");
    System.err.println("Status : " + e.getActualStatus());
    System.err.println("Body   : " + e.getResponseBody());
}
```

### Relationship to @ExpectedStatus

| Scenario                                     | Behaviour |
|----------------------------------------------|---|
| No `@ExpectedStatus`, response is 2xx        | Success |
| No `@ExpectedStatus`, response is 4xx or 5xx | Throws `UnexpectedStatusException` |
| `@ExpectedStatus(201)`, response is 201      | Success |
| `@ExpectedStatus(201)`, response is 200      | Throws `UnexpectedStatusException` |
| `@ExpectedStatus(200)`, response is 204      | Success |
| `@ExpectedStatus(200)`, response is 500       | Throws `UnexpectedStatusException` |

---

## Putting It All Together

```java
@BaseUrl("https://api.example.com/v1")
public interface UserService {

    // default 2xx validation applies
    @Request(url = "/users", method = "GET")
    List<User> getUsers();

    // only 201 is accepted; 200 would throw UnexpectedStatusException
    @Request(url = "/users", method = "POST")
    @ExpectedStatus(201)
    User createUser(@Body User user);

    // either 200 or 204 is accepted
    @Request(url = "/users/{id}", method = "DELETE")
    @ExpectedStatus(200)
    void deleteUser(@PathParam("id") int id);
}

// proxy creation validates @BaseUrl and all relative paths immediately
UserService service = RequestFactory.create(UserService.class);

try {
    service.deleteUser(42);
} catch (UnexpectedStatusException e) {
    // status was not 200 or 204
} catch (InvalidUrlException e) {
    // URL was malformed — should not reach here at runtime
    // if it does, it means a placeholder was not resolved
}
```


## VERSION
1.0.3

## License

MIT License — see [LICENSE](LICENSE) for details.