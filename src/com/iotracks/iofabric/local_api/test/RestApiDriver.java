package com.iotracks.iofabric.local_api.test;

import java.util.Random;

public class RestApiDriver {
	public static void main(String[] args) throws Exception {
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			RestPublishTest pub = new RestPublishTest("DTCnTG4dLyrGC7XYrzzTqNhW7R78hk3V", 1500, 50);
			pub.start();
			Thread.sleep(random.nextInt(250));
			RestReceiveTest rec = new RestReceiveTest("receiver_" + String.valueOf(i + 1), 50);
			rec.start();
		}
		System.out.println();
	}
}
