package com.xmlanno.reflection;

public class ReflectionsException extends RuntimeException {

    public ReflectionsException(String message) {
        super(message);
    }

    public ReflectionsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionsException(Throwable cause) {
        super(cause);
    }
}
