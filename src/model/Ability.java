package model;

import java.awt.Dimension;

import javax.swing.JLabel;

import view.SpriteSheet;

public class Ability {

	public static final Ability DELAY = new Ability("Delay", TARGET_TYPE.PERSONAL, 1000, 0, 1, "", SpriteSheet.ANIMATION.WALK);
	public static final Ability MOVE = new Ability("MOVE", TARGET_TYPE.PERSONAL, 500, 0, 1, "", SpriteSheet.ANIMATION.WALK, 1);
	public static enum TARGET_TYPE { HELPFUL, HARMFUL, PERSONAL, UBIQUITOUS, GROUND };
	public static enum CATEGORY { MOVE, SKILL, SPELL, ITEM };
	
	private String name;
	private TARGET_TYPE outcome; 
	private int delay;
	private int damage;
	private int range;
	//TODO: change to statusEffect[]?
	private StatusEffect statusEffect;
	private String special;
	private SpriteSheet.ANIMATION stance;
	private JLabel label;
	private int moveDistance;
	
	private int delayOpponent = 0;
	
	public Ability(String name, TARGET_TYPE outcome, int delay, int damage, int range, String special, SpriteSheet.ANIMATION stance) {
		this(name, outcome, delay, damage, range, special, stance, 0);
	}
	
	public Ability(String name, TARGET_TYPE outcome, int delay, int damage, int range, String special, SpriteSheet.ANIMATION stance, int moveDistance) {
		this.name = name;
		this.outcome = outcome;
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

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
	public TARGET_TYPE getOutcome() {
		return outcome;
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

	public void setDelayOpponent(int delayOpponent) {
		this.delayOpponent = delayOpponent;
	}

	public StatusEffect getStatusEffect() {
		return statusEffect;
	}

	public void addStatusEffect(StatusEffect statusEffect) {
		this.statusEffect = statusEffect;
	}
}
