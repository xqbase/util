package com.xqbase.util.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Bytes;

public class HttpForm {
	private ByteArrayQueue body = new ByteArrayQueue();
	private LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
	private boolean multipart, appended = false;
	private String boundary = null;
	private byte[] boundaryBytes = null;
	private Charset charset;

	public HttpForm(boolean multipart, Charset charset) {
		this.multipart = multipart;
		this.charset = charset;
		String contentType;
		if (multipart) {
			boundary = Bytes.toHexLower(Bytes.random(16));
			boundaryBytes = boundary.getBytes();
			contentType = "multipart/form-data; boundary=" + boundary;
		} else {
			contentType = "application/x-www-form-urlencoded";
		}
		headers.put("Content-Type", Collections.singletonList(contentType));
	}

	public ByteArrayQueue getBody() {
		return body;
	}

	public LinkedHashMap<String, List<String>> getHeaders() {
		return headers;
	}

	private static final byte[]
			EQ = {'='},
			AMP = {'&'},
			QUOT = {'"'},
			DASH = {'-', '-'},
			CRLF = {'\r', '\n'},
			CONTENT_DISPOSITION_NAME = "Content-Disposition: form-data; name=\"".getBytes(),
			FILENAME = "; filename=\"".getBytes(),
			CONTENT_TYPE = "Content-Type: ".getBytes();

	private String encode(String s) {
		try {
			return URLEncoder.encode(s, charset.name());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void add(String name, String value) {
		if (multipart) {
			next(name, null, null);
			body.add(value.getBytes(charset));
			return;
		}
		if (appended) {
			body.add(AMP);
		}
		appended = true;
		body.add(encode(name).getBytes()).add(EQ).add(encode(value).getBytes());
	}

	public void next(String name, String fileName, String contentType) {
		if (appended) {
			body.add(CRLF);
		}
		appended = true;
		body.add(DASH).add(boundaryBytes).add(CRLF);
		body.add(CONTENT_DISPOSITION_NAME).add(name.getBytes(charset)).add(QUOT);
		if (fileName != null) {
			body.add(FILENAME).add(fileName.getBytes(charset)).add(QUOT);
		}
		body.add(CRLF);
		if (contentType != null) {
			body.add(CONTENT_TYPE).add(contentType.getBytes()).add(CRLF);
		}
		body.add(CRLF);
	}

	public void finish() {
		if (appended) {
			body.add(CRLF);
		}
		body.add(DASH).add(boundaryBytes).add(DASH).add(CRLF);
	}
}