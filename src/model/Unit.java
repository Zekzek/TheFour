package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import view.GraphicsPanel;
import view.SpriteSheet;
import view.SpriteSheet.ANIMATION;
import view.SpriteSheet.FACING;
import controller.BattleQueue;

public class Unit extends TallObject {
	public static enum TEAM { PLAYER, ALLY, NONCOMBATANT, ENEMY1, ENEMY2 }
	private static final int MINI_SIZE = 32;
	private static final int ANIMATION_LENGTH = 5;
	
	private TEAM team;
	private SpriteSheet sheet;
	//TODO: only contain the image
	private JLabel nameLabel;
	
	private SpriteSheet.FACING facing = SpriteSheet.FACING.S;
	private SpriteSheet.ANIMATION defaultStance = SpriteSheet.ANIMATION.WALK;
	private SpriteSheet.ANIMATION stance = SpriteSheet.ANIMATION.WALK;
	private int animationSequence = 0;
	
	private Set<Ability> learnedActions = new HashSet<Ability>();
	private Set<AppliedStatusEffect> statusEffects = new HashSet<AppliedStatusEffect>();
	
	public Unit(String name, TEAM team, URL sheetPath, int hp) {
		super(name, hp);
		this.team = team;
		
		nameLabel = new JLabel(name, SwingConstants.CENTER);
		nameLabel.setOpaque(true);
		nameLabel.setBackground(Color.BLACK);
		nameLabel.setForeground(Color.WHITE);
		if (sheetPath !=null) {
			sheet = SpriteSheet.getSpriteSheet(sheetPath);
			BufferedImage mini = new BufferedImage(SpriteSheet.SPRITE_WIDTH, MINI_SIZE, BufferedImage.TYPE_INT_RGB);
			Graphics g = mini.createGraphics();
			g.drawImage(sheet.getSprite(SpriteSheet.ANIMATION.WALK, SpriteSheet.FACING.S, 0), 0, -8, 
					SpriteSheet.SPRITE_WIDTH, SpriteSheet.SPRITE_HEIGHT, null);
			g.dispose();
			nameLabel.setIcon(new ImageIcon(mini));
		}
	}
	
	public BufferedImage getSprite() {
		if (sheet == null) {
			return new BufferedImage(1, 1, 1);
		}
		else {
			return sheet.getSprite(stance, facing, animationSequence);
		}
	}
	
	public void learnAction(Ability action) {
		learnedActions.add(action);
	}
	
	public void damage(int damage) {
		super.damage(damage);
		if (hp <= 0) {
			BattleQueue.removeCombatant(this);
			stance = SpriteSheet.ANIMATION.DEATH;
			this.animate(stance, 1000, false);
		}
	}
	
	public void addStatusEffect(AppliedStatusEffect effect) {
		statusEffects.add(effect);
	}
	
	public void tickStatusEffects(int time) {
		for (Iterator<AppliedStatusEffect> iterator = statusEffects.iterator(); iterator.hasNext();) {
		    AppliedStatusEffect statusEffect = iterator.next();
		    statusEffect.tick(time);
		    if (statusEffect.isOver()) {
		        iterator.remove();
		    }
		}
	}
	
	public void convertNameLabel(JLabel label) {
		label.setIcon(nameLabel.getIcon());
		label.setText(name);
	}
	
	public void setStance(SpriteSheet.ANIMATION stance) {
		this.stance = stance;
	}
	
	public void setFacing(SpriteSheet.FACING facing) {
		this.facing = facing;
	}
		
	public TEAM getTeam() {
		return team;
	}
	
	public Iterator<Ability> getKnownActions() {
		return learnedActions.iterator();
	}
	
	public JLabel getNameLabel() {
		return nameLabel;
	}
	
	public void aiQueueAction() {
		//TODO: more sophisticated AI with variety (prefer closest, prefer weakest, etc)
		Ability ability = learnedActions.iterator().next();// TODO Auto-generated method stub
		Unit target = World.getTargets(this, ability, GraphicsPanel.getScreenPos()).get(0);
		BattleQueue.queueAction(ability, this, target);
	}

	public void animate(ANIMATION stance, int duration, boolean returnToDefault) {
		animate(stance, duration, returnToDefault, 0);
	}
	
	public void animate(ANIMATION stance, int duration, boolean returnToDefault, int moveDistance) {
		int refreshRate = duration / ANIMATION_LENGTH;
		Unit unit = this;
		final int yOffset;
		final int xOffset;
		if (moveDistance != 0) {
			if (facing == FACING.N) {
				xOffset = 0;
				yOffset = moveDistance;
			} else if (facing == FACING.S) {
				xOffset = 0;
				yOffset = -moveDistance;
			} else if (facing == FACING.E) {
				xOffset = -moveDistance;
				yOffset = 0;
			} else {//if (facing == FACING.W) {
				xOffset = moveDistance;
				yOffset = 0;
			}
			drawXOffset = xOffset;
			drawYOffset = yOffset;
			this.setPos(pos.getX() - xOffset, pos.getY() - yOffset);
		} else {
			xOffset = yOffset = 0;
		}
		Thread animationThread = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < ANIMATION_LENGTH; i++) {
					drawXOffset = (ANIMATION_LENGTH - 1.0 - i) * xOffset / ANIMATION_LENGTH;
					drawYOffset = (ANIMATION_LENGTH - 1.0 - i) * yOffset / ANIMATION_LENGTH;
					animationSequence = i;
					if (stance != null) {
						unit.stance = stance;
					}
					try {
						Thread.sleep(refreshRate);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (returnToDefault) {
					animationSequence = 0;
					unit.stance = defaultStance;
				}
			}
		};
		animationThread.start();
	}

	public void face(ITargetable target) {
		face(target.getPos());
	}
	
	public void face(Position target) {
		int dx = target.getX() - getPos().getX();
		int dy = target.getY() - getPos().getY();
		int magnitudeX = (dx >= 0) ? dx : -dx;
		
		if (dy < 0 && -dy >= magnitudeX) {
			facing = SpriteSheet.FACING.N;
		}
		else if (dy > 0 && dy >= magnitudeX) {
			facing = SpriteSheet.FACING.S;
		}
		else if (dx > 0) {
			facing = SpriteSheet.FACING.E;
		}
		else if (dx < 0) {
			facing = SpriteSheet.FACING.W;
		}
	}
	
	@Override
	public void paint(Graphics2D g2) {
		super.paint(g2);
		AffineTransform savedTransorm = g2.getTransform();
		Position screenPos = GraphicsPanel.getScreenPos();
		// Convert to pixel space, accounting for units being tall objects
		g2.translate(GraphicsPanel.CELL_WIDTH * (pos.getX()-screenPos.getX()), 
				GraphicsPanel.TERRAIN_CELL_HEIGHT * 
				((pos.getY()-screenPos.getY()) - GraphicsPanel.TALL_OBJECT_CELL_HEIGHT_MULTIPLIER + 1));
		g2.translate(drawXOffset * GraphicsPanel.CELL_WIDTH, drawYOffset * GraphicsPanel.TERRAIN_CELL_HEIGHT);

		for (Iterator<AppliedStatusEffect> iterator = statusEffects.iterator(); iterator.hasNext();) {
		    BufferedImage icon = iterator.next().getStatusEffect().getIcon();
		    g2.drawImage(icon, 0, GraphicsPanel.TALL_OBJECT_CELL_HEIGHT * 3/4, null);
		}		
		g2.setTransform(savedTransorm);
	}
}
