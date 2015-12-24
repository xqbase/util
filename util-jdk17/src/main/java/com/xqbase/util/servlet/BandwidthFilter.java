package com.xqbase.util.servlet;

import java.io.Closeable;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.xqbase.util.Numbers;
import com.xqbase.util.Time;
import com.xqbase.util.concurrent.CountLock;
import com.xqbase.util.concurrent.LockMap;

class BandwidthOutputStream extends ServletOutputStream {
	static final int MAX_INTERVAL = 16;

	private ServletOutputStream out;
	private CountLock lock;
	private int limit, blockSize;

	public BandwidthOutputStream(ServletOutputStream out, CountLock lock, int limit) {
		this.out = out;
		this.lock = lock;
		this.limit = limit;
		blockSize = limit * MAX_INTERVAL;
	}

	private void write(byte[] b, int off, int len, int interval) throws IOException {
		if (lock.get() > 1) {
			lock.lock();
			try {
				out.write(b, off, len);
				// Speed = Block-Size / Interval
				Time.sleep(interval);
			} finally {
				lock.unlock();
			}
		} else {
			// Do not lock if only one request
			out.write(b, off, len);
			Time.sleep(interval);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int written = off;
		int remaining = len;
		while (remaining >= blockSize) {
			write(b, written, blockSize, MAX_INTERVAL);
			written += blockSize;
			remaining -= blockSize;
		}
		if (remaining == 0) {
			return;
		}
		int interval = remaining / limit;
		if (interval > 0) {
			write(b, written, remaining, interval);
		} else {
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

class BandwidthWrapper implements Closeable {
	private static LockMap<String> lockMap = new LockMap<>(true);

	private BandwidthOutputStream out = null;
	private CountLock lock = null;
	private ServletResponse resp;
	private String ip;
	private int limit;

	BandwidthWrapper(ServletResponse resp, String ip, int limit) {
		this.resp = resp;
		this.ip = ip;
		this.limit = limit;
	}

	ServletResponse getResponse() {
		if (resp instanceof HttpServletResponse) {
			return new HttpServletResponseWrapper((HttpServletResponse) resp) {
				@Override
				public ServletOutputStream getOutputStream() throws IOException {
					return getOut();
				}
			};
		}
		return new ServletResponseWrapper(resp) {
			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				return getOut();
			}
		};
	}

	ServletOutputStream getOut() throws IOException {
		if (out != null) {
			return out;
		}
		ServletOutputStream out_ = resp.getOutputStream();
		// Throw before lock
		lock = lockMap.acquire(ip);
		out = new BandwidthOutputStream(out_, lock, limit);
		return out;
	}

	@Override
	public void close() {
		if (out != null) {
			lockMap.release(ip, lock);
		}
	}
}

public class BandwidthFilter implements Filter {
	private int limit;

	@Override
	public void init(FilterConfig conf) {
		// Block-Size = Speed (64K) * Interval (16ms)
		limit = Numbers.parseInt(conf.getInitParameter("limit"), 1024);
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (limit > 0) {
			try (BandwidthWrapper wrapper =
					new BandwidthWrapper(resp, req.getRemoteAddr(), limit)) {
				chain.doFilter(req, wrapper.getResponse());
			}
		} else {
			chain.doFilter(req, resp);
		}
	}
}