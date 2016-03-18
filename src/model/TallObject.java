package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import view.GraphicsPanel;
import view.SpriteSheet;


public abstract class TallObject implements ITargetable {

	private static int nextId = 0;
	private final int id;
	protected String name;
	protected GridPosition pos = new GridPosition(0, 0);
	protected int maxHp;
	protected int hp;
	protected double drawXOffset = 0.0;
	protected double drawYOffset = 0.0;
	protected Modifier baseModifier;
	
	//Original
	public TallObject(String name, int hp) {
		this.id = nextId;
		nextId++;
		this.name = name;
		this.maxHp = this.hp = hp;
		this.baseModifier = new Modifier();
	}
	
	//Clone
	public TallObject(TallObject otherObject) {
		this.id = nextId;
		nextId++;
		this.name = otherObject.name;
		this.maxHp = otherObject.maxHp;
		this.hp = otherObject.hp;
		this.baseModifier = new Modifier(otherObject.baseModifier);
		//Since pos should always be unique, don't set it in the copy constructor
	}

	public abstract BufferedImage getSprite();	
	
	public void damage(int damage) {
		hp -= damage;
	}
	
	public boolean isAlive() {
		return hp > 0;
	}

	public void paint(Graphics2D g2) {
		AffineTransform savedTransorm = g2.getTransform();
		GridRectangle screenRectangle = GraphicsPanel.getScreenRectangle();
		// Convert to pixel space, accounting for units being tall objects
		g2.translate(GraphicsPanel.CELL_WIDTH * (pos.getX()-screenRectangle.getX()), 
				GraphicsPanel.TERRAIN_CELL_HEIGHT * 
				((pos.getY()-screenRectangle.getY()) - GraphicsPanel.TALL_OBJECT_CELL_HEIGHT_MULTIPLIER + 1));

		BufferedImage sprite = getSprite();
		if (sprite != null) {
			AffineTransform preImageTransorm = g2.getTransform();
			g2.translate(drawXOffset * GraphicsPanel.CELL_WIDTH, drawYOffset * GraphicsPanel.TERRAIN_CELL_HEIGHT);
			// Convert to image space
			g2.scale(((double)GraphicsPanel.CELL_WIDTH) / SpriteSheet.SPRITE_WIDTH, 
					((double)GraphicsPanel.TALL_OBJECT_CELL_HEIGHT) / SpriteSheet.SPRITE_HEIGHT);
			g2.drawImage(sprite, 0, 0, null);
			// Return to pixel space
			g2.setTransform(preImageTransorm);
		}

		// Draw the HP bar
		if (hp < maxHp) {
			g2.setColor(Color.GREEN);
			g2.fillRect(0, pos.getHeight() * GraphicsPanel.TALL_OBJECT_CELL_HEIGHT, pos.getWidth() * GraphicsPanel.CELL_WIDTH * hp / maxHp, 3);
			g2.setColor(Color.BLACK);
			g2.drawRect(0, pos.getHeight() * GraphicsPanel.TALL_OBJECT_CELL_HEIGHT, pos.getWidth() * GraphicsPanel.CELL_WIDTH, 3);
		}
		
		g2.setTransform(savedTransorm);
	}

	public void updateeWorldPos(int x, int y) {
		GridPosition updatedPos = new GridPosition(x, y);
		if (this.equals(World.getTallObject(updatedPos))) {
			pos.setX(x);
			pos.setY(y);
		} else {
			World.moveObject(this, x, y);
		}
	}
	
	@Override
	public String toString() {
		return name + " #" + id;
	}

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public GridPosition getPos() {
		return pos;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getHp() {
		return hp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TallObject other = (TallObject) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public Modifier getModifier() {
		return baseModifier;
	}
}
