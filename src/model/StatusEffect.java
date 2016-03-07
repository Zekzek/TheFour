package model;

import java.awt.image.BufferedImage;

public class StatusEffect {
	//TODO: apply status effects too all relevant actions
	//TODO: use sprite sheet
	public static enum STATUS_TYPE { BENEFIT, DETRIMENT, NEUTRAL };
	private String name;
	private BufferedImage icon;
	private int duration;
	
	// spell OR skill OR item OR move
	// benefit OR detriment OR neutral
	
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
	

	public StatusEffect(String name, BufferedImage icon, int duration) {
		this.name = name;
		this.icon = icon;
		this.duration = duration;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public BufferedImage getIcon() {
		return icon;
	}


	public void setIcon(BufferedImage icon) {
		this.icon = icon;
	}


	public int getDuration() {
		return duration;
	}


	public void setDuration(int duration) {
		this.duration = duration;
	}


	public int getHpDamagePerSecond() {
		return hpDamagePerSecond;
	}


	public void setHpDamagePerSecond(int hpDamagePerSecond) {
		this.hpDamagePerSecond = hpDamagePerSecond;
	}


	public int getHpHealingPerSecond() {
		return hpHealingPerSecond;
	}


	public void setHpHealingPerSecond(int hpHealingPerSecond) {
		this.hpHealingPerSecond = hpHealingPerSecond;
	}


	public int getMpDamagePerSecond() {
		return mpDamagePerSecond;
	}


	public void setMpDamagePerSecond(int mpDamagePerSecond) {
		this.mpDamagePerSecond = mpDamagePerSecond;
	}


	public int getMpHealingPerSecond() {
		return mpHealingPerSecond;
	}


	public void setMpHealingPerSecond(int mpHealingPerSecond) {
		this.mpHealingPerSecond = mpHealingPerSecond;
	}


	public int getHpShield() {
		return hpShield;
	}


	public void setHpShield(int hpShield) {
		this.hpShield = hpShield;
	}


	public int getMpShield() {
		return mpShield;
	}


	public void setMpShield(int mpShield) {
		this.mpShield = mpShield;
	}


	public double getSpeedModifierAttack() {
		return speedModifierAttack;
	}


	public void setSpeedModifierAttack(double speedModifierAttack) {
		this.speedModifierAttack = speedModifierAttack;
	}


	public double getSpeedModifierCast() {
		return speedModifierCast;
	}


	public void setSpeedModifierCast(double speedModifierCast) {
		this.speedModifierCast = speedModifierCast;
	}


	public double getSpeedModifierMove() {
		return speedModifierMove;
	}


	public void setSpeedModifierMove(double speedModifierMove) {
		this.speedModifierMove = speedModifierMove;
	}


	public double getOutgoingDamageModifierAttack() {
		return outgoingDamageModifierAttack;
	}


	public void setOutgoingDamageModifierAttack(double outgoingDamageModifierAttack) {
		this.outgoingDamageModifierAttack = outgoingDamageModifierAttack;
	}


	public double getOutgoingDamageModifierCast() {
		return outgoingDamageModifierCast;
	}


	public void setOutgoingDamageModifierCast(double outgoingDamageModifierCast) {
		this.outgoingDamageModifierCast = outgoingDamageModifierCast;
	}


	public double getOutgoingAccuracyModifierAttack() {
		return outgoingAccuracyModifierAttack;
	}


	public void setOutgoingAccuracyModifierAttack(
			double outgoingAccuracyModifierAttack) {
		this.outgoingAccuracyModifierAttack = outgoingAccuracyModifierAttack;
	}


	public double getOutgoingAccuracyModifierCast() {
		return outgoingAccuracyModifierCast;
	}


	public void setOutgoingAccuracyModifierCast(double outgoingAccuracyModifierCast) {
		this.outgoingAccuracyModifierCast = outgoingAccuracyModifierCast;
	}


	public double getIncomingDamageModifierAttack() {
		return incomingDamageModifierAttack;
	}


	public void setIncomingDamageModifierAttack(double incomingDamageModifierAttack) {
		this.incomingDamageModifierAttack = incomingDamageModifierAttack;
	}


	public double getIncomingDamageModifierCast() {
		return incomingDamageModifierCast;
	}


	public void setIncomingDamageModifierCast(double incomingDamageModifierCast) {
		this.incomingDamageModifierCast = incomingDamageModifierCast;
	}


	public double getIncomingAccuracyModifierAttack() {
		return incomingAccuracyModifierAttack;
	}


	public void setIncomingAccuracyModifierAttack(
			double incomingAccuracyModifierAttack) {
		this.incomingAccuracyModifierAttack = incomingAccuracyModifierAttack;
	}


	public double getIncomingAccuracyModifierCast() {
		return incomingAccuracyModifierCast;
	}


	public void setIncomingAccuracyModifierCast(double incomingAccuracyModifierCast) {
		this.incomingAccuracyModifierCast = incomingAccuracyModifierCast;
	}


	public double getChanceToFailAttack() {
		return chanceToFailAttack;
	}


	public void setChanceToFailAttack(double chanceToFailAttack) {
		this.chanceToFailAttack = chanceToFailAttack;
	}


	public double getChanceToFailCast() {
		return chanceToFailCast;
	}


	public void setChanceToFailCast(double chanceToFailCast) {
		this.chanceToFailCast = chanceToFailCast;
	}


	public double getChanceToFailMove() {
		return chanceToFailMove;
	}


	public void setChanceToFailMove(double chanceToFailMove) {
		this.chanceToFailMove = chanceToFailMove;
	}


	public boolean isPreventAttack() {
		return preventAttack;
	}


	public void setPreventAttack(boolean preventAttack) {
		this.preventAttack = preventAttack;
	}


	public boolean isPreventCast() {
		return preventCast;
	}


	public void setPreventCast(boolean preventCast) {
		this.preventCast = preventCast;
	}


	public boolean isPreventMove() {
		return preventMove;
	}


	public void setPreventMove(boolean preventMove) {
		this.preventMove = preventMove;
	}
	
}
