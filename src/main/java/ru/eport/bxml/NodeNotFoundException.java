package ru.eport.bxml;

public class NodeNotFoundException extends BXmlException {
    public NodeNotFoundException(TagName nodeName) {
        super("Node " + nodeName.getName() + " not found");
    }

    public NodeNotFoundException(String message) {
        super(message);
    }
}
