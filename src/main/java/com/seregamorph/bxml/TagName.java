package com.seregamorph.bxml;

public final class TagName<T> {
    private static final int TYPE_TAG_NAME_MIN = 0x01;
    private static final int TYPE_TAG_NAME_MAX = 0xBF;

    static final int CLOSE_TAG = 0x00;

    private final int type;
    /**
     * Может быть неизвестен сразу, если неизвестный tagname
     */
    private TagType<T> tagType;
    private final String name;
    private final boolean isPredefined;

    TagName(int type, TagType<T> tagType, String name, boolean predefined) {
        if (type < TYPE_TAG_NAME_MIN || type > TYPE_TAG_NAME_MAX) {
            throw new RuntimeException("Illegal tag type: " + Utils.toHexString(type));
        }
        this.type = type;
        this.tagType = tagType;
        this.name = name;

        this.isPredefined = predefined;
    }

    int getType() {
        return type;
    }

    TagType<T> getTagType() {
        return tagType;
    }

    @SuppressWarnings({"unchecked"})
    void setTagType(TagType tagType) {
        if (isPredefined) {
            throw new IllegalStateException("cannot set tagType for predefined node");
        }
        this.tagType = tagType;
    }

    boolean isPredefined() {
        return isPredefined;
    }

    String getName() {
        return name;
    }

    static boolean isTagName(int type) {
        return TYPE_TAG_NAME_MIN <= type && type <= TYPE_TAG_NAME_MAX;
    }

    static boolean isCloseTag(int type) {
        return CLOSE_TAG == type;
    }
}
