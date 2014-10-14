package com.xqbase.util.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class Captcha {
	private static final int HEIGHT = 20;

	private static Font font = new Font("Arial", Font.BOLD, 15);
	private static SecureRandom random = new SecureRandom();

	public static BufferedImage getImage(String captcha) {
		int width = captcha.length() * 10 + 10;
		BufferedImage image = new BufferedImage(width, HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setFont(font);
		g.setColor(new Color(200, 200, 200));
		g.fillRect(0, 0, width - 1, HEIGHT - 1);
		for (int i = 0; i < 100; i ++) {
			g.setColor(new Color(200 + random.nextInt(56),
					200 + random.nextInt(56), 200 + random.nextInt(56)));
			g.drawString("*", random.nextInt(width), random.nextInt(HEIGHT) + 10);
		}
		for (int i = 0; i < captcha.length(); i ++) {
			g.setColor(new Color(random.nextInt(100), random.nextInt(150), random.nextInt(200)));
			g.drawString("" + captcha.charAt(i), i * width / captcha.length() + 2,
					random.nextInt(HEIGHT) / 10 + 15);
		}
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width - 1, HEIGHT - 1);
		return image;
	}
}