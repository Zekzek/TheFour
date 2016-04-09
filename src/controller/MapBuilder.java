package controller;

import java.awt.Color;
import java.awt.image.BufferedImage;

import model.GridPosition;
import model.GridRectangle;
import view.SpriteSheet;
import view.SpriteSheet.CLIMATE;
import view.SpriteSheet.TERRAIN;
import controller.plot.Plot;

public class MapBuilder {
	private static final SpriteSheet TERRAIN_SHEET = 
			SpriteSheet.getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/terrain.png"));
	private static final TemplateReader WORLD_TEMPLATE = new TemplateReader("/resource/img/templates/world.png");
	private static final TemplateReader CLIMATE_TEMPLATE = new TemplateReader("/resource/img/templates/climate.png");
	
	public static BufferedImage[][] getTiles(GridRectangle rect) {
		BufferedImage[][] tiles = new BufferedImage[rect.getWidth()][rect.getHeight()];
		
		for (int x = 0; x < rect.getWidth(); x++) {
			for (int y = 0; y < rect.getHeight(); y++) {
				tiles[x][y] = TERRAIN_SHEET.getSprite(getClimateType(rect.getX() + x, rect.getY() + y),
						getTerrainType(rect.getX() + x, rect.getY() + y));
			}
		}
		return tiles;
	}
	
	public static BufferedImage[][] getTiles(GridRectangle screenPos, int border) {
		GridRectangle borderedRect = new GridRectangle(screenPos.getX() - border, screenPos.getY() - border, 
				screenPos.getWidth() + 2*border, screenPos.getHeight() + 2*border);
		return getTiles(borderedRect);
	}
	
	public static TERRAIN getTerrainType(GridPosition pos) {
		return getTerrainType(pos.getX(), pos.getY());
	}
	
	public static TERRAIN getTerrainType(int x, int y) {
		TERRAIN terrain;
		Color color = WORLD_TEMPLATE.getColorAt(x, y);
		if (color == null) {
			return TERRAIN.WATER;
		} else {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
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
	
	public static CLIMATE getClimateType(int x, int y) {
		Color color = CLIMATE_TEMPLATE.getColorAt(x, y);
		if (color == null) {
			return CLIMATE.PLAINS;
		} else {
			boolean reddish = color.getRed() > 127;
			boolean greenish = color.getGreen() > 127;
			boolean blueish = color.getBlue() > 127;
			boolean visible = color.getAlpha() > 127;
			CLIMATE climate;
		
			if (!visible) { //invisible
				climate = CLIMATE.PLAINS;
			} else if (reddish) {
				if (greenish) {
					if (blueish) { //white
						climate = CLIMATE.TUNDRA;
					} else { //yellow
						climate = CLIMATE.DESERT;
					}
				} else if (blueish) { //magenta
					climate = CLIMATE.JUNGLE;
				} else { //red
					climate = CLIMATE.VOLCANIC;
				}
			} else if (greenish) {
				if (blueish) { //teal
					climate = CLIMATE.ROCKY;
				} else { //green
					climate = CLIMATE.FOREST;
				}
			} else if (blueish) { //blue
				climate = CLIMATE.CASTLE;
			} else { //black
				climate = CLIMATE.UNDERGROUND;
			}
			return climate;
		}
	}
}
