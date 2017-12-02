package org.flylib.boot.starter.exception;

public class UnknownResourceException extends RuntimeException {
    public UnknownResourceException(String msg) {
        super(msg);
    }
}
