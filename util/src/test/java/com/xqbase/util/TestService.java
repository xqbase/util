package com.xqbase.util;

public class TestService {
	private static Service service = new Service();

	public static void main(String[] args) {
		if (!service.startup(args)) {
			return;
		}
		Log.i("TestService Started");
		while (!service.isInterrupted()) {
			Time.sleep(1000);
			System.out.print('.');
		}
		Log.i("TestService Stopped");
		service.shutdown();
	}
}