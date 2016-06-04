package ru.eport.bxml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class TagType<T> {
    private static final int TAG_TYPE_MIN = 0xC0;
    private static final int TAG_TYPE_MAX = 0xFF;

    private static final int VOID_TYPE = -1;

    private final int type;
    private final String name;

    private static final Map<Integer, TagType<?>> TAG_TYPE_MAP = new HashMap<Integer, TagType<?>>();

    private TagType(int type, String name) {
        this.type = type;
        this.name = name;

        if (type == VOID_TYPE) {
            // VOID is an exception
            return;
        }
        if (type < TAG_TYPE_MIN || type > TAG_TYPE_MAX) {
            throw new RuntimeException("Illegal type: " + Utils.toHexString(type));
        }
        if (TAG_TYPE_MAP.containsKey(type)) {
            throw new RuntimeException("Duplicate type key: " + Utils.toHexString(type));
        }
        TAG_TYPE_MAP.put(type, this);
    }

    static TagType<?> getTagType(int type) throws SerializeException {
        TagType<?> tagType = TAG_TYPE_MAP.get(type);
        if (tagType == null) {
            throw new SerializeException("Tag type " + Utils.toHexString(type) + " not found");
        }
        return tagType;
    }

    final int getType() {
        return type;
    }

    final String getName() {
        return name;
    }

    /**
     * Прочитать значение тега из потока ввода
     *
     * @param dis
     * @return значение, не может быть null
     * @throws IOException
     * @throws SerializeException
     */
    abstract T readValue(DataInputStream dis) throws IOException, SerializeException;

    /**
     * @param dos
     * @param value значение для записи, не null
     * @throws IOException
     * @throws SerializeException
     */
    abstract void writeValue(DataOutputStream dos, T value) throws IOException, SerializeException;

    /**
     * @param dos
     * @param value значение для записи, не null
     * @throws IOException
     * @throws SerializeException
     */
    void write(DataOutputStream dos, T value, boolean allowOptimize) throws IOException, SerializeException {
        dos.write(getType());
        writeValue(dos, value);
    }

    /**
     * @param value - не null
     * @return true, если значение прошло проверку, иначе false
     */
    boolean checkValueConstraint(T value) {
        return true;
    }

    /**
     * Пытаемся сконвертировать значение, полученное от другого сериализатора
     *
     * @param fromTagType
     * @param value
     * @return сконвертированное значение
     * @throws SerializeException если конвертация невозможна
     */
    @SuppressWarnings({"unchecked"})
    T convert(TagType fromTagType, Object value) throws SerializeException {
        assert this != fromTagType;

        throw new SerializeException(String.format("Cannot convert from %s to %s, value=%s (not supported)",
                fromTagType.getName(), this.getName(), fromTagType.toString(value)));
    }

    /**
     * Привести не-null значение к строке
     *
     * @param value
     * @return
     */
    String valueToString(T value) {
        return value.toString();
    }

    /**
     * Привести не-null значение к строке
     *
     * @param value
     * @return
     */
    String valueToStringFull(T value) {
        return valueToString(value);
    }

    /**
     * Приводит значение к строке
     *
     * @param value
     * @return
     */
    final String toString(T value, boolean full) {
        if (value == null) {
            return "";
        }
        return full ? valueToStringFull(value) : valueToString(value);
    }

    /**
     * Приводит значение к строке
     *
     * @param value
     * @return
     */
    final String toString(T value) {
        return toString(value, false);
    }

    @SuppressWarnings({"unchecked"})
    final T castValue(Object value) {
        return (T) value;
    }

    static boolean isTagType(int type) {
        return TAG_TYPE_MIN <= type && type <= TAG_TYPE_MAX;
    }

    public static final TagType<Void> VOID = new TagType<Void>(VOID_TYPE, "void") {
        @Override
        boolean checkValueConstraint(Void value) {
            return value == null;
        }

        @Override
        Void readValue(DataInputStream dis) throws SerializeException {
            throw new SerializeException("cannot read void");
        }

        @Override
        void writeValue(DataOutputStream dos, Void value) throws SerializeException {
            throw new SerializeException("cannot write void");
        }

        @Override
        void write(DataOutputStream dos, Void value, boolean allowOptimize) throws SerializeException {
            throw new SerializeException("cannot write void");
        }

        @Override
        Void convert(TagType fromTagType, Object value) {
            // allow convert from any type to avoid
            return null;
        }
    };

    public static final TagType<Boolean> BOOLEAN = new TagType<Boolean>(0xC0, "boolean") {
        @Override
        Boolean readValue(DataInputStream dis) throws IOException {
            return dis.readBoolean();
        }

        @Override
        void writeValue(DataOutputStream dos, Boolean value) throws IOException {
            dos.writeBoolean(value);
        }
    };

    public static final TagType<Byte> BYTE = new TagType<Byte>(0xC1, "byte") {
        @Override
        Byte readValue(DataInputStream dis) throws IOException {
            return dis.readByte();
        }

        @Override
        void writeValue(DataOutputStream dos, Byte value) throws IOException {
            dos.writeByte(value);
        }

        @Override
        Byte convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == SHORT) {
                return SHORT.castValue(value).byteValue();
            }
            if (fromTagType == INT) {
                return INT.castValue(value).byteValue();
            }
            if (fromTagType == LONG) {
                return LONG.castValue(value).byteValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    /**
     * byte -> short
     */
    public static final TagType<Short> SHORT = new TagType<Short>(0xC2, "short") {
        @Override
        Short readValue(DataInputStream dis) throws IOException {
            return dis.readShort();
        }

        @Override
        void writeValue(DataOutputStream dos, Short value) throws IOException {
            dos.writeShort(value);
        }

        @Override
        void write(DataOutputStream dos, Short value, boolean allowOptimize) throws IOException, SerializeException {
            if (allowOptimize) {
                short v = value.shortValue();

                // пытаемся уместить в byte
                byte b = (byte) v;
                if (b == v) {
                    BYTE.write(dos, b, false);
                    return;
                }

            }
            // пишем как short
            super.write(dos, value, false);
        }

        @Override
        Short convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == BYTE) {
                return BYTE.castValue(value).shortValue();
            }
            if (fromTagType == INT) {
                return INT.castValue(value).shortValue();
            }
            if (fromTagType == LONG) {
                return LONG.castValue(value).shortValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    /**
     * byte -> short -> int
     */
    public static final TagType<Integer> INT = new TagType<Integer>(0xC3, "int") {
        @Override
        Integer readValue(DataInputStream dis) throws IOException {
            return dis.readInt();
        }

        @Override
        void writeValue(DataOutputStream dos, Integer value) throws IOException {
            dos.writeInt(value);
        }

        @Override
        void write(DataOutputStream dos, Integer value, boolean allowOptimize) throws IOException, SerializeException {
            if (allowOptimize) {
                int v = value.intValue();

                // пытаемся уместить в byte
                byte b = (byte) v;
                if (b == v) {
                    BYTE.write(dos, b, false);
                    return;
                }

                // пытаемся уместить в short
                short s = (short) v;
                if (s == v) {
                    SHORT.write(dos, s, false);
                    return;
                }
            }

            // пишем как int
            super.write(dos, value, false);
        }

        @Override
        Integer convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == BYTE) {
                return BYTE.castValue(value).intValue();
            }
            if (fromTagType == SHORT) {
                return SHORT.castValue(value).intValue();
            }
            if (fromTagType == LONG) {
                return LONG.castValue(value).intValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    /**
     * byte -> short -> int -> long
     */
    public static final TagType<Long> LONG = new TagType<Long>(0xC4, "long") {
        @Override
        Long readValue(DataInputStream dis) throws IOException {
            return dis.readLong();
        }

        @Override
        void writeValue(DataOutputStream dos, Long value) throws IOException {
            dos.writeLong(value);
        }

        @Override
        void write(DataOutputStream dos, Long value, boolean allowOptimize) throws IOException, SerializeException {
            if (allowOptimize) {
                long v = value.longValue();

                // пытаемся уместить в byte
                byte b = (byte) v;
                if (b == v) {
                    BYTE.write(dos, b, false);
                    return;
                }

                // пытаемся уместить в short
                short s = (short) v;
                if (s == v) {
                    SHORT.write(dos, s, false);
                    return;
                }

                // пытаемся уместить в int
                int i = (int) v;
                if (i == v) {
                    INT.write(dos, i, false);
                    return;
                }
            }
            // пишем как long
            super.write(dos, value, false);
        }

        @Override
        Long convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == BYTE) {
                return BYTE.castValue(value).longValue();
            }
            if (fromTagType == SHORT) {
                return SHORT.castValue(value).longValue();
            }
            if (fromTagType == INT) {
                return INT.castValue(value).longValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    public static final TagType<Float> FLOAT = new TagType<Float>(0xC5, "float") {
        @Override
        Float readValue(DataInputStream dis) throws IOException {
            return dis.readFloat();
        }

        @Override
        void writeValue(DataOutputStream dos, Float value) throws IOException {
            dos.writeFloat(value);
        }

        @Override
        Float convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == DOUBLE) {
                return DOUBLE.castValue(value).floatValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    public static final TagType<Double> DOUBLE = new TagType<Double>(0xC6, "double") {
        @Override
        Double readValue(DataInputStream dis) throws IOException {
            return dis.readDouble();
        }

        @Override
        void writeValue(DataOutputStream dos, Double value) throws IOException {
            dos.writeDouble(value);
        }

        @Override
        Double convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == FLOAT) {
                return FLOAT.castValue(value).doubleValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    static final int UINT8_MIN_VALUE = 0;
    static final int UINT8_MAX_VALUE = 0xff;

    /**
     * Беззнаковый 8-битный int
     */
    public static final TagType<Integer> UINT8 = new TagType<Integer>(0xC7, "uint8") {
        @Override
        boolean checkValueConstraint(Integer value) {
            int v = value.intValue();
            return (UINT8_MIN_VALUE <= v && v <= UINT8_MAX_VALUE);
        }

        @Override
        Integer readValue(DataInputStream dis) throws IOException {
            return Utils.readUByte(dis);
        }

        @Override
        void writeValue(DataOutputStream dos, Integer value) throws IOException {
            int v = value.intValue();
            dos.write(v);
        }

        @Override
        Integer convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == UINT16) {
                return UINT16.castValue(value).intValue();
            }
            return super.convert(fromTagType, value);
        }
    };

    static final int UINT16_MIN_VALUE = 0;
    static final int UINT16_MAX_VALUE = 0xffff;

    /**
     * Беззнаковый 16-битный int
     */
    public static final TagType<Integer> UINT16 = new TagType<Integer>(0xC8, "uint16") {
        @Override
        boolean checkValueConstraint(Integer value) {
            int v = value.intValue();
            return (UINT16_MIN_VALUE <= v && v <= UINT16_MAX_VALUE);
        }

        @Override
        Integer readValue(DataInputStream dis) throws IOException {
            int ch1 = Utils.readUByte(dis);
            int ch2 = Utils.readUByte(dis);
            return ((ch1 << 8) + ch2);
        }

        @Override
        void writeValue(DataOutputStream dos, Integer value) throws IOException {
            dos.writeShort(value);
        }

        @Override
        void write(DataOutputStream dos, Integer value, boolean allowOptimize) throws IOException, SerializeException {
            if (allowOptimize) {
                int v = value.intValue();

                // пытаемся уместить в uint8
                if (UINT8_MIN_VALUE <= v && v <= UINT8_MAX_VALUE) {
                    UINT8.write(dos, v, false);
                    return;
                }
            }
            // пишем как uint16
            super.write(dos, value, false);
        }

        @Override
        Integer convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == UINT8) {
                return UINT8.castValue(value);
            }
            return super.convert(fromTagType, value);
        }
    };

    public static final TagType<String> UTF8 = new TagType<String>(0xD0, "utf8") {
        @Override
        String readValue(DataInputStream dis) throws IOException, SerializeException {
            try {
                return dis.readUTF();
            } catch (UTFDataFormatException e) {
                throw new SerializeException("Error while reading UTF value", e);
            }
        }

        @Override
        void writeValue(DataOutputStream dos, String value) throws IOException {
            dos.writeUTF(value);
        }
    };

    static String byteArrValueToString(byte[] value, int maxLength, boolean cut) {
        String lenStr = "[" + value.length + " bytes]";
        if (!cut || value.length <= maxLength) {
            return lenStr + Utils.toHexString(value);
        }
        return lenStr + Utils.toHexString(value, 0, maxLength) + "...";
    }

    // 64 KB
    static final int MAX_LENGTH_BYTE_16 = 0xffff;
    // 128 MB
    static final int MAX_LENGTH_BYTE_32 = 0x8000000;

    /**
     * Сериализует байтовый массив длиной до MAX_LENGTH_BYTE_32
     */
    public static final TagType<byte[]> BYTE_ARR = new TagType<byte[]>(0xD1, "byte[]") {
        @Override
        boolean checkValueConstraint(byte[] value) {
            return value.length < MAX_LENGTH_BYTE_32;
        }

        @Override
        byte[] readValue(DataInputStream dis) throws IOException {
            int len = Utils.toUnsignedShort(dis.readShort());
            byte[] b = new byte[len];
            dis.readFully(b);
            return b;
        }

        @Override
        void writeValue(DataOutputStream dos, byte[] value) throws IOException {
            int len = value.length;
            dos.writeShort(len);
            dos.write(value);
        }

        @Override
        void write(DataOutputStream dos, byte[] value, boolean allowOptimize) throws IOException, SerializeException {
            int len = value.length;
            if (len <= MAX_LENGTH_BYTE_16) {
                super.write(dos, value, false);
                return;
            }

            // пишем как byte[] 32
            BYTE_ARR_32.write(dos, value, false);
        }

        @Override
        byte[] convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == BYTE_ARR_32) {
                return BYTE_ARR_32.castValue(value);
            }
            return super.convert(fromTagType, value);
        }

        private static final int TO_STRING_MAX_LENGTH = 512;

        @Override
        String valueToString(byte[] value) {
            return byteArrValueToString(value, TO_STRING_MAX_LENGTH, true);
        }

        @Override
        String valueToStringFull(byte[] value) {
            return byteArrValueToString(value, 0, false);
        }
    };

    /**
     * В явном виде не используется
     */
    private static final TagType<byte[]> BYTE_ARR_32 = new TagType<byte[]>(0xD2, "byte[]") {
        @Override
        boolean checkValueConstraint(byte[] value) {
            throw new RuntimeException("this type cannot be used explicit");
        }

        @Override
        byte[] readValue(DataInputStream dis) throws IOException, SerializeException {
            int len = dis.readInt();
            if (len < 0 || len > MAX_LENGTH_BYTE_32) {
                throw new SerializeException("Illegal length header: " + len);
            }
            byte[] b = new byte[len];
            dis.readFully(b);
            return b;
        }

        @Override
        void writeValue(DataOutputStream dos, byte[] value) throws IOException {
            int len = value.length;
            dos.writeInt(len);
            dos.write(value);
        }

        @Override
        byte[] convert(TagType fromTagType, Object value) throws SerializeException {
            throw new SerializeException("this type cannot be used explicit");
        }
    };

    public static final TagType<Date> UNIX_TIME = new TagType<Date>(0xD3, "unixtime") {
        @Override
        Date readValue(DataInputStream dis) throws IOException {
            int unixTime = dis.readInt();
            long time = unixTime * 1000L;
            return new Date(time);
        }

        @Override
        void writeValue(DataOutputStream dos, Date value) throws IOException {
            long time = value.getTime();
            int unixTime = (int) (time / 1000L);
            dos.writeInt(unixTime);
        }

        @Override
        String valueToString(Date value) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(value);
        }

        @Override
        Date convert(TagType fromTagType, Object value) throws SerializeException {
            if (fromTagType == LONG) {
                return new Date(LONG.castValue(value).longValue());
            }
            return super.convert(fromTagType, value);
        }
    };

    // todo biginteger, bigdecimal
}
