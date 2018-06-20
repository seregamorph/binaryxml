package com.seregamorph.bxml;

public class BXmlException extends RuntimeException {
    public BXmlException(String message) {
        super(message);
    }

    public BXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
