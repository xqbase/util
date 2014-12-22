package com.xqbase.util;

public class Base64 {
	private static final int PADDING_BYTE = '=';

	private static final char[] ENC_TAB =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

	private static final int[] DEC_TAB = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
		-1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
		-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
		41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	};

	public static String encode(byte[] data) {
		return encode(data, 0, data.length);
	}

	public static String encode(byte[] data, int off, int len) {
		char[] result = new char[(len + 2) / 3 * 4];
		int modulus = len % 3;
		int length = len - modulus;
		int j = 0;
		for (int i = 0; i < length; i += 3) {
			int b1 = data[off + i] & 0xFF;
			int b2 = data[off + i + 1] & 0xFF;
			int b3 = data[off + i + 2] & 0xFF;
			result[j] = ENC_TAB[(b1 >>> 2) & 0x3F];
			j ++;
			result[j] = ENC_TAB[((b1 << 4) | (b2 >>> 4)) & 0x3F];
			j ++;
			result[j] = ENC_TAB[((b2 << 2) | (b3 >>> 6)) & 0x3F];
			j ++;
			result[j] = ENC_TAB[b3 & 0x3F];
			j ++;
		}
		if (modulus > 0) {
			int b1 = data[off + length] & 0xFF;
			result[j] = ENC_TAB[(b1 >>> 2) & 0x3F];
			j ++;
			if (modulus > 1) {
				int b2 = data[off + length + 1] & 0xFF;
				result[j] = ENC_TAB[((b1 << 4) | (b2 >>> 4)) & 0x3F];
				j ++;
				result[j] = ENC_TAB[(b2 << 2) & 0x3F];
				j ++;
			} else {
				result[j] = ENC_TAB[(b1 << 4) & 0x3F];
				j ++;
				result[j] = PADDING_BYTE;
				j ++;
			}
			result[j] = PADDING_BYTE;
			j ++;
		}
		return new String(result);
	}

	public static byte[] decode(String str) {
		char[] buffer = str.toCharArray();
		int length = 0;
		for (int i = 0; i < buffer.length; i ++) {
			int dec = DEC_TAB[buffer[i] & 0xFF];
			if (dec != -1) {
				buffer[length] = (char) dec;
				length ++;
			}
		}
		int modulus = length % 4;
		length -= modulus;
		byte[] result = new byte[length / 4 * 3 + (modulus < 2 ? 0 : modulus < 3 ? 1 : 2)];
		int j = 0;
		for (int i = 0; i < length; i += 4) {
			int b1 = buffer[i];
			int b2 = buffer[i + 1];
			int b3 = buffer[i + 2];
			int b4 = buffer[i + 3];
			result[j] = (byte) ((b1 << 2) | (b2 >> 4));
			j ++;
			result[j] = (byte) ((b2 << 4) | (b3 >> 2));
			j ++;
			result[j] = (byte) ((b3 << 6) | b4);
			j ++;
		}
		if (modulus > 1) {
			int b1 = buffer[length];
			int b2 = buffer[length + 1];
			result[j] = (byte) ((b1 << 2) | (b2 >> 4));
			j ++;
			if (modulus > 2) {
				int b3 = buffer[length + 2];
				result[j] = (byte) ((b2 << 4) | (b3 >> 2));
				j ++;
			}
		}
		return result;
	}
}