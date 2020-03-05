package net.dblsaiko.forgething;

public class DatatypeUtil {

    public static byte[] parse(String data) {
        if (data.length() % 2 != 0) throw new IllegalArgumentException("");
        byte[] arr = new byte[data.length() / 2];
        for (int i = 0; i < arr.length; i += 1) {
            String s = data.substring(i * 2, (i * 2) + 2);
            byte b = (byte) Integer.parseUnsignedInt(s, 16);
            arr[i] = b;
        }
        return arr;
    }

    public static String toString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String str = Integer.toHexString(Byte.toUnsignedInt(b));
            if (str.length() < 2) sb.append('0');
            sb.append(str);
        }
        return sb.toString();
    }

}
