package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import view.GraphicsPanel;
import view.SpriteSheet;


public abstract class TallObject {

	protected String name;
	protected Position pos = new Position(0, 0, 1, 1);
	protected int maxHp;
	protected int hp;
	
	public TallObject(String name, int hp) {
		this.name = name;
		this.maxHp = this.hp = hp;
	}
	
	public abstract BufferedImage getSprite();
	
	public void paint(Graphics2D g2) {
		AffineTransform savedTransorm = g2.getTransform();
		Position screenPos = GraphicsPanel.getScreenPos();
		// Convert to pixel space, accounting for units being tall objects
		g2.translate(GraphicsPanel.CELL_WIDTH * (pos.getX()-screenPos.getX()), 
				GraphicsPanel.TERRAIN_CELL_HEIGHT * 
				((pos.getY()-screenPos.getY()) - GraphicsPanel.TALL_OBJECT_CELL_HEIGHT_MULTIPLIER + 1));

		BufferedImage sprite = getSprite();
		if (sprite != null) {
			AffineTransform preImageTransorm = g2.getTransform();
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
	
	public void damage(int damage) {
		hp -= damage;
	}
		
	public String getName() {
		return name;
	}

	public Position getPos() {
		return pos;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public boolean isAlive() {
		return hp > 0;
	}

	public void setPos(int x, int y) {
		Position updatedPos = new Position(x, y, pos.getWidth(), pos.getHeight());
		if (this.equals(World.getTallObject(updatedPos))) {
			pos.setX(x);
			pos.setY(y);
		} else {
			World.moveObject(this, x, y);
		}
	}	
}
