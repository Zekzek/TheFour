package model;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import model.Modifier.FLAT_BONUS;
import model.Modifier.FRACTIONAL_BONUS;
import view.SpriteSheet;
import controller.plot.Plot;

public class StatusEffect {
	public static enum ID {
		SLOW(0), BIND(1), HASTE(2), KNOCKDOWN(3), BURNING(4), MURDEROUS_INTENT(5), DAMAGE_DOWN(6);
		private final int index; //used by spriteSheet to select an icon
		ID(int index) {
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
	};

	private static final SpriteSheet STATUS_EFFECT_SHEET = 
			SpriteSheet.getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/statusEffects.png"));
	private static final Map<ID, BaseEffect> baseEffects = new HashMap<ID, BaseEffect>();
	private static boolean baseEffectsInitialized = false;
	
	private int duration;
	private Unit noPenaltyTarget;
	private BaseEffect baseEffect;
		
	public StatusEffect(ID id, int duration) {
		this.baseEffect = getBaseEffect(id);
		this.duration = duration;
	}
	
	public StatusEffect(ID id, int duration, Unit noPenaltyTarget) {
		this.baseEffect = getBaseEffect(id);
		this.duration = duration;
		this.noPenaltyTarget = noPenaltyTarget;
	}
		
	public StatusEffect(StatusEffect statusEffect) {
		this.baseEffect = statusEffect.baseEffect;
		this.duration = statusEffect.duration;
		this.noPenaltyTarget = statusEffect.noPenaltyTarget;
	}

	public void tick(int time) {
		duration -= time;
	}
	
	public boolean isOver() {
		return duration <= 0;
	}

	public ID getId() {
		return baseEffect.id;
	}
	
	public String getName() {
		return baseEffect.name;
	}

	public BufferedImage getIcon() {
		return STATUS_EFFECT_SHEET.getSprite(baseEffect.id);
	}

	public double getBonus(FRACTIONAL_BONUS bonus, ITargetable target) {
		Double value = baseEffect.modifier.getBonus(bonus);
		if ((target != null && target.equals(noPenaltyTarget)) || value == null) {
			return 1.0;
		} else {
			return value.doubleValue();
		}
	}
	
	public double getBonus(FLAT_BONUS bonus, ITargetable target) {
		Integer value = baseEffect.modifier.getBonus(bonus);
		if ((target != null && target.equals(noPenaltyTarget)) || value == null) {
			return 0;
		} else {
			return value.intValue();
		}
	}
	
	public Modifier getModifier() {
		return baseEffect.modifier;
	}

	public int getDuration() {
		return duration;
	}

	public Unit getNoPenaltyTarget() {
		return noPenaltyTarget;
	}
		
	
	private BaseEffect getBaseEffect(ID id) {
		if (!baseEffectsInitialized) {
			initBaseEffects();
		}
		BaseEffect baseEffect = baseEffects.get(id);
		if (baseEffect == null) {
			throw new UnsupportedOperationException("Not implemented yet - BaseEffect::" + id.name());
		}
		return baseEffect;
	}
		
	private void initBaseEffects() {
		baseEffectsInitialized = true;
		baseEffects.put(ID.SLOW, new BaseEffect(ID.SLOW, "Slow", new Modifier(
				FRACTIONAL_BONUS.SPEED_MODIFIER_STRIKE, 1.3,
				FRACTIONAL_BONUS.SPEED_MODIFIER_SHOT, 1.3,
				FRACTIONAL_BONUS.SPEED_MODIFIER_SPELL, 1.15,
				FRACTIONAL_BONUS.SPEED_MODIFIER_MOVE, 1.5)));

		baseEffects.put(ID.BIND, new BaseEffect(ID.BIND, "Bind", new Modifier(
				FRACTIONAL_BONUS.SPEED_MODIFIER_MOVE, 3.0,
				FRACTIONAL_BONUS.CHANCE_TO_SUCCEED_MOVE, 0.3)));
		
		baseEffects.put(ID.BURNING, new BaseEffect(ID.BURNING, "Burning", new Modifier(
				FLAT_BONUS.HP_DAMAGE_PER_SECOND, 3)));

		baseEffects.put(ID.KNOCKDOWN, new BaseEffect(ID.KNOCKDOWN, "Knockdown", new Modifier()));

		baseEffects.put(ID.MURDEROUS_INTENT, new BaseEffect(ID.MURDEROUS_INTENT, "Murderous Intent", new Modifier(
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.5,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.5,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.75,
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_STRIKE, 1.1,
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SHOT, 1.1,
				FRACTIONAL_BONUS.INCOMING_DAMAGE_MODIFIER_SPELL, 1.1)));
		
		baseEffects.put(ID.DAMAGE_DOWN, new BaseEffect(ID.DAMAGE_DOWN, "Weaken", new Modifier(
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_STRIKE, 0.9,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SHOT, 0.9,
				FRACTIONAL_BONUS.OUTGOING_DAMAGE_MODIFIER_SPELL, 0.95)));
	}

	private class BaseEffect {
		private final ID id;
		private final String name;
		private final Modifier modifier;
		
		public BaseEffect(ID id, String name, Modifier modifier) {
			this.id = id;
			this.name = name;
			this.modifier = modifier;
		}
	}
}