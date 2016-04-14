package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import view.SpriteSheet;
import view.SpriteSheet.ANIMATION;

public class Ability {
	//TODO: display in 'folders'

	public static enum TARGET_TYPE { SELF, ALLY, ENEMY, ALL, GROUND, DEAD };
	public static enum EFFECT {BUFF, DEBUFF, ATTACK, MOVE};
	//SKILLs don't do damage
	public static enum CATEGORY { SKILL, ITEM, MOVE, ATTACK, 
		STRIKE, SHIELD,
		SHOT, BOW,
		SPELL };
	public static enum ID {
		AI_TURN,
		ATTACK, WEAK_ATTACK, QUICK_ATTACK, HEAVY_ATTACK, 
		GUARD_ATTACK, BURNING_ATTACK, PINNING_ATTACK, KNOCKDOWN_STRIKE,
		SHIELD_BASH, BARRAGE, SWEEPING_STRIKE, THROW,
		VIGOR,
		CHALLENGE, SNARE,
		DELAY, WATCH, MOVE, DEATH     
	};
	
	private ID id;
	private String name;
	private CATEGORY category;
	private TARGET_TYPE selectionTargetType;
	private TARGET_TYPE affectsTargetType;
	private EFFECT effect;
	private int delay;
	private int damage;
	private int range;
	private int areaOfEffectDistance;
	private String special;
	private ANIMATION stance;
	
	private int moveDistance;
	private int delayOpponent;
	private Set<StatusEffect> statusEffects = new HashSet<StatusEffect>();
	
