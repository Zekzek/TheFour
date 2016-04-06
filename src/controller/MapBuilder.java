package controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import model.GridPosition;
import model.GridRectangle;
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
	
	public static BufferedImage[][] getTiles(GridRectangle rect) {
		BufferedImage[][] tiles = new BufferedImage[rect.getWidth()][rect.getHeight()];
		
		for (int x = 0; x < rect.getWidth(); x++) {
			for (int y = 0; y < rect.getWidth(); y++) {
				tiles[x][y] = TERRAIN_SHEET.getSprite(myClimate, getTerrainType(rect.getX() + x, rect.getY() + y));
			}
		}
		return tiles;
	}
	
	public static BufferedImage[][] getTiles(GridRectangle screenPos, int border) {
		GridRectangle borderedRect = new GridRectangle(screenPos.getX() - border, screenPos.getY() - border, 
				screenPos.getWidth() + 2*border, screenPos.getHeight() + 2*border);
		return getTiles(borderedRect);
	}
	
	public static void setClimate(CLIMATE climate) {
		myClimate = climate;
	}

	public static TERRAIN getTerrainType(GridPosition pos) {
		return getTerrainType(pos.getX(), pos.getY());
	}
	
	public static TERRAIN getTerrainType(int x, int y) {
		int pixelIndex = y * TEMPLATE_WIDTH + x;
		int byteIndex = pixelIndex * BYTES_PER_PIXEL;
		TERRAIN terrain;
		try {
			terrain = lookupTerrainCellType(
				TEMPLATE_BYTES[byteIndex+R], 
				TEMPLATE_BYTES[byteIndex+G], 
				TEMPLATE_BYTES[byteIndex+B]);
		} catch (ArrayIndexOutOfBoundsException e) {
			terrain = TERRAIN.WATER;
		}
		
		return terrain;
	}
	
	private static TERRAIN lookupTerrainCellType(byte red, byte green, byte blue) {
		TERRAIN terrain;
		int r = red & 0xff;
		int g = green & 0xff;
		int b = blue & 0xff;
		int hi = 240;
		int low = 15;
		
		if (r > hi && g > hi && b> hi) { //near white
			terrain = TERRAIN.ROAD;
		} else if (r < low && g < low && b < low){ //near black
			terrain = TERRAIN.PATH;
		} else if (b > r && b > g) { //mostly blue
			terrain = TERRAIN.WATER;
		} else if (g > r && g > b) { //mostly green
			terrain = TERRAIN.DENSE;
		} else if (r > g && r > b) { //mostly red
			terrain = TERRAIN.LIGHT;
		} else { //gray
			terrain = TERRAIN.LIGHT;
		}
		return terrain;
	}
}
