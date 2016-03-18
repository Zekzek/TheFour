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

	public static enum TARGET_TYPE { HELPFUL, HARMFUL, PERSONAL, UBIQUITOUS, GROUND, DEAD };
	//SKILLs don't do damage
	public static enum CATEGORY { SKILL, ITEM, MOVE, ATTACK, 
		STRIKE, SHIELD,
		SHOT, BOW,
		SPELL };
	public static enum ID {
		ATTACK, WEAK_ATTACK, QUICK_ATTACK, HEAVY_ATTACK, 
		GUARD_ATTACK, BURNING_ATTACK, PINNING_ATTACK, KNOCKDOWN_STRIKE,
		SHIELD_BASH, BARRAGE, SWEEPING_STRIKE, THROW,
		CHALLENGE, SNARE,
		DELAY, WATCH, MOVE, DEATH     
	};
	
	private ID id;
	private String name;
	private TARGET_TYPE targetType;
	private CATEGORY category;
	private int delay;
	private int damage;
	private int range;
	private int areaOfEffectDistance;
	private String special;
	private ANIMATION stance;
	
	private int moveDistance;
	private int delayOpponent = 0;
	private Set<StatusEffect> statusEffects = new HashSet<StatusEffect>();
	
	private JLabel label;
	
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE targetType, int delay, int damage, int range,
			String special, ANIMATION stance) {
		this(id, name, category, targetType, delay, damage, range, 0, special, stance, 0);
	}
	
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE targetType, int delay, int damage, int range, 
			int radius, String special, ANIMATION stance, int moveDistance) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.targetType = targetType;
		this.delay = delay;
		this.damage = damage;
		this.range = range;
		this.areaOfEffectDistance = radius;
		this.special = special;
		this.stance = stance;
		this.moveDistance = moveDistance;
		label = new JLabel(name);
		label.setSize(100, 50);
		label.setPreferredSize(new Dimension(100, 50));
	}
	
	public static Ability get(ID name) {
		return AbilityFactory.getAbility(name);
	}
	
	public int calcDelay(Modifier modifier) {
		if (category == CATEGORY.STRIKE || category == CATEGORY.SHIELD) {
			return (int) (delay * modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE));
		} else if (category == CATEGORY.SHOT || category == CATEGORY.BOW) {
			return (int) (delay * modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_SHOT));
		} else if (category == CATEGORY.SPELL) {
			return (int) (delay * modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_SPELL));
		} else if (category == CATEGORY.MOVE) {
			return (int) (delay * modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_MOVE));
		} else {
			return delay;
		}
	}
	
	public int calcAdditionalDelay(Modifier modifier) {
		if (category == CATEGORY.STRIKE || category == CATEGORY.SHIELD) {
			return (int) (delay * (modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE)-1));
		} else if (category == CATEGORY.SHOT || category == CATEGORY.BOW) {
			return (int) (delay * (modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_SHOT)-1));
		} else if (category == CATEGORY.SPELL) {
			return (int) (delay * (modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_SPELL)-1));
		} else if (category == CATEGORY.MOVE) {
			return (int) (delay * (modifier.getBonus(Modifier.FRACTIONAL_BONUS.SPEED_MODIFIER_MOVE)-1));
		} else {
			return 0;
		}
	}
	
	public int calcDamage(Modifier user, Modifier target) {
		if (category == CATEGORY.STRIKE || category == CATEGORY.SHIELD) {
			return (int) (damage * user.getBonus(Modifier.FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE)
					* target.getBonus(Modifier.FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE));
		} else if (category == CATEGORY.SHOT || category == CATEGORY.BOW) {
			return (int) (damage * user.getBonus(Modifier.FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT)
					* target.getBonus(Modifier.FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT));
		} else if (category == CATEGORY.SPELL) {
			return (int) (damage * user.getBonus(Modifier.FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL)
					* target.getBonus(Modifier.FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SPELL));
		} else {
			return damage;
		}
	}
	
	public double calcChanceToHit(Modifier user, Modifier target) {
		double chanceToHit = 1.0;
		if (category == CATEGORY.STRIKE || category == CATEGORY.SHIELD) {
			return chanceToHit * user.getBonus(Modifier.FRACTIONAL_BONUS.ACCURACY_MODIFIER_STRIKE) 
					/ target.getBonus(Modifier.FRACTIONAL_BONUS.EVASION_MODIFIER_STRIKE);
		} else if (category == CATEGORY.SHOT || category == CATEGORY.BOW) {
			return chanceToHit * user.getBonus(Modifier.FRACTIONAL_BONUS.ACCURACY_MODIFIER_SHOT) 
					/ target.getBonus(Modifier.FRACTIONAL_BONUS.EVASION_MODIFIER_SHOT);
		} else if (category == CATEGORY.SPELL) {
			return chanceToHit * user.getBonus(Modifier.FRACTIONAL_BONUS.ACCURACY_MODIFIER_SPELL) 
					/ target.getBonus(Modifier.FRACTIONAL_BONUS.EVASION_MODIFIER_SPELL);
		} else {
			return chanceToHit;
		}
	}
	
	public double calcSuccessChance(Modifier modifier) {
		double successChance = 1.0;
		if (category == CATEGORY.STRIKE || category == CATEGORY.SHIELD) {
			return successChance * modifier.getBonus(Modifier.FRACTIONAL_BONUS.CHANCE_TO_SUCCEED_STRIKE);
		} else if (category == CATEGORY.SHOT || category == CATEGORY.BOW) {
			return successChance * modifier.getBonus(Modifier.FRACTIONAL_BONUS.CHANCE_TO_SUCCEED_SHOT);
		} else if (category == CATEGORY.SPELL) {
			return successChance * modifier.getBonus(Modifier.FRACTIONAL_BONUS.CHANCE_TO_SUCCEED_SPELL);
		} else if (category == CATEGORY.MOVE) {
			return successChance * modifier.getBonus(Modifier.FRACTIONAL_BONUS.CHANCE_TO_SUCCEED_MOVE);
		} else {
			return successChance;
		}
	}

	@Override
	public String toString() {
		return name;
	}
	public ID getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public TARGET_TYPE getTargetType() {
		return targetType;
	}
	public CATEGORY getCategory() {
		return category;
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
	public int getAreaOfEffectDistance() {
		return areaOfEffectDistance;
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
		private static Map<ID, Ability> abilities = new HashMap<ID, Ability>();
		private static boolean abilitiesInitialized = false;
		
		private static void initAbilities() {
			abilitiesInitialized = true;
			
			// base 25 damage/second, less with status effects or range, etc
						
			// Generic attacks
			abilities.put(ID.ATTACK, new Ability(ID.ATTACK, "Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.HARMFUL, 800, 25, 1,
					"A basic attack, average in every way", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.QUICK_ATTACK, new Ability(ID.QUICK_ATTACK, "Quick Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.HARMFUL, 800, 25, 1,
					"A quick jab", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.HEAVY_ATTACK, new Ability(ID.HEAVY_ATTACK, "Heavy Strike", CATEGORY.ATTACK, Ability.TARGET_TYPE.HARMFUL, 2200, 45, 1,
					"A slow, powerful attack", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.WEAK_ATTACK, new Ability(ID.WEAK_ATTACK, "Weak Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.HARMFUL, 1000, 11, 1, 
					"A weak attack", SpriteSheet.ANIMATION.MELEE));
			
			// Generic status effect attacks 
			Ability guardAttack = new Ability(ID.GUARD_ATTACK, "Guard Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.HARMFUL, 1000, 20, 1,
					"The standard guard's strike", SpriteSheet.ANIMATION.MELEE);
			guardAttack.statusEffects.add(new StatusEffect(StatusEffect.ID.DAMAGE_DOWN, 1100));
			abilities.put(ID.GUARD_ATTACK, guardAttack);
			Ability ignite = new Ability(ID.BURNING_ATTACK, "Ignite", CATEGORY.ATTACK, TARGET_TYPE.HARMFUL, 1200, 25, 10,
					"Set the enemy ablaze", ANIMATION.RANGE);
			ignite.statusEffects.add(new StatusEffect(StatusEffect.ID.BURNING, 8000));
			abilities.put(ID.BURNING_ATTACK, ignite);
			Ability pinningAttack = new Ability(ID.PINNING_ATTACK, "Pinning Shot", CATEGORY.ATTACK, TARGET_TYPE.HARMFUL, 1200, 20, 10,
					"An attack aimed at the legs. It is designed to prevent movement", ANIMATION.RANGE);
			pinningAttack.statusEffects.add(new StatusEffect(StatusEffect.ID.BIND, 2000));
			abilities.put(ID.PINNING_ATTACK, pinningAttack);
			
			// Weapon-specific attacks
			Ability shieldBash = new Ability(ID.SHIELD_BASH, "Shield Bash", CATEGORY.SHIELD, Ability.TARGET_TYPE.HARMFUL, 1200, 30, 1, 
					"Briefly disorients the target", SpriteSheet.ANIMATION.MELEE);
			shieldBash.delayOpponent = 300;
			shieldBash.statusEffects.add(new StatusEffect(StatusEffect.ID.SLOW, 3000));
			abilities.put(ID.SHIELD_BASH, shieldBash);
			Ability barrage = new Ability(ID.BARRAGE, "Barrage", CATEGORY.SHOT, TARGET_TYPE.GROUND, 1500, 20, 8,
					2, "Pepper an area with shots", ANIMATION.RANGE, 0);
			abilities.put(ID.BARRAGE, barrage);
			//TODO: Spin animation
			Ability sweep = new Ability(ID.SWEEPING_STRIKE, "Sweep", CATEGORY.STRIKE, TARGET_TYPE.PERSONAL, 2000, 20, 0,
					1, "Twirl about, attacking everything nearby", ANIMATION.MELEE, 0);
			abilities.put(ID.SWEEPING_STRIKE, sweep);			
			Ability throwObject = new Ability(ID.THROW, "Throw", CATEGORY.STRIKE, TARGET_TYPE.HARMFUL, 1400, 20, 6,
					"Hurl your weapon at a distant target (it comes back, obviously...)", ANIMATION.MELEE);
			abilities.put(ID.THROW, throwObject);
			//TODO: force fall down ability, followed by get up ability?
			Ability knockdown = new Ability(ID.KNOCKDOWN_STRIKE, "Knockdown", CATEGORY.STRIKE, TARGET_TYPE.HARMFUL, 2000, 10, 1,
					"A forceful, ramming attack that will bring your oponent to his knees", ANIMATION.MELEE);
			knockdown.delayOpponent = 800;
			knockdown.statusEffects.add(new StatusEffect(StatusEffect.ID.KNOCKDOWN, 800));
			abilities.put(ID.KNOCKDOWN_STRIKE, knockdown);

			// Skills
			//TODO: Snare needs to create a tall object? Need enemies to move into it? Just next to it? hidden?
			Ability snare = new Ability(ID.SNARE, "Snare", CATEGORY.SKILL, TARGET_TYPE.GROUND, 8000, 25, 1, 
					"Prepare a trap", SpriteSheet.ANIMATION.DEATH);
			abilities.put(ID.SNARE, snare);
			//TODO: point animation?
			Ability challenge = new Ability(ID.CHALLENGE, "Challenge", CATEGORY.SKILL, TARGET_TYPE.HARMFUL, 500, 1, 10,
					"'Encourage' your oponent to focus on attacking only you.", ANIMATION.CAST);
			challenge.statusEffects.add(new StatusEffect(StatusEffect.ID.MURDEROUS_INTENT, 10000));
			abilities.put(ID.CHALLENGE, challenge);
			
			// Movement & Time killers
			abilities.put(ID.MOVE, new Ability(ID.MOVE, "MOVE", CATEGORY.MOVE, TARGET_TYPE.PERSONAL, 500, 0, 1, 
					0, "Walk, while keeping your guard up", SpriteSheet.ANIMATION.WALK, 1));
			abilities.put(ID.DELAY, new Ability(ID.DELAY, "Delay", CATEGORY.SKILL, TARGET_TYPE.PERSONAL, 1000, 0, 0,
					"Wait", SpriteSheet.ANIMATION.WALK));
			abilities.put(ID.WATCH, new Ability(ID.WATCH, "Watch", CATEGORY.SKILL, TARGET_TYPE.UBIQUITOUS, 5000, 0, 16,
					"Watch", ANIMATION.WALK));
			//TODO: stay down
			abilities.put(ID.DEATH, new Ability(ID.DEATH, "Death", CATEGORY.SKILL, TARGET_TYPE.DEAD, 1000, 0, 0,
					"Lose the will to fight on", ANIMATION.DEATH));
		}
		
		public static Ability getAbility(ID id) {
			if (!abilitiesInitialized) {
				initAbilities();
			}
			return abilities.get(id);
		}
	}
}
