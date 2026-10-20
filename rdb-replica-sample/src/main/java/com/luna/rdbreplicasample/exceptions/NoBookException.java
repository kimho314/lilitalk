package com.luna.rdbreplicasample.exceptions;

public class NoBookException extends RuntimeException {

    public NoBookException(Long id) {
        String message = String.format("No book found with id: %d", id);
        super(message);
    }
}
