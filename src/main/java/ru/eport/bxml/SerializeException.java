package ru.eport.bxml;

public class SerializeException extends BXmlException {
    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
