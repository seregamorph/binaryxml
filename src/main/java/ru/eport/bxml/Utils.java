package ru.eport.bxml;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

class Utils {
    private Utils() {
    }

    static int toUnsignedByte(byte b) {
        return b & 0xff;
    }

    static int toUnsignedShort(short v) {
        return v & 0xffff;
    }

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

    /**
     * Прочитать байт из потока ввода с проверкой EOF
     *
     * @param dis
     * @return прочитанный беззнаковый байт из потока ввода
     * @throws IOException
     * @throws EOFException
     */
    static int readUByte(DataInputStream dis) throws IOException {
        int ch = dis.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    public static void main(String[] args) {
        System.out.println(toHexString(new byte[]{0x00, 0x01, (byte) 0xfe, (byte) 0xff}));

        System.out.println(toUnsignedByte((byte) 0x00));
        System.out.println(toUnsignedByte((byte) 0x01));
        System.out.println(toUnsignedByte((byte) 0xff));

        System.out.println(toUnsignedShort((short) 0x00));
        System.out.println(toUnsignedShort((short) 0xff));
        System.out.println(toUnsignedShort((short) 0xffff));
        System.out.println(toUnsignedShort((short) -0xffff));
    }
}
