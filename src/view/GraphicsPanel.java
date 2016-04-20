package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.GridPosition;
import model.GridRectangle;
import model.GroundTarget;
import model.TallObject;
import model.Unit;
import model.World;
import controller.BattleQueue;
import controller.MapBuilder;

public class GraphicsPanel extends JPanel implements MouseMotionListener, MouseListener {
	public enum AMBIENT_LIGHT {DAY, NIGHT, DUSK}
	private static final long serialVersionUID = -4779442729968562068L;
	
	public static final int CELL_WIDTH = 64;
	public static final int CELL_HEIGHT = 64;
	public static final int TERRAIN_CELL_HEIGHT = 32;
	public static final double TALL_OBJECT_CELL_HEIGHT_MULTIPLIER = 2.0;
	public static final int TALL_OBJECT_CELL_HEIGHT = (int) (TERRAIN_CELL_HEIGHT * TALL_OBJECT_CELL_HEIGHT_MULTIPLIER);
	public static final int REFRESH_RATE = 50;
	
	private static final Color CLOSE_COLOR = new Color(255,255,245,0);
	private static final Color FAR_COLOR = new Color(0,0,10,110);
	private static final Map<AMBIENT_LIGHT, Color> LIGHT_COLORS = new HashMap<AMBIENT_LIGHT, Color>();
	private static final Set<GroundTarget> groundTargets = new HashSet<GroundTarget>();
	private static final Set<IGridClickedListener> gridClickedListeners = new HashSet<IGridClickedListener>();
	private static final Font fadeScreenFont = new Font ("MV Boli", Font.BOLD , 20);
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

	private static Unit focusUnit = null;
	private static Color fade = new Color(0,0,0,0);
	private static String fadeScreenText;
	private static GridRectangle screenPos = new GridRectangle(0, 0, 24, 22);
	private static AMBIENT_LIGHT ambientLight = AMBIENT_LIGHT.DAY;
	private static GridPosition hoverPosition;
	private static boolean initialized;
	private static GridPosition moveScreenStartPosition;
	private static volatile double moveCompletePercentage;
	
