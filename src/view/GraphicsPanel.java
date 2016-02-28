package view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.Position;
import model.TallObject;
import model.World;
import controller.BattleQueue;
import controller.MapBuilder;

public class GraphicsPanel extends JPanel{
	private static final long serialVersionUID = -4779442729968562068L;
	
	public static final int CELL_WIDTH = 64;
	public static final int CELL_HEIGHT = 64;
	public static final int TERRAIN_CELL_HEIGHT = 32;
	public static final double TALL_OBJECT_CELL_HEIGHT_MULTIPLIER = 2.0;
	public static final int TALL_OBJECT_CELL_HEIGHT = (int) (TERRAIN_CELL_HEIGHT * TALL_OBJECT_CELL_HEIGHT_MULTIPLIER);
	
	public static final Color CLOSE_COLOR = new Color(255,255,245,50);
	public static final Color FAR_COLOR = new Color(0,0,10,110);
	public static final int REFRESH_RATE = 100;
	private static Color fade = new Color(0,0,0,0);
	
	private static final Thread repaint = new Thread() {
		@Override
		public void run() {
			while(true) {
				try {
					me.repaint();
					Thread.sleep(REFRESH_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private static GraphicsPanel me;
	private static Position screenPos = new Position(0, 0, 16, 16);

	public GraphicsPanel() {
		me = this;
	}
	
	public void startPainting() {
		if (!repaint.isAlive()) {
			repaint.setDaemon(true);
			repaint.start();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		// Convert to world space
		g2.scale(getWidth() / (double)(screenPos.getWidth() * CELL_WIDTH),
				getHeight() / (double)(screenPos.getHeight() * TERRAIN_CELL_HEIGHT));
		
		// Draw terrain
		BufferedImage[][] terrainTiles = MapBuilder.getTiles(screenPos);
		for (int x = 0; x < terrainTiles.length; x++) {
			for (int y = 0; y < terrainTiles[x].length; y++) {
				g2.drawImage(terrainTiles[x][y], x * CELL_WIDTH, y * TERRAIN_CELL_HEIGHT, null);
			}
		}
		
		// Draw the objects
		g2.translate(0, -GraphicsPanel.TERRAIN_CELL_HEIGHT / 4);
		ArrayList<TallObject> contents = World.getSortedContentsWithin(screenPos, TallObject.class);
		for (TallObject tallObject : contents) {
			tallObject.paint(g2);
		}
		g2.translate(0, GraphicsPanel.TERRAIN_CELL_HEIGHT / 4);
		
		// Draw the shadow gradient
	    GradientPaint nearToFar = new GradientPaint(screenPos.getX(), screenPos.getY() + screenPos.getHeight()  * TERRAIN_CELL_HEIGHT,
	    		CLOSE_COLOR, screenPos.getX(), screenPos.getY(), FAR_COLOR);
	    g2.setPaint(nearToFar);
	    g2.fillRect(0, 0, screenPos.getWidth() * CELL_WIDTH, 
	    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT);
	    g2.setPaint(fade);
	    g2.fillRect(0, 0, screenPos.getWidth() * CELL_WIDTH, 
	    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT);
	}
	
	public static void changeScene(int fadeOutDuration, String display, int displayDuration, Runnable runnable, 
			int fadeInDuration, Runnable startScene) {
		Thread prettyLoadScene = new Thread() {
			@Override public void run() {
				// pause
				BattleQueue.pauseBattle();
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
				
				//TODO: display 'display'
				
				// load the scene
				runnable.run();
				
				// wait
				try {
					Thread.sleep(displayDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				
				//TODO: remove 'display'
				
				// fade in over 'after' ms
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
				BattleQueue.resumeBattle();
				
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
	
	public static Position getScreenPos() {
		return screenPos;
	}
}
