package com.seregamorph.bxml.testway4;

import com.seregamorph.bxml.TagName;
import com.seregamorph.bxml.TagNameSet;
import com.seregamorph.bxml.TagType;

import java.util.Date;

public class Way4Tag extends TagNameSet {
    private static final Way4Tag instance = new Way4Tag();

    public static final TagName<Void> MESSAGE = tagName(1, TagType.VOID, "message");

    public static final TagName<Long> PAN = tagName(2, TagType.LONG, "PAN");
    public static final TagName<Integer> PROCESSING_CODE = tagName(3, TagType.INT, "PROCESSING_CODE");
    public static final TagName<Integer> AMOUNT = tagName(4, TagType.INT, "AMOUNT");
    public static final TagName<Integer> AMOUNT_BILLING = tagName(6, TagType.INT, "AMOUNT_BILLING");
    public static final TagName<Date> DATETIME_TRANSMISSION = tagName(7, TagType.UNIX_TIME, "DATETIME_TRANSMISSION");
    public static final TagName<Integer> CONVERSION_CARDHOLDER = tagName(10, TagType.INT, "CONVERSION_CARDHOLDER");
    public static final TagName<Long> STAN = tagName(11, TagType.LONG, "STAN");
    //public static final TagName<> TIME_LOCAL_TRANSACTION = tagName(12, TagType., "TIME_LOCAL_TRANSACTION");
//    public static final TagName<Date> DATETIME_LOCAL_TRANSACTION = tagName(12, TagType.UNIX_TIME, "DATETIME_LOCAL_TRANSACTION");
//    public static final TagName<> EXPIRATION_DATE = tagName(14, TagType., "EXPIRATION_DATE");
//    public static final TagName<> MERCHANT_TYPE = tagName(18, TagType., "MERCHANT_TYPE");
    public static final TagName<Integer> COUNTRY_CODE_ACQUIRING = tagName(19, TagType.INT, "COUNTRY_CODE_ACQUIRING");
    //    public static final TagName<> PAN_EXTENDED = tagName(20, TagType., "PAN_EXTENDED");
//    public static final TagName<> POS_DATE_CODE = tagName(22, TagType., "POS_DATE_CODE");
//    public static final TagName<> POS_ENTRY_MODE = tagName(22, TagType., "POS_ENTRY_MODE");
//    public static final TagName<> CARD_SEQ_NUMBER = tagName(23, TagType., "CARD_SEQ_NUMBER");
//    public static final TagName<> POS_CONDITION_CODE = tagName(25, TagType., "POS_CONDITION_CODE");
    public static final TagName<Integer> ACQUIRING_INSTITUTION_ID = tagName(32, TagType.INT, "ACQUIRING_INSTITUTION_ID");
    public static final TagName<Long> REFERENCE_NUMBER = tagName(37, TagType.LONG, "REFERENCE_NUMBER");
    //    public static final TagName<> APPROVAL_CODE = tagName(38, TagType., "APPROVAL_CODE");
    public static final TagName<Integer> RESPONSE_CODE = tagName(39, TagType.INT, "RESPONSE_CODE");
    public static final TagName<Integer> TERMINAL_ID = tagName(41, TagType.INT, "TERMINAL_ID");
    public static final TagName<Integer> CARD_ACQUIRER_ID = tagName(42, TagType.INT, "CARD_ACQUIRER_ID");
    public static final TagName<Void> PROPRIETARY = tagName(47, TagType.VOID, "PROPRIETARY");
    //    public static final TagName<> ADDITIONAL_PRIVATE_DATA = tagName(48, TagType., "ADDITIONAL_PRIVATE_DATA");
    public static final TagName<Integer> CURRENCY = tagName(49, TagType.INT, "CURRENCY");
    public static final TagName<Integer> CURRENCY_CARDHOLDER_BILLING = tagName(51, TagType.INT, "CURRENCY_CARDHOLDER_BILLING");
    //    public static final TagName<> NETWORK_MANAGEMENT_CODE = tagName(70, TagType., "NETWORK_MANAGEMENT_CODE");
    public static final TagName<Long> ACCOUNT_IDENTIFICATION_1 = tagName(102, TagType.LONG, "ACCOUNT_IDENTIFICATION_1");

    public static final TagName<String> TAG_915 = tagName(115, TagType.UTF8, "TAG_915");
    public static final TagName<String> TAG_925 = tagName(125, TagType.UTF8, "TAG_925");

    private Way4Tag() {
    }

    public static Way4Tag getInstance() {
        return instance;
    }

    private static <T> TagName<T> tagName(int type, TagType<T> tagType, String name) {
        return instance.createTagName(type, tagType, name);
    }
}
