package com.chelly.backend.models.exceptions;

public class DuplicateElementException extends RuntimeException {
    public DuplicateElementException(String message) {
        super(message);
    }
}
