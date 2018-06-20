package com.seregamorph.bxml.testway4;

import com.seregamorph.bxml.Node;
import com.seregamorph.bxml.Parser;
import org.junit.Test;

import java.util.Date;

import static com.seregamorph.bxml.testway4.Way4Tag.*;
import static org.junit.Assert.assertEquals;

/**
 * Тест сериализации сообщения протокола OpenWay (way4)
 * (по аналогичному содержанию)
 */
public class Way4Test {

    @Test
    public void testWay4Message110() {
        Date now = new Date(System.currentTimeMillis() / 1000 * 1000);

        Node<Void> message = Node.node(MESSAGE)
                .set(PAN, Long.parseLong("5213000000000001"))
                .set(PROCESSING_CODE, 0)
                .set(AMOUNT, 100)
                .set(AMOUNT_BILLING, 100)
                .set(DATETIME_TRANSMISSION, now)
                .set(CONVERSION_CARDHOLDER, 61000000)
                .set(STAN, 218L)
                .set(COUNTRY_CODE_ACQUIRING, 643)
                .set(ACQUIRING_INSTITUTION_ID, 33200)
                .set(REFERENCE_NUMBER, 514989000084L)
                .set(RESPONSE_CODE, 57)
                .set(TERMINAL_ID, 50000003)
                .set(CARD_ACQUIRER_ID, 50000003)
                .set(Node.node(PROPRIETARY)
                        .set(TAG_915, "N")
                        .set(TAG_925, "ZZZ1N4Z010000")
                )
                .set(CURRENCY, 810)
                .set(CURRENCY_CARDHOLDER_BILLING, 810)
                .set(ACCOUNT_IDENTIFICATION_1, Long.parseLong("0124444421643"));

        // такое же по содержанию сообщение в протоколе iso8583 занимает 146 байт

        byte[] bytesNonOptimized = message.serialize(false);
        // bxml получается всего 156 байт (без оптимизациеи типов)
        System.out.println(bytesNonOptimized.length);

        // bxml получается всего 132 байт (с оптимизацией типов)
        byte[] bytesOptimized = message.serialize(true);
        System.out.println(bytesOptimized.length);

        Node<?> parsed = Parser.parseXML(Way4Tag.getInstance(), bytesOptimized, true);
        System.out.println(parsed);

        assertEquals(Long.valueOf("5213000000000001"), parsed.getChildValue(PAN));
        assertEquals(Integer.valueOf(0), parsed.getChildValue(PROCESSING_CODE));
        assertEquals(Integer.valueOf(100), parsed.getChildValue(AMOUNT));
        assertEquals(Integer.valueOf(100), parsed.getChildValue(AMOUNT_BILLING));
        assertEquals(now, parsed.getChildValue(DATETIME_TRANSMISSION));
        assertEquals(Integer.valueOf(61000000), parsed.getChildValue(CONVERSION_CARDHOLDER));
        assertEquals(Long.valueOf(218L), parsed.getChildValue(STAN));
        assertEquals(Integer.valueOf(643), parsed.getChildValue(COUNTRY_CODE_ACQUIRING));
        assertEquals(Integer.valueOf(33200), parsed.getChildValue(ACQUIRING_INSTITUTION_ID));
        assertEquals(Long.valueOf("514989000084"), parsed.getChildValue(REFERENCE_NUMBER));
        assertEquals(Integer.valueOf(57), parsed.getChildValue(RESPONSE_CODE));
        assertEquals(Integer.valueOf(50000003), parsed.getChildValue(TERMINAL_ID));
        assertEquals(Integer.valueOf(50000003), parsed.getChildValue(CARD_ACQUIRER_ID));

        Node<Void> tagset47 = parsed.getChild(PROPRIETARY);
        assertEquals("N", tagset47.getChildValue(TAG_915));
        assertEquals("ZZZ1N4Z010000", tagset47.getChildValue(TAG_925));
        //assertEquals(Sets.newHashSet(915, 925), tagset47.tagIds());

        assertEquals(810, parsed.getChildValue(CURRENCY).intValue());
        assertEquals(810, parsed.getChildValue(CURRENCY_CARDHOLDER_BILLING).intValue());
        assertEquals(Long.valueOf("0124444421643"), parsed.getChildValue(ACCOUNT_IDENTIFICATION_1));

        //assertEquals(Sets.newHashSet(2, 3, 4, 6, 7, 10, 11, 19, 32, 37, 39, 41, 42, 47, 49, 51, 102), parsed.getFields().keySet());
    }
}
