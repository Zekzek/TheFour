package controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import model.Position;
import view.GraphicsPanel;
import view.SpriteSheet;
import view.SpriteSheet.CLIMATE;
import view.SpriteSheet.TERRAIN;
import controller.plot.Plot;

public class MapBuilder {
	private static enum BYTE_TO_PIXEL {A, B, G, R};
	private static final int BYTES_PER_PIXEL = BYTE_TO_PIXEL.values().length;
	private static final int R = BYTE_TO_PIXEL.R.ordinal();
	private static final int G = BYTE_TO_PIXEL.G.ordinal();
	private static final int B = BYTE_TO_PIXEL.B.ordinal();
	
	private static final SpriteSheet TERRAIN_SHEET = 
			SpriteSheet.getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/terrain.png"));
	private static final BufferedImage TEMPLATE =
			GraphicsPanel.loadImage(Plot.class.getResource("/resource/img/templates/world.png"));
	private static final int TEMPLATE_WIDTH = TEMPLATE.getWidth();
	private static final byte[] TEMPLATE_BYTES = ((DataBufferByte)TEMPLATE.getRaster().getDataBuffer()).getData();
	
	private static CLIMATE myClimate = CLIMATE.PLAINS;
	
	public static BufferedImage[][] getTiles(Position pos) {
		BufferedImage[][] tiles = new BufferedImage[pos.getWidth()][pos.getHeight()];
		
		for (int x = 0; x < pos.getWidth(); x++) {
			for (int y = 0; y < pos.getWidth(); y++) {
				int pixelIndex = (pos.getY() + y) * TEMPLATE_WIDTH + pos.getX() + x;
				int byteIndex = pixelIndex * BYTES_PER_PIXEL;
				tiles[x][y] = lookupTerrainCell(
					TEMPLATE_BYTES[byteIndex+R], 
					TEMPLATE_BYTES[byteIndex+G], 
					TEMPLATE_BYTES[byteIndex+B]);
			}
		}
		return tiles;
	}
	
	private static BufferedImage lookupTerrainCell(byte red, byte green, byte blue) {
		//NOTE: need to open changed images within eclipse before running in order to see updates
		BufferedImage terrain = null;
		int r = red & 0xff;
		int g = green & 0xff;
		int b = blue & 0xff;
		int hi = 240;
		int low = 15;
		
		if (r > hi && g > hi && b> hi) { //near white
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.ROAD);
		} else if (r < low && g < low && b < low){ //near black
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.PATH);
		} else if (b > r && b > g) { //mostly blue
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.WATER);
		} else if (g > r && g > b) { //mostly green
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.DENSE);
		} else if (r > g && r > b) { //mostly red
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.LIGHT);
		} else { //gray
			terrain = TERRAIN_SHEET.getSprite(myClimate, TERRAIN.LIGHT);
		}
		return terrain;
	}
	
	public static void setClimate(CLIMATE climate) {
		myClimate = climate;
	}
}
