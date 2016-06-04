package ru.eport.bxml;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class Test {
    static String toHexString(int value) {
        return "0x" + toHexString(new byte[]{(byte) value});
    }

    static String toHexString(byte[] bb, int off, int len) {
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

    static String toHexString(byte[] bb) {
        return toHexString(bb, 0, bb.length);
    }

    private static void addOption(Node<Void> root, String optionName, String optionValue) {
        Node<Void> optionNode = root.addChild(FETagSrc.OPTION);
        optionNode.addChild(FETagSrc.NAME, optionName);
        optionNode.addChild(FETagSrc.VALUE, optionValue);
    }

    private static Node<Void> createRoot(Date nowDate) {
        Node<Void> root = new Node<Void>(FETagSrc.PACKAGE);
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

    private static void test1() throws Exception {
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);

        Node<Void> root = createRoot(nowDate);

        String rootStr = root.toString();

        byte[] rootBytes = root.toByteArray(true);

        System.out.println("rootStr.length: " + rootStr.length());
        System.out.println("rootStr: " + rootStr);
        System.out.println("rootBytes.length optimized: " + rootBytes.length);
        System.out.println("effect: " + ((float) rootStr.length()) / rootBytes.length);

        int COUNT = 10000;

        for (int i = 0; i < 10; i++) {
            long startNanos = System.nanoTime();
            for (int j = 0; j < COUNT; j++) {
                Node parsedNode = Parser.parseXML(FETagSrc.getInstance(), rootBytes, true);
//				parsedNode.serialize(new DataOutputStream(new ByteArrayOutputStream(256)), true);
            }
            System.out.println("binary: " + (System.nanoTime() - startNanos) / 100000 + " ms");

            startNanos = System.nanoTime();
//            for (int j = 0; j < COUNT; j++) {
//                XMLParser.parseXML(rootStr, true);
//            }
            System.out.println("text: " + (System.nanoTime() - startNanos) / 100000 + " ms");
        }
    }

    private static void test2() throws Exception {
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);

        Node<Void> root = createRoot(nowDate);

        String rootStr = root.toString();

        System.out.println(rootStr);
        System.out.println("str=" + rootStr.length());

        byte[] bb = root.toByteArray(true);

        try (OutputStream out = new FileOutputStream("bb.dat")) {
            out.write(bb);
        }

        System.out.println("rootBytes.length optimized: " + root.toByteArray(true).length);
        System.out.println("rootBytes.length not optimized: " + root.toByteArray(false).length);

        System.out.println("effect: " + (((double) rootStr.length()) / bb.length));

        System.out.println("data=[" + bb.length + "]" + toHexString(bb));

        Node<?> rootParsed = Parser.parseXML(FETagDst.getInstance(), bb, true);
        System.out.println("parsed=" + rootParsed.toStringDetailed(true, true));
    }

    private static void test3() throws Exception {
        FERequest request = new FERequest();

        String rmaKey = Long.toString(System.currentTimeMillis());
        request.setPointKey(rmaKey);

        // реквизиты
        request.setProduct(4165);
        request.setAccount("9035964850");

        request.setTwoPhase(true);

        // суммы
        double primeValue = 2.0d;
//		InputCurrency primeCurr = InputCurrency.RUR;

        request.setTotal(primeValue);
        request.setInterest(0.0d);
        request.setQty(primeValue);
        request.setEye(primeValue / 30);

        request.setPrimaryCurr("RUR");

        Node root = request.createNode();
        System.out.println(root.toStringDetailed(false, true));

        Node<?> parsedNode = Parser.parseXML(FETagDst.getInstance(), root.toByteArray(true), true);
        FERequest parsedRequest = FERequest.parseNode(parsedNode);
    }

    public static void main(String[] args) throws Exception {
        test3();
    }
}
