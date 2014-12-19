package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.xqbase.util.Numbers;
import com.xqbase.util.Time;
import com.xqbase.util.concurrent.CountLock;
import com.xqbase.util.concurrent.LockMap;

class BandwidthOutputStream extends ServletOutputStream {
	private ServletOutputStream out;
	private CountLock lock;
	private int block;

	public BandwidthOutputStream(ServletOutputStream out, CountLock lock, int block) {
		this.out = out;
		this.lock = lock;
		this.block = block;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int written = off;
		int remaining = len;
		while (remaining >= block) {
			if (lock.get() > 1) {
				lock.lock();
				try {
					out.write(b, written, block);
					// Speed = Block-Size / Interval
					Time.sleep(16);
				} finally {
					lock.unlock();
				}
			} else {
				// Do not lock if only one request
				out.write(b, written, block);
				Time.sleep(16);
			}
			written += block;
			remaining -= block;
		}
		if (remaining > 0) {
			out.write(b, written, remaining);
		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public boolean isReady() {
		return out.isReady();
	}

	@Override
	public void setWriteListener(WriteListener listener) {
		out.setWriteListener(listener);
	}
}

class BandwidthResponse extends HttpServletResponseWrapper implements AutoCloseable {
	private static LockMap<String> lockMap = new LockMap<>(true);

	private BandwidthOutputStream out = null;
	private CountLock lock = null;
	private String ip;
	private int block;

	BandwidthResponse(HttpServletResponse resp, String ip, int block) {
		super(resp);
		this.ip = ip;
		this.block = block;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (out != null) {
			return out;
		}
		ServletOutputStream out_ = getResponse().getOutputStream();
		// Throw before lock
		lock = lockMap.acquire(ip);
		out = new BandwidthOutputStream(out_, lock, block);
		return out;
	}

	@Override
	public void close() {
		if (out != null) {
			lockMap.release(ip, lock);
		}
	}
}

public class BandwidthWrapper implements WrapperFactory {
	private int block;

	public BandwidthWrapper(ServletContext sc) {
		// Block-Size = Speed (64K) * Interval (16ms)
		block = Numbers.parseInt(sc.getInitParameter(BandwidthWrapper.
				class.getName() + ".limit"), 64) * 16;
	}

	@Override
	public BandwidthResponse getWrapper(HttpServletRequest req,
			HttpServletResponse resp) {
		return new BandwidthResponse(resp, req.getRemoteAddr(), block);
	}
}