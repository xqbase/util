package com.xqbase.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Variety of <b>byte[]</b> operations, including:
 * <li>Encoding/Decoding to/from hexadecimal</li>
 * <li>Storing/Retrieving <b>short</b>, <b>int</b> or <b>long</b> to/from <b>byte[]</b></li>
 * <li>Concatenating/Truncating/Comparing of <b>byte[]</b>s
 * <li>Generating random <b>byte[]</b></li>
 * <li>Dumping for debug</li>
 */
public class Bytes {
	/** Not Little-Ending, i.e. storing/retrieving numbers in Big-Ending (default) byte order */
	public static final boolean BIG_ENDIAN = false;
	/** Little-Ending, i.e. storing/retrieving numbers in Little-Ending byte order */
	public static final boolean LITTLE_ENDIAN = true;

	/** A byte array with length of <b>0</b> */
	public static final byte[] EMPTY_BYTES = {};

	private static final char[] HEX_UPPER_CHAR = "0123456789ABCDEF".toCharArray();
	private static final char[] HEX_LOWER_CHAR = "0123456789abcdef".toCharArray();

	private static final byte[] HEX_DECODE_CHAR = {
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  0,  0,  0,  0,  0,  0,
		0, 10, 11, 12, 13, 14, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0, 10, 11, 12, 13, 14, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	};

	/** Decode from hexadecimal */
	public static byte[] fromHex(String hex) {
		char[] c = hex.toCharArray();
		byte[] b = new byte[c.length / 2];
		for (int i = 0; i < b.length; i ++) {
			b[i] = (byte) (HEX_DECODE_CHAR[c[i * 2] & 0xFF] * 16 +
					HEX_DECODE_CHAR[c[i * 2 + 1] & 0xFF]);
		}
		return b;
	}

	private static String toHex(byte[] b, int off, int len, char[] hexChar) {
		char[] c = new char[len * 2];
		for (int i = 0; i < len; i ++) {
			int n = b[off + i];
			c[i * 2] = hexChar[(n & 0xF0) >>> 4];
			c[i * 2 + 1] = hexChar[n & 0xF];
		}
		return new String(c);
	}

	/** Encode to uppercase hexadecimal */
	public static String toHexUpper(byte[] b) {
		return toHexUpper(b, 0, b.length);
	}

	/** Encode to uppercase hexadecimal */
	public static String toHexUpper(byte[] b, int off, int len) {
		return toHex(b, off, len, HEX_UPPER_CHAR);
	}

	/** Encode to lowercase hexadecimal */
	public static String toHexLower(byte[] b) {
		return toHexLower(b, 0, b.length);
	}

	/** Encode to lowercase hexadecimal */
	public static String toHexLower(byte[] b, int off, int len) {
		return toHex(b, off, len, HEX_LOWER_CHAR);
	}

	/** Generate a 2-byte array from a <b>short</b> number in Big-Endian byte order */
	public static byte[] fromShort(int n) {
		return fromShort(n, BIG_ENDIAN);
	}

	/** Generate a 2-byte array from a <b>short</b> number in a given byte order */
	public static byte[] fromShort(int n, boolean littleEndian) {
		byte[] b = new byte[2];
		setShort(n, b, 0, littleEndian);
		return b;
	}

	/** Store a <b>short</b> number into a byte array in Big-Endian byte order */
	public static void setShort(int n, byte[] b, int off) {
		setShort(n, b, off, BIG_ENDIAN);
	}

