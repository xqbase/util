package com.xqbase.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
	private static final int BUFFER_SIZE = 2048;

	public static void copy(InputStream in,
			OutputStream out, boolean flush) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytesRead);
			if (flush) {
				out.flush();
			}
		}
	}

	public static void copy(InputStream in,
			OutputStream out) throws IOException {
		copy(in, out, false);
	}
}