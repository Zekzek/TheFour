package model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

import model.Modifier.FLAT_BONUS;
import model.Modifier.FRACTIONAL_BONUS;
import view.GraphicsPanel;
import view.SpriteSheet;
import view.SpriteSheet.ANIMATION;
import view.SpriteSheet.FACING;
import controller.plot.Plot;

public class Unit extends GameObject {
	public static enum ID { DEFENDER, BERSERKER, SORCERESS, ARCHER, GUARD, FEMALE_BANDIT, MALE_BANDIT, ANNOUNCER, 
		FEMALE_GOBLIN, MALE_GOBLIN, GOBLIN_CHIEF, GIRL, BOY }
	private static final int MINI_SIZE = 32;
	public static final int ANIMATION_LENGTH = 6;
	private static final Font ABILITY_FONT = new Font("Impact", 1, 24);
    
	private static World world;
	
	private SpriteSheet sheet;
	private BufferedImage mini;
	private ImageIcon icon;
	private SpriteSheet.ANIMATION defaultStance = SpriteSheet.ANIMATION.WALK;
	private SpriteSheet.FACING facing = SpriteSheet.FACING.S;
	private int animationSequence = 0;
	private SpriteSheet.ANIMATION stance = SpriteSheet.ANIMATION.WALK;
	private Weapon weapon;
	private Set<Ability> learnedActions = new HashSet<Ability>();
	private Set<StatusEffect> statusEffects = new HashSet<StatusEffect>();
	private String abilityString;
	private boolean inCombat;
	private int xOffset;
	private int yOffset;
	
	private Unit(String name, int hp, URL sheetPath) {
		super(name, hp);
		learnAction(Ability.get(Ability.ID.MOVE));
		if (sheetPath !=null) {
			sheet = SpriteSheet.getSpriteSheet(sheetPath);
			mini = new BufferedImage(SpriteSheet.SPRITE_WIDTH, MINI_SIZE, BufferedImage.TYPE_INT_RGB);
			Graphics g = mini.createGraphics();
			g.drawImage(sheet.getSprite(SpriteSheet.ANIMATION.WALK, SpriteSheet.FACING.S, 0), 0, -8, 
					SpriteSheet.SPRITE_WIDTH, SpriteSheet.SPRITE_HEIGHT, null);
			g.dispose();
			icon = new ImageIcon(mini);
		}
	}
	
