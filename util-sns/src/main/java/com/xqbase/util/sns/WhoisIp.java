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
					if (line.toLowerCase().startsWith("netname:")) {
						infos.add(line.substring(8).trim());
					}
				}
			}
		} catch (IOException e) {
			Log.w("Error Parsing " + ip + ": " + e.getMessage());
		}
		return infos;
	}
}