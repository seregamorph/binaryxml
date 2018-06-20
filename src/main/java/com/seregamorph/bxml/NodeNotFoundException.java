package com.seregamorph.bxml;

public class NodeNotFoundException extends BXmlException {
    public NodeNotFoundException(TagName nodeName) {
        super("Node " + nodeName.getName() + "(" + nodeName.getType() + ") not found");
    }

    public NodeNotFoundException(String message) {
        super(message);
    }
}
