package model;

import java.awt.Dimension;

import javax.swing.JLabel;

import view.SpriteSheet;

public class Ability {

	public static final Ability DELAY = new Ability("Delay", OUTCOME.PERSONAL, 1000, 0, "", SpriteSheet.ANIMATION.WALK);
	public static enum OUTCOME { HELPFUL, HARMFUL, PERSONAL, UBIQUITOUS };
	
	private String name;
	private OUTCOME outcome; 
	private int delay;
	private int damage;
	private String special;
	private SpriteSheet.ANIMATION stance;
	private JLabel label;
	
	private int delayOpponent = 0;
	
	public Ability(String name, OUTCOME outcome, int delay, int damage, String special, SpriteSheet.ANIMATION stance) {
		this.name = name;
		this.outcome = outcome;
		this.delay = delay;
		this.damage = damage;
		this.special = special;
		this.stance = stance;
		label = new JLabel(name);
		label.setSize(100, 50);
		label.setPreferredSize(new Dimension(100, 50));
	}
	
	public String getName() {
		return name;
	}
	public OUTCOME getOutcome() {
		return outcome;
	}
	public int getDelay() {
		return delay;
	}
	public int getDamage() {
		return damage;
	}
	public String getSpecial() {
		return special;
	}
	public JLabel getLabel() {
		return label;
	}
	@Override
	public String toString() {
		return name;
	}

	public SpriteSheet.ANIMATION getStance() {
		return stance;
	}

	public int getDelayOpponent() {
		return delayOpponent;
	}

	public void setDelayOpponent(int delayOpponent) {
		this.delayOpponent = delayOpponent;
	}
}
