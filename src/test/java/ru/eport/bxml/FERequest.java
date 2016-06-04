package ru.eport.bxml;

import ru.eport.util.Iter;

/**
 * Детали запроса XAgent/XCheckOnly для отправки на сервер фронтов
 */
public class FERequest {
    private long pointTime;
    private String pointKey;
    private int reqHash;
    private int product;
    private String account;
    private int timeoutCheck = 50;
    private double total;
    private double interest = 0.0d;
    private double eye;
    private double qty;
    private String primaryCurr;
    private boolean twoPhase = true;
    private boolean sumDeclared;

    public FERequest() {
    }

    public long getPointTime() {
        if (pointTime == 0L) {
            pointTime = System.currentTimeMillis();
        }
        return pointTime;
    }

    public void setPointTime(long pointTime) {
        this.pointTime = pointTime;
    }

    public String getPointKey() {
        if (pointKey == null) {
            throw new RuntimeException("pointKey is not set");
        }
        return pointKey;
    }

    public void setPointKey(String pointKey) {
        this.pointKey = pointKey;
    }

    public int getReqHash() {
        return reqHash;
    }

    public void setReqHash(int reqHash) {
        this.reqHash = reqHash;
    }

    public int getProduct() {
        if (product == 0) {
            throw new RuntimeException("product is not set");
        }
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public String getAccount() {
        if (account == null) {
            throw new RuntimeException("account is not set");
        }
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getTimeoutCheck() {
        return timeoutCheck;
    }

    public void setTimeoutCheck(int timeoutCheck) {
        this.timeoutCheck = timeoutCheck;
    }

    public double getTotal() {
        if (total == 0.0d) {
            throw new RuntimeException("total is not set");
        }
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public double getEye() {
        if (eye == 0.0d) {
            throw new RuntimeException("eye is not set");
        }
        return eye;
    }

    public void setEye(double eye) {
        this.eye = eye;
    }

    public double getQty() {
        if (qty == 0.0d) {
            throw new RuntimeException("qty is not set");
        }
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public String getPrimaryCurr() {
        if (primaryCurr == null) {
            throw new RuntimeException("primaryCurr is not set");
        }
        return primaryCurr;
    }

    public void setPrimaryCurr(String primaryCurr) {
        this.primaryCurr = primaryCurr;
    }

    public boolean isTwoPhase() {
        return twoPhase;
    }

    public void setTwoPhase(boolean twoPhase) {
        this.twoPhase = twoPhase;
    }

    public boolean isSumDeclared() {
        return sumDeclared;
    }

    public void setSumDeclared(boolean sumDeclared) {
        this.sumDeclared = sumDeclared;
    }

    private static void addOption(Node<Void> root, String optionName, String optionValue) {
        Node<Void> optionNode = root.addChild(FETagSrc.OPTION);
        optionNode.addChild(FETagSrc.NAME, optionName);
        optionNode.addChild(FETagSrc.VALUE, optionValue);
    }

    public Node<Void> createNode() {
        Node<Void> root = new Node<Void>(FETagSrc.PACKAGE);
        Node<Void> header = root.addChild(FETagSrc.HEADER);
        header.addChild(FETagSrc.TIME, getPointTime());
        header.addChild(FETagSrc.VERSION, "RMA/10.0.0");
        header.addChild(FETagSrc.DIR, -1L);
        header.addChild(FETagSrc.POINT, 181612);
        header.addChild(FETagSrc.CARD, 26100000000L);
        header.addChild(FETagSrc.PIN, "qwerty123456");
        Node<Void> operation = root.addChild(FETagSrc.OPERATION);
        operation.addChild(FETagSrc.ID, getPointKey());
        operation.addChild(FETagSrc.HASH, getReqHash());
        operation.addChild(FETagSrc.TIME, getPointTime());
        operation.addChild(FETagSrc.DIR, 0L);
        operation.addChild(FETagSrc.PRODUCT, getProduct());
        operation.addChild(FETagSrc.ACCOUNT).addChild(FETagSrc.VALUE, getAccount());
        operation.addChild(FETagSrc.CHECK, isTwoPhase() ? 1 : 0);
        operation.addChild(FETagSrc.SUM, getEye());
        operation.addChild(FETagSrc.TOTAL, getTotal());
        operation.addChild(FETagSrc.INTEREST, getInterest());
        operation.addChild(FETagSrc.QTY, getQty());
        operation.addChild(FETagSrc.PRIMARY, "qty");
        addOption(operation, "ncheck", "1242927220795");
        addOption(operation, "timeoutCheck", "20");
        addOption(operation, "confirmAction", "1");

        return root;
    }

    private void parsePackage(Node<Void> root) throws Exception {
        Node<Void> header = root.getChild(FETagDst.HEADER);
        setPointTime(header.getChildValue(FETagDst.TIME).getTime());
        assert "RMA/10.0.0".equals(header.getChildValue(FETagDst.VERSION));
        assert -1L == header.getChildValue(FETagDst.DIR);
        assert 181612 == header.getChildValue(FETagDst.POINT);
        assert 26100000000L == header.getChildValue(FETagDst.CARD);
        assert "qwerty123456".equals(header.getChildValue(FETagDst.PIN));

        Node<Void> operation = root.getChild(FETagDst.OPERATION);
        setPointKey(operation.getChildValue(FETagDst.ID));

        int reqHash = operation.getChildValue(FETagDst.HASH);
        setReqHash(reqHash);

        setPointTime(operation.getChildValue(FETagDst.TIME).getTime());
        assert 0L == operation.getChildValue(FETagDst.DIR);
        setProduct(operation.getChildValue(FETagDst.PRODUCT));
        setAccount(operation.getChild(FETagDst.ACCOUNT).getChildValue(FETagDst.VALUE));
        setTwoPhase(operation.getChildValue(FETagDst.CHECK) == 1);
        setEye(operation.getChildValue(FETagDst.SUM));
        setTotal(operation.getChildValue(FETagDst.TOTAL));
        setInterest(operation.getChildValue(FETagDst.INTEREST));
        setQty(operation.getChildValue(FETagDst.QTY));
        setPrimaryCurr(operation.getChildValue(FETagDst.PRIMARY));

        Iter<Node<Void>> optionIter = operation.iterChild(FETagDst.OPTION);
        Node<Void> optionNode;
        while ((optionNode = optionIter.next()) != null) {
            String optionName = optionNode.getChildValue(FETagDst.NAME);
            String optionValue = optionNode.getChildValue(FETagDst.VALUE);
            System.out.println("name: " + optionName + " value:" + optionValue);
        }
//		addOption(operation, "ncheck", "1242927220795");
//		addOption(operation, "timeoutCheck", "20");
//		addOption(operation, "confirmAction", "1");
    }

    public static FERequest parseNode(Node root) throws Exception {
        if (root.getName() != FETagDst.PACKAGE) {
            throw new RuntimeException();
        }

        FERequest req = new FERequest();
        req.parsePackage(FETagDst.PACKAGE.castNode(root));
        return req;
    }
}