	/** Store a <b>short</b> number into a byte array in a given byte order */
	public static void setShort(int n, byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			b[off] = (byte) n;
			b[off + 1] = (byte) (n >>> 8);
		} else {
			b[off] = (byte) (n >>> 8);
			b[off + 1] = (byte) n;
		}
	}

	/** Generate a 4-byte array from an <b>int</b> number in Big-Endian byte order */
	public static byte[] fromInt(int n) {
		return fromInt(n, BIG_ENDIAN);
	}

	/** Generate a 4-byte array from an <b>int</b> number in a given byte order */
	public static byte[] fromInt(int n, boolean littleEndian) {
		byte[] b = new byte[4];
		setInt(n, b, 0, littleEndian);
		return b;
	}

	/** Store an <b>int</b> number into a byte array in Big-Endian byte order */
	public static void setInt(int n, byte[] b, int off) {
		setInt(n, b, off, BIG_ENDIAN);
	}

	/** Store an <b>int</b> number into a byte array in a given byte order */
	public static void setInt(int n, byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			b[off] = (byte) n;
			b[off + 1] = (byte) (n >>> 8);
			b[off + 2] = (byte) (n >>> 16);
			b[off + 3] = (byte) (n >>> 24);
		} else {
			b[off] = (byte) (n >>> 24);
			b[off + 1] = (byte) (n >>> 16);
			b[off + 2] = (byte) (n >>> 8);
			b[off + 3] = (byte) n;
		}
	}

	/** Generate an 8-byte array from a <b>long</b> number in Big-Endian byte order */
	public static byte[] fromLong(long n) {
		return fromLong(n, BIG_ENDIAN);
	}

	/** Generate an 8-byte array from a <b>long</b> number in a given byte order */
	public static byte[] fromLong(long n, boolean littleEndian) {
		byte[] b = new byte[8];
		setLong(n, b, 0, littleEndian);
		return b;
	}

	/** Store a <b>long</b> number into a byte array in Big-Endian byte order */
	public static void setLong(long n, byte[] b, int off) {
		setLong(n, b, off, BIG_ENDIAN);
	}

	/** Store a <b>long</b> number into a byte array in a given byte order */
	public static void setLong(long n, byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			setInt((int) n, b, off, LITTLE_ENDIAN);
			setInt((int) (n >>> 32), b, off + 4, LITTLE_ENDIAN);
		} else {
			setInt((int) (n >>> 32), b, off, BIG_ENDIAN);
			setInt((int) n, b, off + 4, BIG_ENDIAN);
		}
	}

	/** Retrieve a <b>short</b> from a byte array in Big-Endian byte order */
	public static int toShort(byte[] b) {
		return toShort(b, 0);
	}

	/** Retrieve a <b>short</b> from a byte array in a given byte order */
	public static int toShort(byte[] b, boolean littleEndian) {
		return toShort(b, 0, littleEndian);
	}

	/** Retrieve a <b>short</b> from a byte array in Big-Endian byte order */
	public static int toShort(byte[] b, int off) {
		return toShort(b, off, BIG_ENDIAN);
	}

	/** Retrieve a <b>short</b> from a byte array in a given byte order */
	public static int toShort(byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			return ((b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8));
		}
		return (((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF));
	}

	/** Retrieve an <b>int</b> from a byte array in Big-Endian byte order */
	public static int toInt(byte[] b) {
		return toInt(b, 0);
	}

	/** Retrieve an <b>int</b> from a byte array in a given byte order */
	public static int toInt(byte[] b, boolean littleEndian) {
		return toInt(b, 0, littleEndian);
	}

	/** Retrieve an <b>int</b> from a byte array in Big-Endian byte order */
	public static int toInt(byte[] b, int off) {
		return toInt(b, off, BIG_ENDIAN);
	}

	/** Retrieve an <b>int</b> from a byte array in a given byte order */
	public static int toInt(byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) |
					((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
		}
		return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) |
				((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
	}

	/** Retrieve a <b>long</b> from a byte array in Big-Endian byte order */
	public static long toLong(byte[] b) {
		return toLong(b, 0);
	}

	/** Retrieve a <b>long</b> from a byte array in a given byte order */
	public static long toLong(byte[] b, boolean littleEndian) {
		return toLong(b, 0, littleEndian);
	}

	/** Retrieve a <b>long</b> from a byte array in Big-Endian byte order */
	public static long toLong(byte[] b, int off) {
		return toLong(b, off, BIG_ENDIAN);
	}

	/** Retrieve a <b>long</b> from a byte array in a given byte order */
	public static long toLong(byte[] b, int off, boolean littleEndian) {
		if (littleEndian) {
			return (toInt(b, off, LITTLE_ENDIAN) & 0xFFFFFFFFL) |
					((toInt(b, off + 4, LITTLE_ENDIAN) & 0xFFFFFFFFL) << 32);
		}
		return ((toInt(b, off, BIG_ENDIAN) & 0xFFFFFFFFL) << 32) |
				(toInt(b, off + 4, BIG_ENDIAN) & 0xFFFFFFFFL);
	}

	/** Concatenate 2 byte arrays */
	public static byte[] add(byte[] b1, int off1, int len1, byte[] b2, int off2, int len2) {
		byte[] b = new byte[len1 + len2];
		System.arraycopy(b1, off1, b, 0, len1);
		System.arraycopy(b2, off2, b, len1, len2);
		return b;
	}

	/** Concatenate <i>n</i> byte arrays */
	public static byte[] add(byte[]... b) {
		int nLen = 0;
		for (int i = 0; i < b.length; i ++) {
			nLen += b[i].length;
		}
		byte[] lp = new byte[nLen];
		nLen = 0;
		for (int i = 0; i < b.length; i ++) {
			byte[] lpi = b[i];
			System.arraycopy(lpi, 0, lp, nLen, lpi.length);
			nLen += lpi.length;
		}
		return lp;
	}

	/** Clone a byte array */
	public static byte[] clone(byte[] b) {
		return sub(b, 0, b.length);
	}

	/** Truncate and keep left part of a byte array */
	public static byte[] left(byte[] b, int len) {
		return sub(b, 0, len);
	}

	/** Truncate and keep right part of a byte array (by a given length) */
	public static byte[] right(byte[] b, int len) {
		return sub(b, b.length - len, len);
	}

	/** Truncate and keep right part of a byte array (by a given position) */
	public static byte[] sub(byte[] b, int off) {
		return sub(b, off, b.length - off);
	}

	/** Truncate and keep middle part of a byte array */
	public static byte[] sub(byte[] b, int off, int len) {
		byte[] result = new byte[len];
		System.arraycopy(b, off, result, 0, len);
		return result;
	}

	/** Check whether a byte array equals to another */
	public static boolean equals(byte[] b1, int off1, byte[] b2, int off2, int len) {
		if (b1 == b2 && off1 == off2) {
			return true;
		}
		for (int i = 0; i < len; i ++) {
			if (b1[off1 + i] != b2[off2 + i]) {
				return false;
			}
		}
		return true;
	}

	/** Check whether a byte array equals to another */
	public static boolean equals(byte[] b1, byte[] b2) {
		return b1.length == b2.length && equals(b1, 0, b2, 0, b1.length);
	}

	private static Random random = new Random();
	private static SecureRandom secureRandom = new SecureRandom();

	private static byte[] random(int len, Random random_) {
		byte[] result = new byte[len];
		random_.nextBytes(result);
		return result;
	}

	/** Generate a random byte array by a given length */
	public static byte[] random(int len) {
		return random(len, random);
	}

	/** Generate a random byte array by a given length, using secure random number generator */
	public static byte[] secureRandom(int len) {
		return random(len, secureRandom);
	}

	private static String dumpLine(byte[] b, int offDiv16, int begin, int end) {
		char[] line = "                                                                             ".toCharArray();
		//			  "0000:0000  00 00 00 00 00 00 00 00-00 00 00 00 00 00 00 00   ................"
		line[0] = HEX_UPPER_CHAR[(offDiv16 >>> 24) & 0xF];
		line[1] = HEX_UPPER_CHAR[(offDiv16 >>> 20) & 0xF];
		line[2] = HEX_UPPER_CHAR[(offDiv16 >>> 16) & 0xF];
		line[3] = HEX_UPPER_CHAR[(offDiv16 >>> 12) & 0xF];
		line[4] = ':';
		line[5] = HEX_UPPER_CHAR[(offDiv16 >>> 8) & 0xF];
		line[6] = HEX_UPPER_CHAR[(offDiv16 >>> 4) & 0xF];
		line[7] = HEX_UPPER_CHAR[offDiv16 & 0xF];
		line[8] = '0';
		for (int i = 0; i < 16; i ++) {
			if (i >= begin && i < end) {
				int b_ = b[offDiv16 * 16 + i];
				line[i * 3 + 11] = HEX_UPPER_CHAR[(b_ & 0xF0) >> 4];
				line[i * 3 + 12] = HEX_UPPER_CHAR[b_ & 0xF];
				if (b_ >= 32 && b_ < 127) {
					line[61 + i] = (char) b_;
				} else {
					line[61 + i] = '.';
				}
			}
		}
		if (begin < 8 && end > 8) {
			line[34] = '-';
		}
		return new String(line);
	}

	/** Dump a byte array into a {@link PrintStream} in human readable format */
	public static void dump(PrintStream out, byte[] b, int off, int len) {
		int end = off + len;
		int offDiv16 = off / 16;
		int endDiv16 = end / 16;
		if (offDiv16 == endDiv16) {
			out.println(dumpLine(b, offDiv16, off % 16, end % 16));
		} else {
			out.println(dumpLine(b, offDiv16, off % 16, 16));
			for (int i = offDiv16 + 1; i < endDiv16; i ++) {
				out.println(dumpLine(b, i, 0, 16));
			}
			if (end % 16 > 0) {
				out.println(dumpLine(b, endDiv16, 0, end % 16));
			}
		}
	}

	/** Dump a byte array into a {@link PrintStream} in human readable format */
	public static void dump(PrintStream out, byte[] b) {
		dump(out, b, 0, b.length);
	}

	/** Dump a byte array into a {@link PrintWriter} in human readable format */
	public static void dump(PrintWriter out, byte[] b, int off, int len) {
		int end = off + len;
		int offDiv16 = off / 16;
		int endDiv16 = end / 16;
		if (offDiv16 == endDiv16) {
			out.println(dumpLine(b, offDiv16, off % 16, end % 16));
		} else {
			out.println(dumpLine(b, offDiv16, off % 16, 16));
			for (int i = offDiv16 + 1; i < endDiv16; i ++) {
				out.println(dumpLine(b, i, 0, 16));
			}
			if (end % 16 > 0) {
				out.println(dumpLine(b, endDiv16, 0, end % 16));
			}
		}
	}

	/** Dump a byte array into a {@link PrintWriter} in human readable format */
	public static void dump(PrintWriter out, byte[] b) {
		dump(out, b, 0, b.length);
	}
}