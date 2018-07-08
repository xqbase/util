package com.xqbase.util.sns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.xqbase.util.Log;

public class WhoisIp {
	public static List<String> getNetNames(String ip) {
		List<String> infos = new ArrayList<>();
		String refer = null;
		try (Socket socket = new Socket("whois.iana.org", 43)) {
			socket.getOutputStream().write((ip + "\r\n").getBytes());
			try (BufferedReader in = new BufferedReader(new
					InputStreamReader(socket.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					line = line.toLowerCase();
					if (line.startsWith("refer:")) {
						refer = line.substring(6).trim();
						break;
					}
				}
			}
		} catch (IOException e) {
			Log.w("Error Parsing " + ip + ": " + e.getMessage());
			return infos;
		}
		if (refer == null) {
			return infos;
		}
		try (Socket socket = new Socket(refer, 43)) {
			socket.getOutputStream().write((ip + "\r\n").getBytes());
			try (BufferedReader in = new BufferedReader(new
					InputStreamReader(socket.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					System.err.println(line);
					if (line.toLowerCase().startsWith("netname:")) {
						infos.add(line.substring(8).trim());
						break;
					}
				}
			}
		} catch (IOException e) {
			Log.w("Error Parsing " + ip + ": " + e.getMessage());
		}
		return infos;
	}

	public static void main(String[] args) {
		System.out.println(getNetNames("112.1.161.0"));
		System.out.println(getNetNames("112.1.161.1"));
		System.out.println(getNetNames("112.0.143.0"));
		System.out.println(getNetNames("112.243.54.0"));
		System.out.println(getNetNames("182.34.47.0"));
		System.out.println(getNetNames("39.86.64.0"));
		System.out.println(getNetNames("219.137.62.0"));
		System.out.println(getNetNames("219.137.63.0"));
		System.out.println(getNetNames("116.23.92.0"));
		System.out.println(getNetNames("116.23.93.0"));
		System.out.println(getNetNames("116.23.94.0"));
		System.out.println(getNetNames("116.23.95.0"));
	}
}