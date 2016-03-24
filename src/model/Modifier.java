package model;

import java.util.HashMap;
import java.util.Map;

public class Modifier {
	public static enum FRACTIONAL_BONUS {
		SPEED_MODIFIER_STRIKE, SPEED_MODIFIER_SHOT, SPEED_MODIFIER_SPELL, SPEED_MODIFIER_MOVE, 
		OUTGOING_DAMAGE_MODIFIER_STRIKE, OUTGOING_DAMAGE_MODIFIER_SHOT, OUTGOING_DAMAGE_MODIFIER_SPELL,
		ACCURACY_MODIFIER_STRIKE, ACCURACY_MODIFIER_SHOT, ACCURACY_MODIFIER_SPELL,
		INCOMING_DAMAGE_MODIFIER_STRIKE, INCOMING_DAMAGE_MODIFIER_SHOT, INCOMING_DAMAGE_MODIFIER_SPELL,
		EVASION_MODIFIER_STRIKE, EVASION_MODIFIER_SHOT, EVASION_MODIFIER_SPELL,
		CHANCE_TO_SUCCEED_STRIKE, CHANCE_TO_SUCCEED_SHOT, CHANCE_TO_SUCCEED_SPELL, CHANCE_TO_SUCCEED_MOVE
	};
	public static enum FLAT_BONUS {
		HP_DAMAGE_PER_SECOND, HP_HEALED_PER_SECOND, MP_DAMAGE_PER_SECOND, MP_HEALED_PER_SECOND, HP_SHIELD, MP_SHIELD, RANGE
	};

	private final Map<FRACTIONAL_BONUS, Double> fractionalBonuses = new HashMap<FRACTIONAL_BONUS, Double>();
	private final Map<FLAT_BONUS, Integer> flatBonuses = new HashMap<FLAT_BONUS, Integer>();
	
	/**
	 * 
	 * @param bonuses sequence of enum, value
	 */
	public Modifier(Object... bonuses) {
		for (int i = 0; i < bonuses.length; i+=2) {
			if (bonuses[i] instanceof FRACTIONAL_BONUS) {
				if (bonuses[i + 1] instanceof Double) {
					fractionalBonuses.put((FRACTIONAL_BONUS)bonuses[i], (Double)bonuses[i+1]);
				} else {
					System.err.println("BaseEffect(constructor) ~ " + bonuses[i+1] + " is not Double, ignoring");
				}
			} else if (bonuses[i] instanceof FLAT_BONUS) {
				if (bonuses[i + 1] instanceof Integer) {
					flatBonuses.put((FLAT_BONUS)bonuses[i], (Integer)bonuses[i+1]);
				} else {
					System.err.println("BaseEffect(constructor) ~ " + bonuses[i+1] + " is not Integer, ignoring");
				}
			} else {
				System.err.println("BaseEffect(constructor) ~ " + bonuses[i] + " is not FRACTIONAL_BONUS or FLAT_BONUS, ignoring");
			}
		}
	}
	
	public Modifier(Modifier modifier) {
		this(new Modifier[] {modifier});
	}
	
	public Modifier(Modifier[] modifiers) {
		for (Modifier modifier : modifiers) {
			for (FRACTIONAL_BONUS key : modifier.fractionalBonuses.keySet()) {
				fractionalBonuses.put(key, getBonus(key) * modifier.getBonus(key));
			}
			for (FLAT_BONUS key : modifier.flatBonuses.keySet()) {
				flatBonuses.put(key, getBonus(key) * modifier.getBonus(key));
			}
		}
	}
	
	public void addModifier(Modifier modifier) {
		for (FRACTIONAL_BONUS key : modifier.fractionalBonuses.keySet()) {
			fractionalBonuses.put(key, getBonus(key) * modifier.getBonus(key));
		}
		for (FLAT_BONUS key : modifier.flatBonuses.keySet()) {
			flatBonuses.put(key, getBonus(key) * modifier.getBonus(key));
		}
	}
	
	public void setBonus(FRACTIONAL_BONUS bonus, double value) {
		fractionalBonuses.put(bonus, value);
	}
	
	public void setBonus(FLAT_BONUS bonus, int value) {
		flatBonuses.put(bonus, value);
	}
	
	public double getBonus(FRACTIONAL_BONUS bonus) {
		Double value = fractionalBonuses.get(bonus);
		if (value == null) {
			return 1.0;
		} else {
			return value.doubleValue();
		}
	}
	
	public int getBonus(FLAT_BONUS bonus) {
		Integer value = flatBonuses.get(bonus);
		if (value == null) {
			return 0;
		} else {
			return value.intValue();
		}
	}
	
	public int damageHpShield(int damage) {
		Integer value = flatBonuses.get(FLAT_BONUS.HP_SHIELD);
		if (value == null) {
			return damage;
		} else {
			int newValue = value - damage;
			int remainingDamage = damage - value;
			if (newValue <= 0) {
				flatBonuses.remove(FLAT_BONUS.HP_SHIELD);
			} else {
				flatBonuses.put(FLAT_BONUS.HP_SHIELD, newValue);
			}
			return remainingDamage < 0 ? 0 : remainingDamage;
		}
	}
}
