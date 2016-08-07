package com.xqbase.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * A queue of bytes where a sequence of bytes can be
 * added (into the tail) or retrieved (from the head).
 */
public class ByteArrayQueue implements Cloneable {
	private byte[] array;
	private int offset = 0;
	private int length = 0;
	private boolean shared = false;

	/** Creates a ByteArrayQueue that shares this ByteArrayQueue's content. */
	@Override
	public ByteArrayQueue clone() {
		return new ByteArrayQueue(array, offset, length);
	}

	/** Creates a ByteArrayQueue that shares a byte array. */
	public ByteArrayQueue(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
		this.shared = true;
	}

	/** Creates a ByteArrayQueue with the initial capacity of 32 bytes. */
	public ByteArrayQueue() {
		this(32);
	}

	/** Creates a ByteArrayQueue with the initial capacity of the given bytes. */
	public ByteArrayQueue(int capacity) {
		array = new byte[capacity];
	}

	/**
	 * @return The array which the bytes in the queue are stored.
	 * @see #offset()
	 * @see #length()
	 */
	public byte[] array() {
		return array;
	}

	/**
	 * @return The index of the array at the head of the queue.
	 * @see #array()
	 * @see #length()
	 */
	public int offset() {
		return offset;
	}

	/**
	 * @return The total number of available bytes in the queue.
	 * @see #array()
	 * @see #length()
	 */
	public int length() {
		return length;
	}

	/** Clears the queue. */
	public void clear() {
		offset = 0;
		length = 0;
		// Release buffer
		if (array.length > 1024) {
			array = new byte[32];
			shared = false;
		}
	}

	/** @param capacity - New capacity. */
	public void setCapacity(int capacity) {
		byte[] newArray = new byte[Math.max(capacity, length)];
		System.arraycopy(array, offset, newArray, 0, length);
		array = newArray;
		offset = 0;
		shared = false;
	}

	private int addLength(int len) {
		int newLength = length + len;
		if (shared || newLength > array.length) {
			setCapacity(Math.max(array.length << 1, newLength));
		} else if (offset + newLength > array.length) {
			System.arraycopy(array, offset, array, 0, length);
			offset = 0;
		}
		return newLength;
	}

	/**
	 * Adds a sequence of bytes into the tail of the queue,
	 * equivalent to <code>add(b, 0, b.length).</code>
	 */
	public ByteArrayQueue add(byte[] b) {
		return add(b, 0, b.length);
	}

	/** Adds a sequence of bytes into the tail of the queue. */
	public ByteArrayQueue add(byte[] b, int off, int len) {
		int newLength = addLength(len);
		System.arraycopy(b, off, array, offset + length, len);
		length = newLength;
		return this;
	}

	/** Adds one byte into the tail of the queue. */
	public ByteArrayQueue add(int b) {
		int newLength = addLength(1);
		array[offset + length] = (byte) b;
		length = newLength;
		return this;
	}

	/**
	 * @return An {@link OutputStream} suitable for writing binary data
	 *			into the tail of the queue.
	 * @see #add(byte[])
	 * @see #add(byte[], int, int)
	 */
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) {
				add(b);
			}

			@Override
			public void write(byte[] b, int off, int len) {
				add(b, off, len);
			}
		};
	}

	/**
	 * Retrieves a sequence of bytes from the head of the queue,
	 * equivalent to <code>remove(b, 0, b.length).</code>
	 */
	public ByteArrayQueue remove(byte[] b) {
		return remove(b, 0, b.length);
	}

	/** Retrieves a sequence of bytes from the head of the queue. */
	public ByteArrayQueue remove(byte[] b, int off, int len) {
		System.arraycopy(array, offset, b, off, len);
		return remove(len);
	}

	/** Removes a sequence of bytes from the head of the queue. */
	public ByteArrayQueue remove(int len) {
		offset += len;
		length -= len;
		// Release buffer if empty
		if (length == 0 && array.length > 1024) {
			array = new byte[32];
			offset = 0;
			shared = false;
		}
		return this;
	}

	/** Retrieves one byte from the head of the queue. */
	public int remove() {
		int b = array[offset] & 0xFF;
		remove(1);
		return b;
	}

	/**
	 * @return An {@link InputStream} suitable for reading binary data
	 *			from the head of the queue.
	 * @see #remove(byte[])
	 * @see #remove(byte[], int, int)
	 */
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() {
				return length() == 0 ? -1 : remove();
			}

			@Override
			public int read(byte[] b, int off, int len) {
				if (length() == 0) {
					return -1;
				}
				int bytesToRead = Math.min(len, length());
				remove(b, off, bytesToRead);
				return bytesToRead;
			}

			@Override
			public int available() {
				return length();
			}
		};
	}

	/**
	 * Converts the queue's contents into a string decoding bytes using
	 * the platform's default character set.
	 *
	 * @return String decoded from the queue's contents.
	 */
	@Override
	public String toString() {
		return new String(array, offset, length);
	}

	/**
	 * Converts the queue's contents into a string by decoding the bytes using
	 * the specified {@link java.nio.charset.Charset charset}
	 *
	 * @param charset - A supported {@link java.nio.charset.Charset charset}.
	 * @return String decoded from the queue's contents.
	 */
	public String toString(Charset charset) {
		return new String(array, offset, length, charset);
	}

	/**
	 * Converts the queue's contents into a string by decoding the bytes using
	 * the specified {@link java.nio.charset.Charset charsetName}
	 *
	 * @param charsetName - The name of a supported
	 *		  {@link java.nio.charset.Charset charset}.
	 * @return String decoded from the queue's contents.
	 */
	public String toString(String charsetName) throws UnsupportedEncodingException {
		return new String(array, offset, length, charsetName);
	}
}