	private Unit(Unit otherUnit) {
		super(otherUnit);
		this.sheet = otherUnit.sheet;
		this.mini = otherUnit.mini;
		this.icon = otherUnit.icon;
		this.defaultStance = otherUnit.defaultStance;
		this.facing = otherUnit.facing;
		this.animationSequence = otherUnit.animationSequence;
		this.stance = otherUnit.stance;
		for (Ability ability : otherUnit.learnedActions) {
			this.learnedActions.add(ability);
		}
		for (StatusEffect statusEffect : otherUnit.statusEffects) {
			this.statusEffects.add(statusEffect);
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
	
	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}
	
	public void damage(int damage) {
		int shield = getModifier().getBonus(FLAT_BONUS.HP_SHIELD);
		if (shield > 0) {
			damage = damageHpShield(damage);
		}
		super.damage(damage);
	}
	
	private int damageHpShield(int damage) {
		int remainingDamage = damage;
		Iterator<StatusEffect> statusEffectIterator = statusEffects.iterator();
		while (statusEffectIterator.hasNext()) {
			StatusEffect effect = statusEffectIterator.next();
			remainingDamage = effect.getModifier().damageHpShield(remainingDamage);
		}
		return remainingDamage;
	}

	public void heal(int amount) {
		super.heal(amount);
	}
	
	public void addStatusEffect(StatusEffect effect) {
		for (StatusEffect currentEffect : statusEffects) {
			if (currentEffect.equals(effect)) {
				currentEffect.updateWith(effect);
				return;
			}
		}
		statusEffects.add(effect);
	}
	
	public void tickStatusEffects(int time) {
		for (Iterator<StatusEffect> iterator = statusEffects.iterator(); iterator.hasNext();) {
		    StatusEffect statusEffect = iterator.next();
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

	public ReadiedAction aiGetAction(long time) {
		//TODO: more sophisticated AI with variety (prefer closest, prefer weakest, etc)
		double bestScore = Double.NEGATIVE_INFINITY;
		Ability bestAbility = null;
		ITargetable bestTarget = null;
		for (Ability ability : learnedActions) {
			for (ITargetable target : world.getTargets(this, ability)) {
				double score = getScore(ability, target);
				System.out.println("(" + score + ")  " + ability + " -> " + target);
				if (score > bestScore) {
					bestScore = score;
					bestAbility = ability;
					bestTarget = target;
				}
			}
		}
		if (bestAbility != null && bestTarget != null) {
			System.out.println("Choosing (" + bestScore + ")  " + bestAbility + " -> " + bestTarget);
			return new ReadiedAction(bestAbility, this, bestTarget, time);
		} else {
			return new ReadiedAction(Ability.get(Ability.ID.DELAY), this, this, time);
		}		
	}
	
	
	private double getScore(Ability ability, ITargetable target) {
		int damageBonus = ability.getDamage();
		int debuffBonus = 0;
		if (target instanceof Unit) {
		Iterator<StatusEffect> effects = ability.getStatusToTargetEffectIterator();
			Unit targetUnit = (Unit)target;
			while (effects.hasNext()) {
				if (!targetUnit.statusEffects.contains(effects.next())) {
					debuffBonus += 1;
				}
			}
		}
		int benefit = damageBonus + 10 * debuffBonus;

		double delayBonus = 1000.0 / ability.getDelay();
		double rangeBonus;
		if (pos.equals(target.getPos())) {
			rangeBonus = 1.0;
		} else {
			double range = ability.getRange();
			double distance = pos.getDistanceFromCenterTo(target.getPos());
			if (range >= distance) {
				rangeBonus = 4.0;
			} else {
				rangeBonus = 4.0 - ((distance - range) / 3.0);
			}
		}
		
		System.out.println(rangeBonus);
		return benefit * delayBonus * rangeBonus;
	}
	
	public void face(ITargetable target) {
		face(target.getPos());
	}
	
	public void face(GridPosition target) {
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

//	public void animate(Ability ability) {
//		ANIMATION stance = ability.getStance();//TODO: get stance from weapon and ability, then pick one 
//		int duration = ability.calcDelay(getModifier());
//		int moveDistance = ability.getMoveDistance();
//		
//		int refreshRate = duration / ANIMATION_LENGTH;
//		Unit unit = this;
//		
//		final int yOffset;
//		final int xOffset;
//		//TODO: draw damage to screen?
//		abilityString = ability.getName();
//		if (moveDistance != 0) {
//			xOffset = (facing == FACING.W ? moveDistance : facing == FACING.E ? -moveDistance : 0); 
//			yOffset = (facing == FACING.N ? moveDistance : facing == FACING.S ? -moveDistance : 0); 
//			pos.setxOffset(xOffset);
//			pos.setyOffset(yOffset);
//			this.updateWorldPos(world, pos.getX() - xOffset, pos.getY() - yOffset);
//		} else {
//			xOffset = yOffset = 0;
//		}
//		Thread animationThread = new Thread() {
//			@Override
//			public void run() {
//				for (int i = 0; i < ANIMATION_LENGTH; i++) {
//					pos.setxOffset((ANIMATION_LENGTH - 1f - i) * xOffset / ANIMATION_LENGTH);
//					pos.setyOffset((ANIMATION_LENGTH - 1f - i) * yOffset / ANIMATION_LENGTH);
//					animationSequence = i;
//					if (stance != null) {
//						unit.stance = stance;
//					}
//					try {
//						Thread.sleep(refreshRate);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				abilityString = "";
//			}
//		};
//		animationThread.start();
//	}
	
	public void setAnimationFrame(int frame, Ability ability) {
		if (frame == 0) {
			if (ability.getStance() != null) {
				stance = ability.getStance();
			}
			int moveDistance = ability.getMoveDistance();
			abilityString = ability.getName();
			if (moveDistance != 0) {
				xOffset = (facing == FACING.W ? moveDistance : facing == FACING.E ? -moveDistance : 0); 
				yOffset = (facing == FACING.N ? moveDistance : facing == FACING.S ? -moveDistance : 0); 
				pos.setxOffset(xOffset);
				pos.setyOffset(yOffset);
				this.updateWorldPos(world, pos.getX() - xOffset, pos.getY() - yOffset);
			} else {
				xOffset = yOffset = 0;
			}
		}
		else if ( frame == ANIMATION_LENGTH) {
			abilityString = "";
			xOffset = yOffset = 0;
		}

		pos.setxOffset((ANIMATION_LENGTH - 1f - frame) * xOffset / ANIMATION_LENGTH);
		pos.setyOffset((ANIMATION_LENGTH - 1f - frame) * yOffset / ANIMATION_LENGTH);
		animationSequence = frame % ANIMATION_LENGTH;//TODO: remove?
		
	}
	
	protected void paintDecorations(Graphics2D g2) {
		int statusEffectCounter = 0;
		for (Iterator<StatusEffect> iterator = statusEffects.iterator(); iterator.hasNext();) {
		    BufferedImage icon = iterator.next().getIcon();
		    g2.drawImage(icon, GraphicsPanel.CELL_WIDTH * (statusEffectCounter % 4) / 4,
		    		GraphicsPanel.TALL_OBJECT_CELL_HEIGHT * (3 - (statusEffectCounter / 4))  / 4, null);
		    statusEffectCounter++;
		}
		
		if (abilityString != null && abilityString.length() > 0) {
		    GraphicsPanel.drawCenteredText(g2, abilityString, GraphicsPanel.CELL_WIDTH/2, GraphicsPanel.TALL_OBJECT_CELL_HEIGHT/4,
		    		ABILITY_FONT, Color.BLACK, Color.WHITE);
		}
	}
	
	@Override
	public Modifier getModifier() {
		Modifier[] statusModifiers = new Modifier[statusEffects.size()];
		Iterator<StatusEffect> statusEffectIterator = statusEffects.iterator();
		int count = 0;
		while (statusEffectIterator.hasNext()) {
			statusModifiers[count] = statusEffectIterator.next().getModifier();
			count++;
		}
		Modifier netModifier = new Modifier(statusModifiers);
		netModifier.addModifier(baseModifier);
		if (weapon != null) {
			netModifier.addModifier(weapon.getModifier());
		}
		return netModifier;
	}
	
	public double getStatusEffectModifier(FRACTIONAL_BONUS effect, ITargetable target) {
		double mod = 1.0;
		Iterator<StatusEffect> iterator = statusEffects.iterator();
		while(iterator.hasNext()) {
			mod *= iterator.next().getBonus(effect, target);
		}
		return mod;
	}
	
	public int getStatusEffectModifier(FLAT_BONUS effect, ITargetable target) {
		int mod = 0;
		Iterator<StatusEffect> iterator = statusEffects.iterator();
		while(iterator.hasNext()) {
			mod += iterator.next().getBonus(effect, target);
		}
		return mod;
	}
	
	public BufferedImage getMini() {
		return mini;
	}
	
	public static Unit get(ID id, TEAM team) {
		return UnitFactory.getUnit(id, team, null);
	}
	
	public static Unit get(ID id, TEAM team, String name) {
		return UnitFactory.getUnit(id, team, name);
	}
	
	public void setStance(SpriteSheet.ANIMATION stance) {
		this.stance = stance;
	}
	
	public void setFacing(SpriteSheet.FACING facing) {
		this.facing = facing;
	}
		
	public Iterator<Ability> getKnownActions() {
		return learnedActions.iterator();
	}
	
	private static class UnitFactory {
		private static Map<ID, Unit> units = new HashMap<ID, Unit>();
		private static boolean unitsInitialized = false;

		private static void initUnits() {
			unitsInitialized = true;
			Unit defender = new Unit("Steele", 250, Plot.class.getResource("/resource/img/spriteSheet/defender.png"));
			defender.learnAction(Ability.get(Ability.ID.GUARD_ATTACK));
			defender.learnAction(Ability.get(Ability.ID.SHIELD_BASH));
			defender.learnAction(Ability.get(Ability.ID.CHALLENGE));
			defender.learnAction(Ability.get(Ability.ID.VIGOR));
			defender.setWeapon(Weapon.getWeapon(Weapon.ID.SPEAR_AND_SHIELD));
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 0.8);
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT, 0.8);
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SPELL, 0.9);
			units.put(ID.DEFENDER, defender);
			
			Unit sorceress = new Unit("Juliana", 160, Plot.class.getResource("/resource/img/spriteSheet/sorceress.png"));
			sorceress.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			sorceress.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 1.2);
			units.put(ID.SORCERESS, sorceress);

			Unit berserker = new Unit("Destrox", 180, Plot.class.getResource("/resource/img/spriteSheet/berserker.png"));
			berserker.learnAction(Ability.get(Ability.ID.SWEEPING_STRIKE));
			berserker.learnAction(Ability.get(Ability.ID.THROW));
			berserker.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			berserker.learnAction(Ability.get(Ability.ID.HEAVY_ATTACK));
			berserker.setWeapon(Weapon.getWeapon(Weapon.ID.MACE));
			berserker.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 1.2);
			berserker.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 1.1);
			units.put(ID.BERSERKER, berserker);

