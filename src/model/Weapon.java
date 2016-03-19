package model;

import java.util.HashMap;
import java.util.Map;

import model.Modifier.FLAT_BONUS;
import model.Modifier.FRACTIONAL_BONUS;

public class Weapon {
	public static enum ID { BOW, SPEAR, MACE, DAGGER, SPEAR_AND_SHIELD, UNARMED };
	private static final Map<ID, Weapon> weapons = new HashMap<ID, Weapon>();
	private static boolean weaponsInitialized = false;
	
	private ID id;
	private Modifier modifier;
	
	private Weapon(ID id, Modifier modifier) {
		this.id = id;
		this.modifier = modifier;
	}
	
	public static Weapon getWeapon(ID id) {
		if (!weaponsInitialized) {
			initWeapons();
		}
		return weapons.get(id);
	}
	
	public ID getId() {
		return id;
	}
	public Modifier getModifier() {
		return modifier;
	}
		
	private static void initWeapons() {
		weaponsInitialized = true;
		weapons.put(ID.BOW, new Weapon(ID.BOW, new Modifier(
				FLAT_BONUS.RANGE, 7,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.8,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.5
				)));
		weapons.put(ID.DAGGER, new Weapon(ID.DAGGER, new Modifier(
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.5,
				FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE, 0.55
				)));
		weapons.put(ID.MACE, new Weapon(ID.MACE, new Modifier(
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 1.3,
				FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE, 1.3
				)));
		weapons.put(ID.SPEAR, new Weapon(ID.SPEAR, new Modifier(
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 1.0,
				FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE, 1.0
				)));
		weapons.put(ID.SPEAR_AND_SHIELD, new Weapon(ID.SPEAR_AND_SHIELD, new Modifier(
				FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE, 0.8,
				FRACTIONAL_BONUS.SPEED_MODIFIER_SHOT, 0.8,
				FRACTIONAL_BONUS.SPEED_MODIFIER_SPELL, 0.5,
				FRACTIONAL_BONUS.SPEED_MODIFIER_MOVE, 0.8,
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 0.8,
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT, 0.8
				)));
		weapons.put(ID.UNARMED, new Weapon(ID.UNARMED, new Modifier(
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 1.1,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.5,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.5
				)));
	}
}