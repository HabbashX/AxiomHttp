# ⚡ AxiomHttp (Custom Java HTTP Client Library)

A lightweight, annotation-driven Java HTTP client library built with **ByteBuddy runtime proxies**, an **interceptor pipeline**, and a **method metadata caching system**.

It allows you to define HTTP APIs using interfaces while the library handles request building, execution, serialization, and response mapping internally.

---

# 🚀 Key Features

* 🧠 Annotation-based HTTP API definitions (`@Request`, `@Path`, `@Query`, `@Headers`, `@Body`)
* ⚡ Runtime proxy generation using ByteBuddy
* 🔁 Interceptor pipeline (before / after execution hooks)
* 🧩 Method metadata caching (reflection optimized)
* 🌐 Built-in HTTP execution layer (GET / POST / PUT / DELETE)
* 📦 Request context abstraction (clean separation of concerns)
* 🔌 Pluggable JSON serialization (Jackson-ready)
* 🧠 Clean separation between parsing, execution, and transport layers
* ⚡ Async Requests Handling
---

# 🏗 Architecture Overview

```
Interface Method Call
        ↓
ByteBuddy Proxy (RequestProxyEngine)
        ↓
MethodContext creation
        ↓
MethodCache → MethodMeta lookup
        ↓
InterceptorChain (before)
        ↓
HttpExecutor
        ↓
RequestMethod (GET/POST/PUT/DELETE)
        ↓
InterceptorChain (after)
        ↓
Final Response
```

---

# 📦 Core Design Principles

## 1. Separation of Concerns

* Reflection only happens once (MethodMeta)
* Execution layer does NOT know annotations
* Interceptors modify context only

## 2. Stateless Execution Layer

* Request methods are reusable
* No runtime mutation of execution strategies

## 3. Cached Reflection

* Method metadata is cached via MethodCache
* Eliminates repeated annotation parsing

---

# 📌 Annotations

## @Request

Defines endpoint configuration:

```java
@Request(uri = "https://api.example.com/users/{id}", method = "GET")
```

---

## @Path

Replaces URI variables:

```java
@Path("id") String id
```

---

## @Query

Adds query parameters:

```java
@Query("name") String name
```

---

## @Headers

Static headers per method:

```java
@Headers({
    "Authorization: Bearer token",
    "Accept: application/json"
})
```

---

# ⚙️ Usage Examples

## 1. Simple GET Request

```java
public interface UserApi {

    @Request(uri = "https://api.example.com/users/{id}", method = "GET")
    String getUser(@Path("id") String id);
}
```

### Usage

```java
UserApi api = RequestFactory.create(UserApi.class);
String user = api.getUser("123");
```

---

## 2. Query Parameters

```java
@Request(uri = "https://api.example.com/search", method = "GET")
String search(@Query("q") String query, @Query("page") int page);
```

---

## 3. POST Request with Body

```java
@Request(uri = "https://api.example.com/users", method = "POST")
String createUser(@Body User user);
```

---

## 4. Headers Example

```java
@Request(uri = "https://api.example.com/private", method = "GET")
@Headers({"Authorization","token"})
String getPrivateData();
```

---

## 5. Mixed Parameters

```java
@Request(uri = "https://api.example.com/users/{id}/posts", method = "GET")
List<Post> getPosts(
    @Path("id") String userId,
    @Query("limit") int limit
);
```

---

# 🔁 Interceptor System

## HeaderInterceptor

Injects headers from annotations:

```java
public class HeaderInterceptor extends Interceptor {
    @Override
    public MethodContext before(MethodContext ctx) {
        return ctx;
    }

    @Override
    public Object after(Object response, MethodContext ctx) {
        return response;
    }
}
```

---

## PathQueryInterceptor

Handles:

* Path replacement
* Query string building

---

## Custom Interceptor Example

```java
public class LoggingInterceptor extends Interceptor {

    @Override
    public MethodContext before(MethodContext ctx) {
        System.out.println("→ Request: " + ctx.url);
        return ctx;
    }

    @Override
    public Object after(Object response, MethodContext ctx) {
        System.out.println("← Response received");
        return response;
    }
}
```

---

# 🧠 Method Caching

Methods are cached to avoid repeated reflection:

```java
MethodMeta meta = methodCache.get(method);
```

Contains:

* HTTP method
* URI template
* parameter structure
* annotations

---

# 🌐 Execution Flow

1. Proxy intercepts method call
2. MethodContext is created
3. MethodMeta is loaded from cache
4. Interceptors modify request
5. HttpExecutor executes request
6. Response is returned

---


# 📜 License

MIT License

---

# ⚡ Summary

AxiomHttp is a reflection-optimized, interceptor-driven HTTP client framework designed to make Java API communication declarative, fast, and extensible.
