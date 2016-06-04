package ru.eport.bxml;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final int MAX_LEVEL = 16;

    private static Node parse(TagNameSet tagNameSet, DataInputStream dis, TagName tagName, int level, boolean allowConvert)
            throws IOException, SerializeException {
        if (level > MAX_LEVEL) {
            throw new SerializeException("Too deep structure. Max level is " + MAX_LEVEL);
        }

        if (tagName == null) {
            // ожидаем имя тега
            int type = Utils.readUByte(dis);
            if (!TagName.isTagName(type)) {
                throw new IOException("Expecting node name. Unexpected byte: " + type);
            }
            tagName = tagNameSet.getTagName(type);
        }

        Object nodeValue = null;
        List<Node<?>> childNodes = null;
        boolean valueDefined = false;

        while (true) {
            int type = Utils.readUByte(dis);
            if (TagName.isCloseTag(type)) {
                break;
            }

            if (TagType.isTagType(type)) {
                // не может быть более одного value
                if (valueDefined) {
                    throw new SerializeException("node value already defined");
                }
                // значение тега
                TagType<?> tagType = TagType.getTagType(type);
                nodeValue = tagType.readValue(dis);
                if (nodeValue == null) {
                    throw new RuntimeException("node value is null");
                }
                valueDefined = true;

                if (tagName.isPredefined()) {
                    // tagType должен совпадать
                    // если не совпал, пытаемся сконвертить
                    if (tagName.getTagType() != tagType) {
                        if (allowConvert) {
                            nodeValue = tagName.getTagType().convert(tagType, nodeValue);
                        } else {
                            throw new SerializeException(String.format("Cannot convert from type %s to %s (forbidden)",
                                    tagType.getName(), tagName.getTagType().getName()));
                        }
                    }
                } else {
                    tagName.setTagType(tagType);
                }

                continue;
            }

            if (TagName.isTagName(type)) {
                // вложенный тег
                TagName childNodeName = tagNameSet.getTagName(type);
                Node childNode = parse(tagNameSet, dis, childNodeName, level + 1, allowConvert);

                if (childNodes == null) {
                    childNodes = new ArrayList<Node<?>>(4);
                }

                childNodes.add(childNode);
                continue;
            }

            throw new SerializeException("Unexpected byte: " + Utils.toHexString(type));
        }

        Node<?> node = new Node(tagName, nodeValue, false);
        if (childNodes != null) {
            for (Node<?> child : childNodes) {
                node.addChild(child);
            }
        }
        return node;
    }

    public static Node<?> parseXML(TagNameSet tagNameSet, DataInputStream dis, boolean allowConvert)
            throws IOException, SerializeException {
        return parse(tagNameSet, dis, null, 0, allowConvert);
    }

    public static Node<?> parseXML(TagNameSet tagNameSet, byte[] bytes, int off, int len, boolean allowConvert)
            throws IOException, SerializeException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes, off, len);
        DataInputStream dis = new DataInputStream(bais);
        return parse(tagNameSet, dis, null, 0, allowConvert);
    }

    public static Node<?> parseXML(TagNameSet tagNameSet, byte[] bytes, boolean allowConvert)
            throws IOException, SerializeException {
        return parseXML(tagNameSet, bytes, 0, bytes.length, allowConvert);
    }
}
