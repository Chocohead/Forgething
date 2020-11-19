package net.dblsaiko.forgething;

public class DatatypeUtil {
	public static byte[] parse(String data) {
		int dataLength = data.length();
		if (dataLength % 2 != 0) throw new IllegalArgumentException("Uneven data length " + dataLength);

		byte[] arr = new byte[dataLength / 2];

		for (int i = 0, d = 0; d < dataLength; i++, d += 2) {
			String s = data.substring(d, d + 2);
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
