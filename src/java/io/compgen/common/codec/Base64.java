package io.compgen.common.codec;

public class Base64 {

	public static final char[] base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	
	public static String encodeBase64(byte[] bytes) {
		String s = "";

		for (int i=0; i < bytes.length; i+=3) {
			int one = bytes[i] & 0xFF;
			int two = (i+1) < bytes.length ? bytes[i+1] & 0xFF : 0;
			int three = (i+2) < bytes.length ? bytes[i+2] & 0xFF : 0;

			int idx1 = one >> 2;
			int idx2 = ((one << 4) | (two >> 4)) & 0x3F;
			int idx3 = ((two << 2) | (three >> 6)) & 0x3F;
			int idx4 = three & 0x3F;

			s += base64[idx1];
			s += base64[idx2];

			if ((i+1) < bytes.length) {
				s += base64[idx3];
			}
			if ((i+2) < bytes.length) {
				s += base64[idx4];
			}
		}
		
		if (bytes.length % 3 == 2) {
			s += "=";
		} else if (bytes.length % 3 == 1) {
			s += "==";
		}
		
		return s;
	}
	
	public static void main(String[] args) {
		System.out.println(encodeBase64(args[0].getBytes()));
	}
}