			Unit archer = new Unit("Archer", 190, Plot.class.getResource("/resource/img/spriteSheet/archer.png"));
			archer.learnAction(Ability.get(Ability.ID.SNARE));
			archer.learnAction(Ability.get(Ability.ID.BARRAGE));
			archer.learnAction(Ability.get(Ability.ID.BURNING_ATTACK));
			archer.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			archer.learnAction(Ability.get(Ability.ID.HEAVY_ATTACK));
			archer.setWeapon(Weapon.getWeapon(Weapon.ID.BOW));
			archer.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 1.1);
			archer.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 1.2);
			units.put(ID.ARCHER, archer);

			Unit guard = new Unit("Guard", 150, Plot.class.getResource("/resource/img/spriteSheet/guard.png"));
			guard.learnAction(Ability.get(Ability.ID.GUARD_ATTACK));
			guard.learnAction(Ability.get(Ability.ID.WATCH));
			guard.setWeapon(Weapon.getWeapon(Weapon.ID.SPEAR));
			guard.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 0.95);
			guard.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT, 0.95);
			units.put(ID.GUARD, guard);
			
			Unit announcer = new Unit("Announcer", 1,  Plot.class.getResource("/resource/img/spriteSheet/guard.png"));
			announcer.setWeapon(Weapon.getWeapon(Weapon.ID.UNARMED));
			units.put(ID.ANNOUNCER, announcer);

			Unit femaleBandit = new Unit("Female Bandit", 60, Plot.class.getResource("/resource/img/spriteSheet/banditFemale.png"));
			femaleBandit.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			femaleBandit.learnAction(Ability.get(Ability.ID.ATTACK));
			femaleBandit.learnAction(Ability.get(Ability.ID.THROW));
			femaleBandit.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			femaleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.39);
			femaleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.41);
			femaleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.4);
			units.put(ID.FEMALE_BANDIT, femaleBandit);
			
			Unit maleBandit = new Unit("Male Bandit", 60, Plot.class.getResource("/resource/img/spriteSheet/banditMale.png"));
			maleBandit.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			maleBandit.learnAction(Ability.get(Ability.ID.ATTACK));
			maleBandit.learnAction(Ability.get(Ability.ID.THROW));
			maleBandit.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			maleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.41);
			maleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.39);
			maleBandit.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.39);
			units.put(ID.MALE_BANDIT, maleBandit);
			
			Unit femaleGoblin = new Unit("Female Goblin", 60, Plot.class.getResource("/resource/img/spriteSheet/goblinFemale.png"));
			femaleGoblin.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			femaleGoblin.learnAction(Ability.get(Ability.ID.ATTACK));
			femaleGoblin.learnAction(Ability.get(Ability.ID.THROW));
			femaleGoblin.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			femaleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.39);
			femaleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.41);
			femaleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.39);
			units.put(ID.FEMALE_GOBLIN, femaleGoblin);
			
			Unit maleGoblin = new Unit("Male Goblin", 60, Plot.class.getResource("/resource/img/spriteSheet/goblinMale.png"));
			maleGoblin.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			maleGoblin.learnAction(Ability.get(Ability.ID.ATTACK));
			maleGoblin.learnAction(Ability.get(Ability.ID.THROW));
			maleGoblin.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			maleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.41);
			maleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.39);
			maleGoblin.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.39);
			units.put(ID.MALE_GOBLIN, maleGoblin);
			
			Unit goblinChief = new Unit("Chief Drenug", 300, Plot.class.getResource("/resource/img/spriteSheet/goblinChief.png"));
			goblinChief.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			goblinChief.learnAction(Ability.get(Ability.ID.ATTACK));
			goblinChief.learnAction(Ability.get(Ability.ID.THROW));
			goblinChief.setWeapon(Weapon.getWeapon(Weapon.ID.MACE));
			units.put(ID.GOBLIN_CHIEF, goblinChief);
			
			Unit girl = new Unit("Girl", 60, Plot.class.getResource("/resource/img/spriteSheet/girl.png"));
			girl.learnAction(Ability.get(Ability.ID.WATCH));
			girl.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.3);
			girl.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.3);
			girl.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.3);
			units.put(ID.GIRL, girl);
			
			Unit boy = new Unit("Boy", 60, Plot.class.getResource("/resource/img/spriteSheet/boy.png"));
			boy.learnAction(Ability.get(Ability.ID.WATCH));
			boy.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.3);
			boy.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.3);
			boy.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.3);
			units.put(ID.BOY, boy);
		}

		public static Unit getUnit(ID id, TEAM team, String name) {
			if (!unitsInitialized) {
				initUnits();
			}
			Unit unit = units.get(id);
			if (unit == null) {
				throw new UnsupportedOperationException("Not implemented yet - Unit::" + id.name());
			}
			unit = new Unit(unit);
			unit.setTeam(team);
			if (name != null) {
				unit.setName(name);
			}
			return unit;
		}
	}

	public Iterator<StatusEffect> getStatusEffects() {
		return statusEffects.iterator();
	}

	public void setInCombat(boolean value) {
		inCombat = value;
	}
	
	public boolean isInCombat() {
		return inCombat;
	}
	
	public static void setWorld(World world) {
		Unit.world = world;
	}
}
