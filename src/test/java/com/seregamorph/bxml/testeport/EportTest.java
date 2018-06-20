package com.seregamorph.bxml.testeport;

import com.seregamorph.bxml.Node;
import com.seregamorph.bxml.Parser;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class EportTest {

    private static String toHexString(byte[] bb, int off, int len) {
        StringBuilder sb = new StringBuilder(len * 2);

        for (int i = off; i < off + len; i++) {
            byte b = bb[i];

            int ch = b & 0xff;
            String s = Integer.toString(ch, 16);
            if (s.length() < 2) {
                assert s.length() == 1;
                sb.append('0');
            } else {
                assert s.length() == 2;
            }
            sb.append(s);
        }

        return sb.toString();
    }

    private static void addOption(Node<Void> root, String optionName, String optionValue) {
        Node<Void> optionNode = root.addChild(FETagSrc.OPTION);
        optionNode.addChild(FETagSrc.NAME, optionName);
        optionNode.addChild(FETagSrc.VALUE, optionValue);
    }

    private static Node<Void> createRoot(Date nowDate) {
        Node<Void> root = new Node<>(FETagSrc.PACKAGE);
        Node<Void> header = root.addChild(FETagSrc.HEADER);
        header.addChild(FETagSrc.TIME, nowDate.getTime());
        header.addChild(FETagSrc.VERSION, "RMA/10.0.0");
        header.addChild(FETagSrc.DIR, -1L);
        header.addChild(FETagSrc.POINT, 181612);
        header.addChild(FETagSrc.CARD, 26100000000L);
        header.addChild(FETagSrc.PIN, "qwerty123456");
        Node<Void> operation = root.addChild(FETagSrc.OPERATION);
        operation.addChild(FETagSrc.ID, "756961320");
        operation.addChild(FETagSrc.HASH, 0);
        operation.addChild(FETagSrc.TIME, nowDate.getTime());
        operation.addChild(FETagSrc.DIR, 0L);
        operation.addChild(FETagSrc.PRODUCT, 4420);
        operation.addChild(FETagSrc.ACCOUNT).addChild(FETagSrc.VALUE, "9157201011");
        operation.addChild(FETagSrc.CHECK, 11);
        operation.addChild(FETagSrc.SUM, 30.0d);
        operation.addChild(FETagSrc.TOTAL, 1000.0d);
        operation.addChild(FETagSrc.INTEREST, 0.0d);
        operation.addChild(FETagSrc.QTY, 1000.0d);
        operation.addChild(FETagSrc.PRIMARY, "qty");
        addOption(operation, "ncheck", "1242927220795");
        addOption(operation, "timeoutCheck", "20");
        addOption(operation, "confirmAction", "1");

        return root;
    }

    @Test
    public void test1() throws Exception {
        Date now = new Date(System.currentTimeMillis() / 1000 * 1000);
        Node<?> root = createRoot(now);
        System.out.println("original:\n" + root.toStringDetailed(false, true));

        byte[] bytes = root.toByteArray(true);
        Node<?> parsedNode = Parser.parseXML(FETagDst.getInstance(), bytes, true);

        System.out.println("parsed:\n" + parsedNode.toStringDetailed(false, true));

        Node<?> header = parsedNode.getChild(FETagDst.HEADER);
        assertEquals("RMA/10.0.0", header.getChildValue(FETagDst.VERSION));
        assertEquals(Long.valueOf(-1L), header.getChildValue(FETagDst.DIR));
        assertEquals(Integer.valueOf(181612), header.getChildValue(FETagDst.POINT));
        assertEquals(Long.valueOf("26100000000"), header.getChildValue(FETagDst.CARD));
        assertEquals("qwerty123456", header.findChild(0x08).getValue());
        Node<Void> operation = parsedNode.getChild(FETagDst.OPERATION);
        assertEquals(Long.valueOf(0L), operation.getChildValue(FETagDst.DIR));
    }
}
