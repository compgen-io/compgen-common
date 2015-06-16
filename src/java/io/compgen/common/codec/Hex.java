package io.compgen.common.codec;

public class Hex {
	public static final char[] hexChars = "0123456789ABCDEF".toCharArray();
	public static String toHexString(byte[] bytes) {
		char[] out = new char[bytes.length * 2];
		for (int i=0; i< bytes.length; i++) {
			int val = bytes[i] & 0xFF;
			int hi = val >> 4;
			int lo = val & 0x0F;
			out[i * 2] = hexChars[hi];
			out[i * 2 + 1] = hexChars[lo];
		}
		return new String(out);
	}
}
