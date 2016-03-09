package model;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import view.SpriteSheet;
import view.SpriteSheet.ANIMATION;

public class Ability {

	public static enum TARGET_TYPE { HELPFUL, HARMFUL, PERSONAL, UBIQUITOUS, GROUND };
	public static enum CATEGORY { SKILL, SPELL, ITEM, MOVE };
	
	private String name;
	private TARGET_TYPE targetType;
	private CATEGORY category;
	private int delay;
	private int damage;
	private int range;
	private String special;
	private ANIMATION stance;
	private int moveDistance;
	private int delayOpponent = 0;
	private Set<StatusEffect> statusEffects = new HashSet<StatusEffect>();
	
	private JLabel label;
	
	private Ability(String name, CATEGORY category, TARGET_TYPE targetType, int delay, int damage, int range, 
			String special, ANIMATION stance) {
		this(name, category, targetType, delay, damage, range, special, stance, 0);
	}
	
	private Ability(String name, CATEGORY category, TARGET_TYPE targetType, int delay, int damage, int range, 
			String special, ANIMATION stance, int moveDistance) {
		this.name = name;
		this.category = category;
		this.targetType = targetType;
		this.delay = delay;
		this.damage = damage;
		this.range = range;
		this.special = special;
		this.stance = stance;
		this.moveDistance = moveDistance;
		label = new JLabel(name);
		label.setSize(100, 50);
		label.setPreferredSize(new Dimension(100, 50));
	}
	
	public static Ability get(String name) {
		return AbilityFactory.getAbility(name);
	}

	@Override
	public String toString() {
		return name;
	}
	public String getName() {
		return name;
	}
	public CATEGORY getCategory() {
		return category;
	}
	public TARGET_TYPE getOutcome() {
		return targetType;
	}
	public int getDelay() {
		return delay;
	}
	public int getDamage() {
		return damage;
	}
	public int getRange() {
		return range;
	}
	public String getSpecial() {
		return special;
	}
	public JLabel getLabel() {
		return label;
	}
	public int getMoveDistance() {
		return moveDistance;
	}
	public SpriteSheet.ANIMATION getStance() {
		return stance;
	}
	public int getDelayOpponent() {
		return delayOpponent;
	}
	public Iterator<StatusEffect> getStatusEffectIterator() {
		return statusEffects.iterator();
	}

	private static class AbilityFactory {
		private static Map<String, Ability> abilities = new HashMap<String, Ability>();
		private static boolean abilitiesInitialized = false;
		
		private static void initAbilities() {
			abilitiesInitialized = true;
			abilities.put("Delay", new Ability("Delay", CATEGORY.SKILL, TARGET_TYPE.PERSONAL, 1000, 0, 1,
					"Wait", SpriteSheet.ANIMATION.WALK));
			abilities.put("Move", new Ability("MOVE", CATEGORY.MOVE, TARGET_TYPE.PERSONAL, 500, 0, 1, 
					"Walk, while keeping your guard up", SpriteSheet.ANIMATION.WALK, 1));
			abilities.put("Watch", new Ability("Watch", CATEGORY.SKILL, TARGET_TYPE.UBIQUITOUS, 5000, 0, 16,
					"Watch", ANIMATION.WALK));
			abilities.put("Quick Attack", new Ability("Quick Attack", CATEGORY.SKILL, Ability.TARGET_TYPE.HARMFUL, 800, 32, 1,
					"A quick jab", SpriteSheet.ANIMATION.MELEE));
			abilities.put("Heavy Strike", new Ability("Heavy Strike", CATEGORY.SKILL, Ability.TARGET_TYPE.HARMFUL, 2200, 45, 1,
					"A slow, powerful attack", SpriteSheet.ANIMATION.MELEE));
			abilities.put("Weak Attack", new Ability("Weak Attack", CATEGORY.SKILL, Ability.TARGET_TYPE.HARMFUL, 1000, 11, 1, 
					"A weak attack", SpriteSheet.ANIMATION.MELEE));
			abilities.put("Guard Attack", new Ability("Guard Attack", CATEGORY.SKILL, Ability.TARGET_TYPE.HARMFUL, 1000, 20, 1,
					"The standard guard's strike", SpriteSheet.ANIMATION.MELEE));
			
			Ability shieldBash = new Ability("Shield Bash", CATEGORY.SKILL, Ability.TARGET_TYPE.HARMFUL, 1200, 30, 1, 
					"Briefly disorients the target", SpriteSheet.ANIMATION.MELEE);
			shieldBash.delayOpponent = 300;
			shieldBash.statusEffects.add(StatusEffect.get("Slow"));
			abilities.put("Shield Bash", shieldBash);
		}
		
		public static Ability getAbility(String name) {
			if (!abilitiesInitialized) {
				initAbilities();
			}
			return abilities.get(name);
		}
	}
}
