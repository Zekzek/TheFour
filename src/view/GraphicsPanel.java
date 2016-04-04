package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.GridRectangle;
import model.TallObject;
import model.World;
import controller.BattleQueue;
import controller.MapBuilder;

public class GraphicsPanel extends JPanel{
	public enum AMBIENT_LIGHT {DAY, NIGHT, DUSK}
	private static final long serialVersionUID = -4779442729968562068L;
	
	public static final int CELL_WIDTH = 64;
	public static final int CELL_HEIGHT = 64;
	public static final int TERRAIN_CELL_HEIGHT = 32;
	public static final double TALL_OBJECT_CELL_HEIGHT_MULTIPLIER = 2.0;
	public static final int TALL_OBJECT_CELL_HEIGHT = (int) (TERRAIN_CELL_HEIGHT * TALL_OBJECT_CELL_HEIGHT_MULTIPLIER);
	
	public static final Map<AMBIENT_LIGHT, Color> CLOSE_COLORS = new HashMap<AMBIENT_LIGHT, Color>();
	public static final Map<AMBIENT_LIGHT, Color> FAR_COLORS = new HashMap<AMBIENT_LIGHT, Color>();
	public static final Color CLOSE_COLOR = new Color(255,255,245,0);
	public static final Color FAR_COLOR = new Color(0,0,10,110);
	public static final int REFRESH_RATE = 100;
	
	private static Color fade = new Color(0,0,0,0);
	private static Font fadeScreenFont = new Font ("MV Boli", Font.BOLD , 24);
	private static String fadeScreenText;
	
