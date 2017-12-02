package org.flylib.boot.starter.exception;

public class CustomRuntimeException extends RuntimeException {
    private final String code;

    public CustomRuntimeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public CustomRuntimeException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
