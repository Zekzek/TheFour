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
import controller.BattleQueue;
import controller.plot.Plot;

public class Unit extends TallObject {
	public static enum TEAM { PLAYER, ALLY, NONCOMBATANT, ENEMY1, ENEMY2 }
	public static enum ID { DEFENDER, BERSERKER, SORCERESS, ARCHER, GUARD, FEMALE_BANDIT, MALE_BANDIT, ANNOUNCER }
	private static final int MINI_SIZE = 32;
	private static final int ANIMATION_LENGTH = 6;
	private static final Font ABILITY_FONT = new Font("Impact", 1, 24);
    
	private SpriteSheet sheet;
	private ImageIcon icon;
	private SpriteSheet.ANIMATION defaultStance = SpriteSheet.ANIMATION.WALK;
	private TEAM team;
	private SpriteSheet.FACING facing = SpriteSheet.FACING.S;
	private int animationSequence = 0;
	private SpriteSheet.ANIMATION stance = SpriteSheet.ANIMATION.WALK;
	private Weapon weapon;
	private Set<Ability> learnedActions = new HashSet<Ability>();
	private Set<StatusEffect> statusEffects = new HashSet<StatusEffect>();
	private String abilityString;
	
	private Unit(String name, int hp, URL sheetPath) {
		super(name, hp);
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
	
	private Unit(Unit otherUnit) {
		super(otherUnit);
		this.sheet = otherUnit.sheet;
		this.icon = otherUnit.icon;
		this.defaultStance = otherUnit.defaultStance;
		this.team = otherUnit.team;
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
		if (hp <= 0) {
			BattleQueue.removeCombatant(this, Ability.ID.DEATH);
		}
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
	
	public void aiQueueAction() {
		//TODO: more sophisticated AI with variety (prefer closest, prefer weakest, etc)
		double bestScore = Double.NEGATIVE_INFINITY;
		Ability bestAbility = null;
		Unit bestTarget = null;
		for (Ability ability : learnedActions) {
			for (Unit target : World.getTargets(this, ability, GraphicsPanel.getScreenRectangle())) {
				double score = getScore(ability, target);
//				System.out.println("(" + score + ")  " + ability + " -> " + target);
				if (score > bestScore) {
					bestScore = score;
					bestAbility = ability;
					bestTarget = target;
				}
			}
		}
		if (bestAbility != null && bestTarget != null) {
			BattleQueue.queueAction(bestAbility, this, bestTarget);
		} else {
			BattleQueue.queueAction(Ability.get(Ability.ID.DELAY), this, this);
		}
	}
	
	private double getScore(Ability ability, Unit target) {
		int damageBonus = ability.getDamage();
		int debuffBonus = 0;
		Iterator<StatusEffect> effects = ability.getStatusEffectIterator();
		while (effects.hasNext()) {
			if (!target.statusEffects.contains(effects.next())) {
				debuffBonus += 1;
			}
		}
		int benefit = damageBonus + 10 * debuffBonus;

		double delayBonus = 1000.0 / ability.getDelay();
		double rangeBonus;
		if (pos.equals(target.getPos())) {
			rangeBonus = 1.0;
		} else {
			double range = ability.getRange();
			double distance = pos.getDistanceTo(target.getPos());
			if (range >= distance) {
				rangeBonus = 2.0;
			} else {
				rangeBonus = 2.0 - ((distance - range) / 7.0);
			}
		}
		double alreadyQueuedPenalty = BattleQueue.isQueued(this, ability, target) ? 0.5 : 1.0;
		
		return benefit * delayBonus * rangeBonus * alreadyQueuedPenalty;
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

	public void animate(Ability ability) {
		ANIMATION stance = ability.getStance();//TODO: get stance from weapon and ability, then pick one 
		int duration = ability.calcDelay(getModifier());
		int moveDistance = ability.getMoveDistance();
		
		int refreshRate = duration / ANIMATION_LENGTH;
		Unit unit = this;
		
		final int yOffset;
		final int xOffset;
		//TODO: draw damage to screen?
		abilityString = ability.getName();
		if (moveDistance != 0) {
			xOffset = (facing == FACING.W ? moveDistance : facing == FACING.E ? -moveDistance : 0); 
			yOffset = (facing == FACING.N ? moveDistance : facing == FACING.S ? -moveDistance : 0); 
			drawXOffset = xOffset;
			drawYOffset = yOffset;
			this.updateWorldPos(pos.getX() - xOffset, pos.getY() - yOffset);
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
				abilityString = "";
			}
		};
		animationThread.start();
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
	
	public boolean isAllyOf(Unit unit) {
		return (isPlayerTeam() && unit.isPlayerTeam()) ||
				(isEnemyTeam() && unit.isEnemyTeam());
	}
	
	public boolean isEnemyOf(Unit unit) {
		return (isPlayerTeam() && unit.isEnemyTeam()) ||
				(isEnemyTeam() && unit.isPlayerTeam());
	}
	
	public boolean isPlayerTeam() {
		return team == TEAM.PLAYER || team == TEAM.ALLY;
	}
	
	public boolean isEnemyTeam() {
		return team == TEAM.ENEMY1 || team == TEAM.ENEMY2;
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
		private static Map<ID, Unit> units = new HashMap<ID, Unit>();
		private static boolean unitsInitialized = false;

		private static void initUnits() {
			unitsInitialized = true;
			Unit defender = new Unit("Defender", 250, Plot.class.getResource("/resource/img/spriteSheet/defender.png"));
			defender.learnAction(Ability.get(Ability.ID.GUARD_ATTACK));
			defender.learnAction(Ability.get(Ability.ID.SHIELD_BASH));
			defender.learnAction(Ability.get(Ability.ID.SWEEPING_STRIKE));
			defender.learnAction(Ability.get(Ability.ID.VIGOR));
			defender.setWeapon(Weapon.getWeapon(Weapon.ID.SPEAR_AND_SHIELD));
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 0.8);
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT, 0.8);
			defender.baseModifier.setBonus(FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SPELL, 0.9);
			units.put(ID.DEFENDER, defender);
			
			Unit sorceress = new Unit("Sorceress", 160, Plot.class.getResource("/resource/img/spriteSheet/sorceress.png"));
			sorceress.setWeapon(Weapon.getWeapon(Weapon.ID.DAGGER));
			sorceress.baseModifier.setBonus(FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 1.2);
			units.put(ID.SORCERESS, sorceress);

			Unit berserker = new Unit("Berserker", 180, Plot.class.getResource("/resource/img/spriteSheet/berserker.png"));
			berserker.learnAction(Ability.get(Ability.ID.CHALLENGE));
			berserker.learnAction(Ability.get(Ability.ID.SWEEPING_STRIKE));
			berserker.learnAction(Ability.get(Ability.ID.THROW));
			berserker.learnAction(Ability.get(Ability.ID.QUICK_ATTACK));
			berserker.learnAction(Ability.get(Ability.ID.HEAVY_ATTACK));
			berserker.learnAction(Ability.get(Ability.ID.KNOCKDOWN_STRIKE));
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
			unit.setName(name);
			return unit;
		}
	}
}
