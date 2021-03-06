package view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import controller.plot.Plot;
import model.StatusEffect;
import model.Structure;

public class SpriteSheet {
	public static enum FACING {N, W, S, E}
	
	public static enum ANIMATION {
		//TODO: add default: animation which will use weapon animation or... attack? if none specified
		WALK(0,true), CAST(4,true), MELEE(8,true), RANGE(12,true), DEATH(16,false), KNEEL(17,false),
		SPIN(18,true), POINT(22,true);
		private final int rowIndex; //used by spriteSheet to select an icon
		private final boolean directional;
		
		ANIMATION(int rowIndex, boolean directional) {
			this.rowIndex = rowIndex;
			this.directional = directional;
		}
		public int getRow(FACING facing) {
			return rowIndex + (directional?facing.ordinal():0);
		}
	};
	public static enum CLIMATE {PLAINS, JUNGLE, TUNDRA, FOREST, DESERT, ROCKY, VOLCANIC, CASTLE, UNDERGROUND}
	public static enum TERRAIN {ROAD, PATH, LIGHT, DENSE, WATER}
	public static enum ICON_ID {CLOSE, PLAY, PAUSE}
	
	public static final int SPRITE_HEIGHT = 64;
	public static final int TERRAIN_SPRITE_HEIGHT = 32;
	public static final int SPRITE_WIDTH = 64;
	public static final int ICON_HEIGHT = 16;
	public static final int ICON_WIDTH = 16;
	public static Map<URL, SpriteSheet> spriteSheets = new HashMap<URL, SpriteSheet>();
	
	private BufferedImage sheet;
	
	private SpriteSheet(URL url) {
		try {
			System.out.println("Loading SpriteSheet for " + url);
			sheet = GraphicsPanel.getDeviceCompatible(ImageIO.read(url));
		} catch (IOException e) {
			e.printStackTrace();
			sheet = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		}
	}
	
	public static void preloadSpriteSheets() {
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/terrain.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/objects.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/statusEffects.png"));
		
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/defender.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/sorceress.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/berserker.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/archer.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/guard.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/banditMale.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/goblinFemale.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/goblinMale.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/goblinChief.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/girl.png"));
		getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/boy.png"));
	}
	
	public static SpriteSheet getSpriteSheet(URL url) {
		SpriteSheet sheetFromMap = spriteSheets.get(url);
		if(sheetFromMap != null) {
			return sheetFromMap;
		} else {
			SpriteSheet sheet = new SpriteSheet(url);
			spriteSheets.put(url, sheet);
			return sheet;
		}
	}
	
	public BufferedImage getSprite(CLIMATE climate, TERRAIN terrain) {
		return sheet.getSubimage(
				terrain.ordinal() * SPRITE_WIDTH, 
				climate.ordinal() * TERRAIN_SPRITE_HEIGHT,
				SPRITE_WIDTH,
				TERRAIN_SPRITE_HEIGHT);
	}
	
	public BufferedImage getSprite(ANIMATION animation, FACING facing, int index) {
		return sheet.getSubimage(
				index * SPRITE_WIDTH,
				animation.getRow(facing) * SPRITE_HEIGHT,
				SPRITE_WIDTH,
				SPRITE_HEIGHT);
	}
	
	public BufferedImage getSprite(StatusEffect.ID statusEffect) {
		return sheet.getSubimage(
				statusEffect.getIndex() % 4 * ICON_WIDTH,
				((int)(statusEffect.getIndex() / 4)) * ICON_HEIGHT,
				ICON_WIDTH,
				ICON_HEIGHT);
	}

	public BufferedImage getSprite(Structure.ID id, CLIMATE climate) {
		int index = id.getIndex(climate);
		return sheet.getSubimage(index % CLIMATE.values().length * SPRITE_WIDTH,
				index / CLIMATE.values().length * SPRITE_HEIGHT,
				SPRITE_WIDTH, SPRITE_HEIGHT);
	}
	
	public BufferedImage getSprite(ICON_ID id) {
		return sheet.getSubimage(id.ordinal() * ICON_WIDTH, 0,
				ICON_WIDTH, ICON_HEIGHT);
	}
}
