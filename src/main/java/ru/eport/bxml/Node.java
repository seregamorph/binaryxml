package ru.eport.bxml;

import ru.eport.util.Iter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Node<T> {
    private final TagName<T> name;
    private final T value;

    // todo подумать о структуре с хеш-таблицей
    private Node firstChild;
    private Node lastChild;
    private Node nextNode;

    Node(TagName<T> name, T value, boolean checkValue) {
        this.name = name;
        this.value = value;

        // null не проверяем
        // если это распарсенное значение, проверку делать не нужно
        if (value == null || !checkValue) {
            return;
        }
        TagType<T> tagType = name.getTagType();
        if (tagType != null) {
            if (!tagType.checkValueConstraint(value)) {
                throw new IllegalArgumentException("Illegal node value: " + tagType.toString(value));
            }
        }
    }

    public Node(TagName<T> name) {
        this(name, null, true);
    }

    public Node(TagName<T> name, T value) {
        this(name, value, true);
    }

    public void addChild(Node child) {
        if (firstChild == null) {
            firstChild = child;
            lastChild = child;
        } else {
            assert lastChild != null;
            lastChild.nextNode = child;
            lastChild = child;
        }
    }

    public <K> Node<K> addChild(TagName<K> name, K value) {
        Node<K> child = new Node<K>(name, value);
        addChild(child);
        return child;
    }

    public <K> Node<K> addChild(TagName<K> name) {
        return addChild(name, null);
    }

    /**
     * Имя тега
     *
     * @return
     */
    public TagName<T> getName() {
        return name;
    }

    /**
     * Значение тега
     *
     * @return
     */
    public T getValue() {
        return value;
    }

    @SuppressWarnings({"unchecked"})
    public <K> Node<K> findChild(TagName<K> name) {
        Node node = firstChild;
        while (node != null) {
            if (node.name == name) {
                return node;
            }
            node = node.nextNode;
        }
        return null;
    }

    public <K> Node<K> getChild(TagName<K> name) throws NodeNotFoundException {
        Node<K> node = findChild(name);
        if (node == null) {
            throw new NodeNotFoundException(name);
        }
        return node;
    }

    public <K> K getChildValue(TagName<K> name) throws NodeNotFoundException {
        Node<K> node = findChild(name);
        if (node == null) {
            throw new NodeNotFoundException(name);
        }
        return node.getValue();
    }

    @SuppressWarnings({"unchecked"})
    private String toStringImpl(int level, boolean full, boolean printTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("\t");
        }
        sb.append("<").append(name.getName());
        if (printTypes || !name.isPredefined()) {
            sb.append(":");
            TagType<T> tagType = name.getTagType();
            if (tagType != null) {
                sb.append(tagType.getName());
            } else {
                sb.append("null");
            }
        }
        sb.append(">");
        sb.append(name.getTagType().toString(value, full));

        Node child = firstChild;
        boolean emptyChild = child == null;
        while (child != null) {
            sb.append("\n");
            sb.append(child.toStringImpl(level + 1, full, printTypes));
            child = child.nextNode;
        }

        if (!emptyChild) {
            for (int i = 0; i < level; i++) {
                sb.append("\t");
            }
        }

        sb.append("</").append(name.getName()).append(">");
        if (nextNode == null) {
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }
        Node node = (Node) o;
        if (node.getName() != this.getName()) {
            return false;
        }
        if (this.value == null) {
            return node.getValue() == null;
        } else {
            return this.value.equals(node.getValue());
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return toStringImpl(0, false, false);
    }

    public String toStringDetailed(boolean full, boolean printTypes) {
        return toStringImpl(0, full, printTypes);
    }

    @SuppressWarnings({"unchecked"})
    public void serialize(DataOutputStream dos, boolean allowOptimize) throws IOException, SerializeException {
        // node name
        dos.write(name.getType());

        // node value
        if (value != null) {
            TagType tagType = name.getTagType();
            if (tagType == null) {
                throw new RuntimeException("undefined serializer for node " + name.getName());
            }
            tagType.write(dos, value, allowOptimize);
        }

        // child nodes
        Node child = firstChild;
        while (child != null) {
            child.serialize(dos, allowOptimize);
            child = child.nextNode;
        }

        // close tag
        dos.write(TagName.CLOSE_TAG);
    }

    public byte[] toByteArray(boolean allowOptimize) throws SerializeException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        DataOutputStream dos = new DataOutputStream(baos);
        serialize(dos, allowOptimize);
        return baos.toByteArray();
    }

    /**
     * Итератор по всем вложенным тегам<br/>
     *
     * @return итератор по вложенным тегам
     */
    public Iter<Node> iterChild() {
        return new ChildIter();
    }

    /**
     * Тег для итератора
     */
    private static final Node ITERATOR_INITIAL_NODE = new Node<Void>(null, null, false);

    private class ChildIter implements Iter<Node> {
        private Node current = ITERATOR_INITIAL_NODE;

        public Node next() {
            if (current == ITERATOR_INITIAL_NODE) {
                return current = firstChild;
            } else if (null == current) {
                return null;
            } else {
                return current = current.nextNode;
            }
        }
    }

    /**
     * Итератор по вложенным тегам с фильтром типа тега
     *
     * @param tagName
     * @return указатель на итератор
     */
    public <K> Iter<Node<K>> iterChild(TagName<K> tagName) {
        return new ChildIterByName<K>(tagName);
    }

    /**
     * Итератор по вложенным тегам нужного названия
     */
    private class ChildIterByName<K> implements Iter<Node<K>> {
        private Node current = ITERATOR_INITIAL_NODE;
        private final TagName<K> childTagName;

        private ChildIterByName(TagName<K> childTagName) {
            if (childTagName == null) {
                throw new IllegalArgumentException("childTagName is null");
            }
            this.childTagName = childTagName;
        }

        @SuppressWarnings({"unchecked"})
        public Node<K> next() {
            if (current == ITERATOR_INITIAL_NODE) {
                current = firstChild;
            } else if (null == current) {
                return null;
            } else {
                current = current.nextNode;
            }
            while (current != null) {
                if (childTagName == current.name) {
                    return current;
                }
                current = current.nextNode;
            }
            // больше нет
            return null;
        }
    }
}
