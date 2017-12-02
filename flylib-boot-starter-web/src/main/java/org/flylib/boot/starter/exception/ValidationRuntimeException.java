package org.flylib.boot.starter.exception;

public class ValidationRuntimeException extends CustomRuntimeException {
    public ValidationRuntimeException(String code, String message) {
        super(code, message);
    }
}
