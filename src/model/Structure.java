package model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Structure extends TallObject{

	//TODO: use spritesheet
	private BufferedImage sprite;
	
	public Structure(String name, URL url, int hp) {
		super(name, hp);
		try {
			if (url != null) {
				sprite = ImageIO.read(url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public BufferedImage getSprite() {
		return sprite;
	}
}
