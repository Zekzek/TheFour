package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import view.GraphicsPanel;
import view.SpriteSheet;
import view.SpriteSheet.ANIMATION;
import view.SpriteSheet.FACING;
import controller.BattleQueue;
import controller.plot.Plot;

public class Unit extends TallObject {
	public static enum TEAM { PLAYER, ALLY, NONCOMBATANT, ENEMY1, ENEMY2 }
	private static final int MINI_SIZE = 32;
	private static final int ANIMATION_LENGTH = 5;
	
	private URL sheetPath;
	private SpriteSheet sheet;
	private ImageIcon icon;
	private SpriteSheet.ANIMATION defaultStance = SpriteSheet.ANIMATION.WALK;
	
	private TEAM team;
	private SpriteSheet.FACING facing = SpriteSheet.FACING.S;
	private int animationSequence = 0;
	private Set<Ability> learnedActions = new HashSet<Ability>();
	private Set<AppliedStatusEffect> statusEffects = new HashSet<AppliedStatusEffect>();
	private SpriteSheet.ANIMATION stance = SpriteSheet.ANIMATION.WALK;
	
	private Unit(String name, int hp, URL sheetPath) {
		super(name, hp);
		this.sheetPath = sheetPath;
		if (sheetPath !=null) {
			sheet = SpriteSheet.getSpriteSheet(sheetPath);
			BufferedImage mini = new BufferedImage(SpriteSheet.SPRITE_WIDTH, MINI_SIZE, BufferedImage.TYPE_INT_RGB);
			Graphics g = mini.createGraphics();
			g.drawImage(sheet.getSprite(SpriteSheet.ANIMATION.WALK, SpriteSheet.FACING.S, 0), 0, -8, 
					SpriteSheet.SPRITE_WIDTH, SpriteSheet.SPRITE_HEIGHT, null);
			g.dispose();
			icon = new ImageIcon(mini);
		}
	}
	
	public Unit copy() {
		Unit unit = new Unit(name, hp, sheetPath);
		unit.team = team;
		unit.stance = stance;
		unit.facing = facing;
		for (Ability ability : learnedActions) {
			unit.learnAction(ability);
		}
		for (AppliedStatusEffect statusEffect : statusEffects) {
			unit.addStatusEffect(statusEffect);
		}
		return unit;
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
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
		label.setIcon(icon);
		label.setText(name);
	}
	
	public void aiQueueAction() {
		//TODO: more sophisticated AI with variety (prefer closest, prefer weakest, etc)
		Ability ability = learnedActions.iterator().next();
		Unit target = World.getTargets(this, ability, GraphicsPanel.getScreenPos()).get(0);
		BattleQueue.queueAction(ability, this, target);
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
	
	@Override
	public void paint(Graphics2D g2) {
		super.paint(g2);
		AffineTransform savedTransorm = g2.getTransform();
		Position screenPos = GraphicsPanel.getScreenPos();
		// Convert to pixel space, accounting for units being tall objects
		//TODO: this is duplicated code, combine with TallObject
		g2.translate(GraphicsPanel.CELL_WIDTH * (pos.getX()-screenPos.getX()), 
				GraphicsPanel.TERRAIN_CELL_HEIGHT * 
				((pos.getY()-screenPos.getY()) - GraphicsPanel.TALL_OBJECT_CELL_HEIGHT_MULTIPLIER + 1));
		g2.translate(drawXOffset * GraphicsPanel.CELL_WIDTH, drawYOffset * GraphicsPanel.TERRAIN_CELL_HEIGHT);

		for (Iterator<AppliedStatusEffect> iterator = statusEffects.iterator(); iterator.hasNext();) {
		    BufferedImage icon = iterator.next().getStatusEffect().getIcon();
		    g2.drawImage(icon, 0, GraphicsPanel.TALL_OBJECT_CELL_HEIGHT * 3/4, null);
		    //TODO: start a second row for more than 4 status effects?
		}
		g2.setTransform(savedTransorm);
	}
	
	public double getSpeedModifierAttack() {
		double speedMod = 1.0;
		Iterator<AppliedStatusEffect> iterator = statusEffects.iterator();
		while(iterator.hasNext()) {
			speedMod *= iterator.next().getStatusEffect().getSpeedModifierAttack();
		}
		return speedMod;
	}
	
	public static Unit get(String name, TEAM team) {
		return UnitFactory.getUnit(name, team);
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
	
	public void setTeam(TEAM team) {
		this.team = team;
	}
	
	private static class UnitFactory {
		private static Map<String, Unit> units = new HashMap<String, Unit>();
		private static boolean unitsInitialized = false;

		private static void initUnits() {
			unitsInitialized = true;
			Unit guard = new Unit("Guard", 100, Plot.class.getResource("/resource/img/spriteSheet/guard.png"));
			guard.learnAction(Ability.get("Guard Attack"));
			guard.learnAction(Ability.get("Watch"));
			units.put("Guard", guard);
			
			Unit announcer = new Unit("Announcer", 1,  Plot.class.getResource("/resource/img/spriteSheet/guard.png"));
			units.put("Announcer", announcer);
			
			Unit defender = new Unit("Defender", 200, Plot.class.getResource("/resource/img/spriteSheet/defender.png"));
			defender.setStance(SpriteSheet.ANIMATION.WALK);
			defender.learnAction(Ability.get("Quick Attack"));
			defender.learnAction(Ability.get("Shield Bash"));
			units.put("Defender", defender);
			
			Unit berserker = new Unit("Berserker", 220, Plot.class.getResource("/resource/img/spriteSheet/berserker.png"));
			berserker.setStance(SpriteSheet.ANIMATION.WALK);
			berserker.setFacing(SpriteSheet.FACING.W);
			berserker.learnAction(Ability.get("Heavy Strike"));
			units.put("Berserker", berserker);
			
			Unit femaleBandit = new Unit("Female Bandit", 100, Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"));
			femaleBandit.learnAction(Ability.get("Weak Attack"));
			units.put("Female Bandit", femaleBandit);
			
			Unit maleBandit = new Unit("Female Bandit", 100, Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"));
			maleBandit.learnAction(Ability.get("Weak Attack"));
			units.put("Male Bandit", maleBandit);
			
			Unit sorceress = new Unit("Sorceress", 160, Plot.class.getResource("/resource/img/spriteSheet/sorceress.png"));
			units.put("Sorceress", sorceress);
		}

		public static Unit getUnit(String name, TEAM team) {
			if (!unitsInitialized) {
				initUnits();
			}
			Unit unit = units.get(name).copy();
			unit.setTeam(team);
			return unit;
		}
	}
}
