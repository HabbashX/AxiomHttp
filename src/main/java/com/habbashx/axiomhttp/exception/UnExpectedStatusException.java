package com.habbashx.axiomhttp.exception;

/**
 * Thrown when an HTTP response carries a status code that does not match
 * the expected status declared on the request method.
 *
 * <p>AxiomHttp validates the response status code after every request. By default,
 * any status outside the 2xx range is considered a failure. This behaviour can be
 * overridden per-method using
 * {@link com.habbashx.axiomhttp.annotation.ExpectedStatus @ExpectedStatus}.
 *
 * <p>This exception carries the actual status code received, the URL that was
 * requested, and the raw response body, giving callers enough context to handle
 * or log the failure without making a second request.
 *
 * <p><b>Example — catching and inspecting the failure:</b>
 * <pre>{@code
 * try {
 *     User user = userService.getUserById(42);
 * } catch (UnexpectedStatusException e) {
 *     System.err.println("Request to " + e.getUrl() + " failed.");
 *     System.err.println("Status : " + e.getActualStatus());
 *     System.err.println("Body   : " + e.getResponseBody());
 * }
 * }</pre>
 *
 * @see com.habbashx.axiomhttp.annotation.ExpectedStatus
 */
public class UnExpectedStatusException extends RuntimeException {

    public UnExpectedStatusException(String message) {
        super(message);
    }
}
