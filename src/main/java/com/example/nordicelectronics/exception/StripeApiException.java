package com.example.nordicelectronics.exception;

/**
 * Custom exception for Stripe API errors
 */
public class StripeApiException extends Exception {

    private final int statusCode;
    private final String responseBody;

    public StripeApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public StripeApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public StripeApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public StripeApiException(String message, Throwable cause, int statusCode, String responseBody) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
