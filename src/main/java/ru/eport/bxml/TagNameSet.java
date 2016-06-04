package ru.eport.bxml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class TagNameSet {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final Map<Integer, TagName<?>> tagNamesMap = new HashMap<Integer, TagName<?>>();

    /**
     * @param type
     * @param tagType
     * @param name
     * @param <T>
     * @return
     * @throws IllegalArgumentException
     */
    protected final <T> TagName<T> createTagName(int type, TagType<T> tagType,
                                                 String name) throws IllegalArgumentException {
        if (tagType == null) {
            throw new IllegalArgumentException("tagType is null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (tagNamesMap.containsKey(type)) {
            throw new IllegalArgumentException("Node names map already contains type: " + Utils.toHexString(type));
        }
        TagName<T> nodeName = new TagName<T>(type, tagType, name, true);
        tagNamesMap.put(type, nodeName);
        return nodeName;
    }

    final TagName<?> getTagName(int type) {
        TagName nodeName = tagNamesMap.get(type);
        if (nodeName != null) {
            return nodeName;
        }
        String name = Utils.toHexString(type);
        logger.warning("Undefined tag name type: " + name);
        return new TagName<Void>(type, TagType.VOID, name, false);
    }
}
