package model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import controller.plot.Plot;

public class StatusEffect {
	// TODO: apply status effects too all relevant actions
	// TODO: use sprite sheet
	public static enum STATUS_TYPE {
		BENEFIT, DETRIMENT, NEUTRAL
	};

	private String name;
	private BufferedImage icon;
	private int duration;

	// regeneration / degeneration
	private int hpDamagePerSecond = 0;
	private int hpHealingPerSecond = 0;
	private int mpDamagePerSecond = 0;
	private int mpHealingPerSecond = 0;

	// temporary bonus stats
	private int hpShield = 0;
	private int mpShield = 0;

	// speed modifiers
	private double speedModifierAttack = 1.0;
	private double speedModifierCast = 1.0;
	private double speedModifierMove = 1.0;

	// outgoing effect modifiers
	private double outgoingDamageModifierAttack = 1.0;
	private double outgoingDamageModifierCast = 1.0;
	private double outgoingAccuracyModifierAttack = 1.0;
	private double outgoingAccuracyModifierCast = 1.0;

	// incoming effect modifiers
	private double incomingDamageModifierAttack = 1.0;
	private double incomingDamageModifierCast = 1.0;
	private double incomingAccuracyModifierAttack = 1.0;
	private double incomingAccuracyModifierCast = 1.0;

	// action prevention
	private double chanceToFailAttack = 0.0;
	private double chanceToFailCast = 0.0;
	private double chanceToFailMove = 0.0;
	private boolean preventAttack = false;
	private boolean preventCast = false;
	private boolean preventMove = false;

	private StatusEffect(String name, BufferedImage icon, int duration) {
		this.name = name;
		this.icon = icon;
		this.duration = duration;
	}

	public static StatusEffect get(String name) {
		return StatusEffectFactory.getStatusEffect(name);
	}

	public String getName() {
		return name;
	}

	public BufferedImage getIcon() {
		return icon;
	}

	public int getDuration() {
		return duration;
	}

	public int getHpDamagePerSecond() {
		return hpDamagePerSecond;
	}

	public int getHpHealingPerSecond() {
		return hpHealingPerSecond;
	}

	public int getMpDamagePerSecond() {
		return mpDamagePerSecond;
	}

	public int getMpHealingPerSecond() {
		return mpHealingPerSecond;
	}

	public int getHpShield() {
		return hpShield;
	}

	public int getMpShield() {
		return mpShield;
	}

	public double getSpeedModifierAttack() {
		return speedModifierAttack;
	}

	public double getSpeedModifierCast() {
		return speedModifierCast;
	}

	public double getSpeedModifierMove() {
		return speedModifierMove;
	}

	public double getOutgoingDamageModifierAttack() {
		return outgoingDamageModifierAttack;
	}

	public double getOutgoingDamageModifierCast() {
		return outgoingDamageModifierCast;
	}

	public double getOutgoingAccuracyModifierAttack() {
		return outgoingAccuracyModifierAttack;
	}

	public double getOutgoingAccuracyModifierCast() {
		return outgoingAccuracyModifierCast;
	}

	public double getIncomingDamageModifierAttack() {
		return incomingDamageModifierAttack;
	}

	public double getIncomingDamageModifierCast() {
		return incomingDamageModifierCast;
	}

	public double getIncomingAccuracyModifierAttack() {
		return incomingAccuracyModifierAttack;
	}

	public double getIncomingAccuracyModifierCast() {
		return incomingAccuracyModifierCast;
	}

	public double getChanceToFailAttack() {
		return chanceToFailAttack;
	}

	public double getChanceToFailCast() {
		return chanceToFailCast;
	}

	public double getChanceToFailMove() {
		return chanceToFailMove;
	}

	public boolean isPreventAttack() {
		return preventAttack;
	}

	public boolean isPreventCast() {
		return preventCast;
	}

	public boolean isPreventMove() {
		return preventMove;
	}

	static class StatusEffectFactory {
		private static Map<String, StatusEffect> statusEffects = new HashMap<String, StatusEffect>();
		private static boolean statusEffectsInitialized = false;

		private static void initStatusEffects() {
			statusEffectsInitialized = true;
			BufferedImage icon = null;
			try {
				icon = ImageIO.read(Plot.class
						.getResource("/resource/img/spriteSheet/snail.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			StatusEffect slow = new StatusEffect("Slow", icon, 3000);
			slow.speedModifierAttack = 1.3;
			slow.speedModifierCast = 1.3;
			slow.speedModifierMove = 1.5;

			statusEffects.put("Slow", slow);
		}

		public static StatusEffect getStatusEffect(String name) {
			if (!statusEffectsInitialized) {
				initStatusEffects();
			}
			return statusEffects.get(name);
		}
	}
}