	private void initialize() {
		initialized = true;
		LIGHT_COLORS.put(AMBIENT_LIGHT.DAY, new Color(255,255,250,10));
		LIGHT_COLORS.put(AMBIENT_LIGHT.DUSK, new Color(80,80,75,80));
		LIGHT_COLORS.put(AMBIENT_LIGHT.NIGHT, new Color(20,20,20,140));
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void startPainting() {
		if (!initialized) 
			initialize();

		if (!repaint.isAlive()) {
			repaint.setDaemon(true);
			repaint.start();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform savedTransorm = g2.getTransform();
		
		// Convert to world space
		double scale = getScale();
		int drawnHeight = (int) (screenPos.getHeight() * TERRAIN_CELL_HEIGHT * scale);
	    int drawnWidth = (int) (screenPos.getWidth() * CELL_WIDTH * scale);
	    int horizontalPadding = (getWidth() - drawnWidth)/2;
	    int verticalPadding = (getHeight() - drawnHeight)/2;
	    if (horizontalPadding < 0)
	    	horizontalPadding = 0;
	    if (verticalPadding < 0)
	    	verticalPadding = 0;
	    
	    g2.translate(horizontalPadding, verticalPadding);
	    g2.scale(scale, scale);
		
		if (focusUnit != null) {
			if (moveScreenStartPosition != null) {
				int startX = moveScreenStartPosition.getX();
				int startY = moveScreenStartPosition.getY();
				int endX = focusUnit.getPos().getX() - screenPos.getWidth()/2;
				int endY = focusUnit.getPos().getY() - screenPos.getHeight()/2;
				double posX = startX + moveCompletePercentage * (endX - startX);
				double posY = startY + moveCompletePercentage * (endY - startY);
				screenPos.setX((int) posX);
				screenPos.setY((int) posY);
				g2.translate(((int)posX - posX) * GraphicsPanel.CELL_WIDTH,
						((int)posY - posY) * GraphicsPanel.TERRAIN_CELL_HEIGHT);
			}
			else {
				GridPosition focusPos = focusUnit.getPos();
				screenPos.setX(focusPos.getX() - screenPos.getWidth()/2);
				screenPos.setY(focusPos.getY() - screenPos.getHeight()/2);
				g2.translate(-focusUnit.getDrawXOffset() * GraphicsPanel.CELL_WIDTH,
						-focusUnit.getDrawYOffset() * GraphicsPanel.TERRAIN_CELL_HEIGHT);
			}
		}
		
		// Draw terrain
		BufferedImage[][] terrainTiles = MapBuilder.getTiles(screenPos, 1);
		for (int x = 0; x < terrainTiles.length; x++) {
			for (int y = 0; y < terrainTiles[x].length; y++) {
				g2.drawImage(terrainTiles[x][y], (x-1) * CELL_WIDTH, (y-1) * TERRAIN_CELL_HEIGHT, null);
			}
		}
		
		// Draw active ground targets
		for (GroundTarget target : groundTargets) {
			target.paint(g2);
		}
		
		// Draw the objects
		GridRectangle visibleScreen = new GridRectangle(screenPos.getX(), screenPos.getY(), screenPos.getWidth(), screenPos.getHeight() + 1);
		ArrayList<TallObject> contents = World.getSortedContentsWithin(visibleScreen, TallObject.class);
		Area darkArea = new Area(new Rectangle(0, 0, screenPos.getWidth() * CELL_WIDTH, 
	    		screenPos.getHeight() * TERRAIN_CELL_HEIGHT));
	    for (TallObject tallObject : contents) {
			tallObject.paint(g2);
			if (tallObject instanceof Unit) {
				darkArea.subtract(new Area(new Ellipse2D.Double(
						(tallObject.getPos().getX() + tallObject.getDrawXOffset() - screenPos.getX() - 1) * CELL_WIDTH, 
						(tallObject.getPos().getY() + tallObject.getDrawYOffset() - screenPos.getY() - 1) * TERRAIN_CELL_HEIGHT, 
						CELL_WIDTH * 3, TERRAIN_CELL_HEIGHT * 3)));
			}
		}
		
	    //Draw the shadow gradient
	    g2.setTransform(savedTransorm);
		g2.setPaint(LIGHT_COLORS.get(ambientLight));
	    g2.fill(darkArea);
	    GradientPaint nearToFar = new GradientPaint(0, drawnHeight, CLOSE_COLOR, 0, 0, FAR_COLOR);
	    g2.setPaint(nearToFar);
	    g2.fillRect(horizontalPadding, verticalPadding, drawnWidth, drawnHeight);
	    
	    //Draw fade to black
	    g2.setPaint(fade);
	    g2.fillRect(horizontalPadding, verticalPadding, drawnWidth, drawnHeight);
	    if (fadeScreenText != null) {
	    	drawCenteredText(g2, fadeScreenText, getWidth() / 2, getHeight() / 2, fadeScreenFont, Color.WHITE, null);
	    }
	    
	    //Black out edges
	    g2.setColor(Color.BLACK);
	    g2.fillRect(0, 0, getWidth(), verticalPadding);
	    g2.fillRect(0, drawnHeight + verticalPadding, getWidth(), verticalPadding);
	    g2.fillRect(0, 0, horizontalPadding, getHeight());
	    g2.fillRect(drawnWidth + horizontalPadding, 0, horizontalPadding, getHeight());
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
	
	public static void changeScene(SceneTransition transition) {
		changeScene(transition.getFadeInDuration(), 
				transition.getFadedText(), transition.getFadedDuration(), transition.getSetupRunnable(), 
				transition.getFadeOutDuration(), transition.getStartRunnable());
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
				if (runnable != null)
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
				
				if (startScene != null)
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
		focusUnit = null;
		moveScreenStartPosition = null;
		screenPos.setX(x);
		screenPos.setY(y);
	}
	
	public static void moveScreenTo(Unit unit, int duration) {
		if (focusUnit == null || !focusUnit.equals(unit)) {
			focusUnit = unit;
			moveScreenStartPosition = new GridPosition(screenPos.getX(), screenPos.getY());
			Thread moveCameraThread = new Thread() {
				@Override
				public void run() {
					moveCompletePercentage = 0.0;
					for (int i = REFRESH_RATE; i < duration; i += REFRESH_RATE) {
						try {
							Thread.sleep(REFRESH_RATE);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						moveCompletePercentage = i / (double)duration;
					}
					moveScreenStartPosition = null;
				}
			};
			moveCameraThread.start();
		}
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
	
	public static void clearTargets() {
		groundTargets.clear();
	}

	public static void addGroundTarget(GroundTarget target) {
		groundTargets.add(target);
	}
	
	private double getScale() {
		double worldPixelWidth = screenPos.getWidth() * CELL_WIDTH;
		double worldPixelHeight = screenPos.getHeight() * TERRAIN_CELL_HEIGHT;
		
		double widthScale = getWidth() / worldPixelWidth;
		double heightScale = getHeight() / worldPixelHeight;
		
		return widthScale < heightScale ? widthScale : heightScale;
	}
	
	public static void addGridClickedListener(IGridClickedListener listener) {
		gridClickedListeners.add(listener);
	}
	
	public static boolean isHoverGrid(GridPosition pos) {
		return pos.equals(hoverPosition);
	}

	private void setHoverPosition(MouseEvent e) {
		double scale = getScale();
		int drawnHeight = (int) (screenPos.getHeight() * TERRAIN_CELL_HEIGHT * scale);
	    int drawnWidth = (int) (screenPos.getWidth() * CELL_WIDTH * scale);
	    int horizontalPadding = (getWidth() - drawnWidth)/2;
	    int verticalPadding = (getHeight() - drawnHeight)/2;
	    if (horizontalPadding < 0)
	    	horizontalPadding = 0;
	    if (verticalPadding < 0)
	    	verticalPadding = 0;
		hoverPosition = new GridPosition(screenPos.getX() + (int)((e.getX() - horizontalPadding) / scale / CELL_WIDTH), 
				screenPos.getY() + (int)((e.getY() - verticalPadding) / scale / TERRAIN_CELL_HEIGHT));
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		setHoverPosition(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		setHoverPosition(e);
		for (IGridClickedListener listener : gridClickedListeners) {
			listener.reportGridClicked(hoverPosition);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
