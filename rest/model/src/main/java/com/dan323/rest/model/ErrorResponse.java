package com.dan323.rest.model;

/**
 * Body of every non-2xx response.
 */
public record ErrorResponse(String message) {
}