	private JLabel label;
		
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE targetType, EFFECT effect, int delay, 
			int damage, String special, ANIMATION stance) {
		this(id, name, category, targetType, targetType, effect, delay, damage, 1, 0, special, stance, 0, 0);
	}
	
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE targetType, EFFECT effect, int delay, 
			int damage, int range, int areaOfEffectDistance, String special, ANIMATION stance) {
		this(id, name, category, targetType, targetType, effect, delay, damage, range,
				areaOfEffectDistance, special, stance, 0, 0);
	}
	
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE selectionTargetType, TARGET_TYPE affectsTargetType,
			EFFECT effect, int delay, int damage, int range, int areaOfEffectDistance, String special, ANIMATION stance) {
		this(id, name, category, selectionTargetType, affectsTargetType, effect, delay, damage, range,
				areaOfEffectDistance, special, stance, 0, 0);
	}
	
	private Ability(ID id, String name, CATEGORY category, TARGET_TYPE selectionTargetType, TARGET_TYPE affectsTargetType,
			EFFECT effect, int delay, int damage, int range, int areaOfEffectDistance, String special, ANIMATION stance,
			int moveDistance, int delayOpponent) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.selectionTargetType = selectionTargetType;
		this.affectsTargetType = affectsTargetType;
		this.effect = effect;
		this.delay = delay;
		this.damage = damage;
		this.range = range;
		this.areaOfEffectDistance = areaOfEffectDistance;
		this.special = special;
		this.stance = stance;
		this.moveDistance = moveDistance;
		this.delayOpponent = delayOpponent;
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
	public CATEGORY getCategory() {
		return category;
	}
	public TARGET_TYPE getSelectionTargetType() {
		return selectionTargetType;
	}
	public TARGET_TYPE getAffectsTargetType() {
		return affectsTargetType;
	}
	public EFFECT getEffect() {
		return effect;
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
	public ANIMATION getStance() {
		return stance;
	}
	public int getMoveDistance() {
		return moveDistance;
	}
	public int getDelayOpponent() {
		return delayOpponent;
	}
	public JLabel getLabel() {
		return label;
	}
	public Iterator<StatusEffect> getStatusEffectIterator() {
		return statusEffects.iterator();
	}

	private static class AbilityFactory {
		private static Map<ID, Ability> abilities = new HashMap<ID, Ability>();
		private static boolean abilitiesInitialized = false;
		
		private static void initAbilities() {
			abilitiesInitialized = true;
			//Should average 25 DPS. Penalty for slow(damage up front), ranged, debuff, etc
			
			// AI Placeholder
			abilities.put(ID.AI_TURN, new Ability(ID.AI_TURN, "", CATEGORY.ATTACK, Ability.TARGET_TYPE.SELF, 
					EFFECT.BUFF, 1, 0, "AI placeholder to select an ability", SpriteSheet.ANIMATION.WALK));
			
			// Generic attacks
			abilities.put(ID.ATTACK, new Ability(ID.ATTACK, "Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.ENEMY, 
					EFFECT.ATTACK, 1000, 25, "A basic attack, average in every way", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.QUICK_ATTACK, new Ability(ID.QUICK_ATTACK, "Quick Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.ENEMY,
					EFFECT.ATTACK, 800, 20, "A quick jab", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.HEAVY_ATTACK, new Ability(ID.HEAVY_ATTACK, "Heavy Strike", CATEGORY.ATTACK, Ability.TARGET_TYPE.ENEMY,
					EFFECT.ATTACK, 1500, 35, "A slow, powerful attack", SpriteSheet.ANIMATION.MELEE));
			abilities.put(ID.WEAK_ATTACK, new Ability(ID.WEAK_ATTACK, "Weak Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.ENEMY,
					EFFECT.ATTACK, 1000, 12, "An untrained, weak attack", SpriteSheet.ANIMATION.MELEE));
			
			// Generic status effect attacks 
			Ability guardAttack = new Ability(ID.GUARD_ATTACK, "Guard Attack", CATEGORY.ATTACK, Ability.TARGET_TYPE.ENEMY,
					EFFECT.DEBUFF, 1000, 23, "A basic attack used by guards. It disorients the target, making it difficult to attack effectively",
					SpriteSheet.ANIMATION.MELEE);
			guardAttack.statusEffects.add(new StatusEffect(StatusEffect.ID.DAMAGE_DOWN, 1500));
			abilities.put(ID.GUARD_ATTACK, guardAttack);
			Ability ignite = new Ability(ID.BURNING_ATTACK, "Ignite", CATEGORY.ATTACK, TARGET_TYPE.ENEMY, 
					EFFECT.DEBUFF, 1200, 25, "Set the enemy ablaze", ANIMATION.RANGE);
			ignite.statusEffects.add(new StatusEffect(StatusEffect.ID.BURNING, 8000));
			abilities.put(ID.BURNING_ATTACK, ignite);
			Ability pinningAttack = new Ability(ID.PINNING_ATTACK, "Pinning Shot", CATEGORY.ATTACK, TARGET_TYPE.ENEMY,
					EFFECT.DEBUFF, 1400, 30, "An attack aimed at the legs. It is designed to prevent movement", ANIMATION.RANGE);
			pinningAttack.statusEffects.add(new StatusEffect(StatusEffect.ID.BIND, 2500));
			abilities.put(ID.PINNING_ATTACK, pinningAttack);
			
			// Weapon-specific attacks
			Ability shieldBash = new Ability(ID.SHIELD_BASH, "Shield Bash", CATEGORY.SHIELD, TARGET_TYPE.ENEMY, TARGET_TYPE.ENEMY,
					EFFECT.DEBUFF, 1400, 25, 1, 0, "Briefly disorients the target", SpriteSheet.ANIMATION.MELEE, 0, 500);
			shieldBash.statusEffects.add(new StatusEffect(StatusEffect.ID.SLOW, 10000));
			abilities.put(ID.SHIELD_BASH, shieldBash);
			Ability barrage = new Ability(ID.BARRAGE, "Barrage", CATEGORY.SHOT, TARGET_TYPE.GROUND, TARGET_TYPE.ENEMY, 
					EFFECT.ATTACK, 1500, 20, 1, 2, "Pepper an area with shots", ANIMATION.RANGE);
			abilities.put(ID.BARRAGE, barrage);
			Ability sweep = new Ability(ID.SWEEPING_STRIKE, "Sweep", CATEGORY.STRIKE, TARGET_TYPE.SELF, TARGET_TYPE.ENEMY,
					EFFECT.ATTACK, 2000, 28, 0, 1, "Twirl about, attacking everything nearby", ANIMATION.SPIN);
			abilities.put(ID.SWEEPING_STRIKE, sweep);
			Ability throwObject = new Ability(ID.THROW, "Throw", CATEGORY.STRIKE, TARGET_TYPE.ENEMY, EFFECT.ATTACK, 1000,
					18, 6, 0, "Hurl your weapon at a distant target (it comes back, obviously...)", ANIMATION.MELEE);
			abilities.put(ID.THROW, throwObject);
			//TODO: force fall down ability, followed by get up ability?
			Ability knockdown = new Ability(ID.KNOCKDOWN_STRIKE, "Knockdown", CATEGORY.STRIKE, TARGET_TYPE.ENEMY, TARGET_TYPE.ENEMY,
					EFFECT.DEBUFF, 2000, 10, 1, 0, "A forceful, ramming attack that will bring your oponent to his knees",
					ANIMATION.MELEE, 0, 800);
			knockdown.statusEffects.add(new StatusEffect(StatusEffect.ID.KNOCKDOWN, 800));
			abilities.put(ID.KNOCKDOWN_STRIKE, knockdown);

			// Spells
			Ability vigor = new Ability(ID.VIGOR, "Vigor", CATEGORY.SPELL, TARGET_TYPE.SELF, EFFECT.BUFF, 
					1000, -10, "Gradually recover health over a long period of time", ANIMATION.CAST);
			vigor.statusEffects.add(new StatusEffect(StatusEffect.ID.REGEN, 20000));
			abilities.put(ID.VIGOR, vigor);
			
			// Skills
			//TODO: Snare needs to create a tall object? Need enemies to move into it? Just next to it? hidden?
			Ability snare = new Ability(ID.SNARE, "Snare", CATEGORY.SKILL, TARGET_TYPE.GROUND, EFFECT.ATTACK, 
					8000, 25, "Prepare a trap", SpriteSheet.ANIMATION.KNEEL);
			abilities.put(ID.SNARE, snare);
			Ability challenge = new Ability(ID.CHALLENGE, "Challenge", CATEGORY.SKILL, TARGET_TYPE.ENEMY, EFFECT.ATTACK, 
					300, 0, 10, 0, "'Discourage' your opponent from attacking your allies.", ANIMATION.POINT);
			challenge.statusEffects.add(new StatusEffect(StatusEffect.ID.MURDEROUS_INTENT, 10000));
			abilities.put(ID.CHALLENGE, challenge);
			
			// Movement & Time killers
			abilities.put(ID.MOVE, new Ability(ID.MOVE, "Move", CATEGORY.MOVE, TARGET_TYPE.SELF, TARGET_TYPE.SELF, 
					EFFECT.MOVE, 500, 0, 1, 0, "Walk, while keeping your guard up", SpriteSheet.ANIMATION.WALK, 1, 0));
			abilities.put(ID.DELAY, new Ability(ID.DELAY, "Delay", CATEGORY.SKILL, TARGET_TYPE.SELF, EFFECT.MOVE, 
					1000, 0, "Wait", SpriteSheet.ANIMATION.WALK));
			abilities.put(ID.WATCH, new Ability(ID.WATCH, "Watch", CATEGORY.SKILL, TARGET_TYPE.ALL, EFFECT.MOVE, 
					5000, 0, 16, 0, "Watch", ANIMATION.WALK));
			abilities.put(ID.DEATH, new Ability(ID.DEATH, "PASS OUT", CATEGORY.SKILL, TARGET_TYPE.DEAD, EFFECT.MOVE, 
					1000, 0, "Lose the will to fight on", ANIMATION.DEATH));
		}
		
		public static Ability getAbility(ID id) {
			if (!abilitiesInitialized) {
				initAbilities();
			}
			return abilities.get(id);
		}
	}
}