	private static final Thread repaint = new Thread() {
		@Override
		public void run() {
			while(true) {
				try {
					GameFrame.repaintAll();
					Thread.sleep(REFRESH_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private static GridRectangle screenPos = new GridRectangle(0, 0, 16, 16);
	private static AMBIENT_LIGHT ambientLight = AMBIENT_LIGHT.DAY;
	
	public void startPainting() {
		if (CLOSE_COLORS.isEmpty()) {
			CLOSE_COLORS.put(AMBIENT_LIGHT.DAY, new Color(255,255,245,0));
			FAR_COLORS.put(AMBIENT_LIGHT.DAY, new Color(0,0,10,110));
			CLOSE_COLORS.put(AMBIENT_LIGHT.DUSK, new Color(100,100,90,60));
			FAR_COLORS.put(AMBIENT_LIGHT.DUSK, new Color(0,0,10,130));
			CLOSE_COLORS.put(AMBIENT_LIGHT.NIGHT, new Color(10,10,0,130));
			FAR_COLORS.put(AMBIENT_LIGHT.NIGHT, new Color(0,0,10,180));
		}
		if (!repaint.isAlive()) {
			repaint.setDaemon(true);
			repaint.start();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		double worldPixelWidth = screenPos.getWidth() * CELL_WIDTH;
		double worldPixelHeight = screenPos.getHeight() * TERRAIN_CELL_HEIGHT;
		
		double widthScale = getWidth() / worldPixelWidth;
		double heightScale = getHeight() / worldPixelHeight;
		
		double scale = widthScale < heightScale ? widthScale : heightScale;
		
		// Convert to world space
		g2.scale(scale, scale);
		
		// Draw terrain
		BufferedImage[][] terrainTiles = MapBuilder.getTiles(screenPos);
		for (int x = 0; x < terrainTiles.length; x++) {
			for (int y = 0; y < terrainTiles[x].length; y++) {
				g2.drawImage(terrainTiles[x][y], x * CELL_WIDTH, y * TERRAIN_CELL_HEIGHT, null);
			}
		}
		
		// Draw the objects
		ArrayList<TallObject> contents = World.getSortedContentsWithin(screenPos, TallObject.class);
		for (TallObject tallObject : contents) {
			tallObject.paint(g2);
		}
		
		// Draw the shadow gradient
	    GradientPaint nearToFar = new GradientPaint(screenPos.getX(), screenPos.getY() + screenPos.getHeight()  * TERRAIN_CELL_HEIGHT,
	    		CLOSE_COLORS.get(ambientLight), screenPos.getX(), screenPos.getY(), FAR_COLORS.get(ambientLight));
	    g2.setPaint(nearToFar);
	    g2.fillRect(0, 0, screenPos.getWidth() * CELL_WIDTH, 
	    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT);
	    g2.setPaint(fade);
	    g2.fillRect(0, 0, screenPos.getWidth() * CELL_WIDTH, 
	    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT);
	    if (fadeScreenText != null) {
	    	drawCenteredText(g2, fadeScreenText, screenPos.getWidth() * CELL_WIDTH / 2, 
		    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT / 2, fadeScreenFont, Color.WHITE, null);
	    }
	}
	
	public static void drawCenteredText(Graphics2D g, String text, int x, int y, Font font, Color fontColor, Color outlineColor) {
	    FontMetrics metrics = g.getFontMetrics(font);
	    x -= metrics.stringWidth(text) / 2;
	    y -= metrics.getHeight() / 2;
	    g.setFont(font);
	    g.setColor(fontColor);
	    g.drawString(text, x, y);
	    if (outlineColor != null) {
	    	TextLayout textLayout = new TextLayout(text, font, g.getFontRenderContext());
	    	Shape outline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
		    g.setColor(outlineColor);
		    g.draw(outline);
	    }
	}
	
	public static void changeScene(SceneTransition sorcRequest) {
		changeScene(sorcRequest.getFadeInDuration(), 
				sorcRequest.getFadedText(), sorcRequest.getFadedDuration(), sorcRequest.getSetupRunnable(), 
				sorcRequest.getFadeOutDuration(), sorcRequest.getStartRunnable());
	}
	
	public static void changeScene(int fadeOutDuration, String display, int displayDuration, Runnable runnable, 
			int fadeInDuration, Runnable startScene) {
		Thread prettyLoadScene = new Thread() {
			@Override public void run() {
				// pause
				BattleQueue.setPause(true);
				GameFrame.disableMenu();
				
				// fade to black
				for (int i = 0; i < fadeOutDuration; i += REFRESH_RATE) {
					fade = new Color(0, 0, 0, 255 * i / fadeOutDuration);
					try {
						Thread.sleep(REFRESH_RATE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				fade = new Color(0, 0, 0, 255);				
				
				// load the scene
				fadeScreenText = display;
				runnable.run();
				
				// wait
				try {
					Thread.sleep(displayDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				
				// fade in over 'after' ms
				fadeScreenText = null;
				for (int i = fadeInDuration; i > 0; i -= REFRESH_RATE) {
					fade = new Color(0, 0, 0, 255 * i / fadeInDuration);
					try {
						Thread.sleep(REFRESH_RATE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				fade = new Color(0, 0, 0, 0);
				
				// resume
				GameFrame.enableMenu();
				BattleQueue.setPause(false);
				
				startScene.run();
			}
		};
		prettyLoadScene.start();
	}
	
	public static BufferedImage getDeviceCompatible(BufferedImage image) {
	    BufferedImage formattedImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
	    		.getDefaultConfiguration().createCompatibleImage( image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
	    Graphics2D g2 = formattedImage.createGraphics();
	    g2.drawRenderedImage(image, null);
	    g2.dispose();
	    return formattedImage;
	}
	
	public static BufferedImage loadImage(URL url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return img;
	}
	
	public static void moveScreenTo(int x, int y) {
		screenPos.setX(x);
		screenPos.setY(y);
	}
	
	public static GridRectangle getScreenRectangle() {
		return screenPos;
	}
	
	public static AMBIENT_LIGHT getAmbientLight() {
		return ambientLight;
	}

	public static void setAmbientLight(AMBIENT_LIGHT ambientLight) {
		GraphicsPanel.ambientLight = ambientLight;
	}
}
