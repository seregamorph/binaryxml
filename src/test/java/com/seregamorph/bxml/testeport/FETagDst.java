package com.seregamorph.bxml.testeport;

import com.seregamorph.bxml.TagName;
import com.seregamorph.bxml.TagNameSet;
import com.seregamorph.bxml.TagType;

import java.util.Date;

public final class FETagDst extends TagNameSet {
    private static final FETagDst instance = new FETagDst();

    private FETagDst() {
    }

    public static FETagDst getInstance() {
        return instance;
    }

    private static <T> TagName<T> tagName(int type, TagType<T> tagType, String name) {
        return instance.createTagName(type, tagType, name);
    }

    public static final TagName<Void> PACKAGE = tagName(0x01, TagType.VOID, "package");
    public static final TagName<Void> HEADER = tagName(0x02, TagType.VOID, "header");
    // имеет тип LONG в src
    public static final TagName<Date> TIME = tagName(0x03, TagType.UNIX_TIME, "time");
    public static final TagName<String> VERSION = tagName(0x04, TagType.UTF8, "version");
    public static final TagName<Long> DIR = tagName(0x05, TagType.LONG, "dir");
    public static final TagName<Integer> POINT = tagName(0x06, TagType.INT, "point");
    public static final TagName<Long> CARD = tagName(0x07, TagType.LONG, "card");
    // отсутствует в dst
    //public static final TagName<String> PIN = tagName(0x08, TagType.UTF8, "pin");
    public static final TagName<Void> OPERATION = tagName(0x09, TagType.VOID, "operation");
    public static final TagName<String> ID = tagName(0x0A, TagType.UTF8, "id");
    public static final TagName<Integer> HASH = tagName(0x0B, TagType.INT, "hash");
    public static final TagName<Integer> PRODUCT = tagName(0x0C, TagType.INT, "product");
    public static final TagName<Void> ACCOUNT = tagName(0x0D, TagType.VOID, "account");
    public static final TagName<String> VALUE = tagName(0x0E, TagType.UTF8, "value");
    public static final TagName<Integer> CHECK = tagName(0x0F, TagType.UINT8, "check");
    public static final TagName<Double> SUM = tagName(0x10, TagType.DOUBLE, "sum"); // todo
    public static final TagName<Double> TOTAL = tagName(0x11, TagType.DOUBLE, "total"); // todo
    public static final TagName<Double> INTEREST = tagName(0x12, TagType.DOUBLE, "interest"); // todo
    public static final TagName<Double> QTY = tagName(0x13, TagType.DOUBLE, "qty"); // todo
    public static final TagName<String> PRIMARY = tagName(0x14, TagType.UTF8, "primary");
    public static final TagName<Void> OPTION = tagName(0x15, TagType.VOID, "option");
    public static final TagName<String> NAME = tagName(0x16, TagType.UTF8, "name");
}
