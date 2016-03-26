package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import controller.BattleQueue;
import model.Modifier.FLAT_BONUS;

public class ReadiedAction {
	private static final Random RAND = new Random();
	private final Ability ability;
	private final Unit source;
	private final ITargetable target;
	private final Thread SCHEDULE_ACTIVATE_AT_MID = new Thread() {
		public void run() {
			try {
				Thread.sleep(ability.calcDelay(source.getModifier()) / 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			activateAtMid();
		}
	};
	private final Thread SCHEDULE_ACTIVATE_AT_END = new Thread() {
		public void run() {
			try {
				Thread.sleep(ability.calcDelay(source.getModifier()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			activateAtEnd();
		}
	};
	private Runnable doAtMid;
	private Runnable doAtEnd;
	private int startTime;
	
	public ReadiedAction(Ability ability, Unit source, ITargetable target, int startTime) {
		super();
		this.ability = ability;
		this.source = source;
		this.target = target;
		this.startTime = startTime;
	}
	
	public void delay(int delay) {
		startTime += delay;
	}
	
	public void activate() {
		System.out.println(source + " uses " + ability + " on " + target);
		activateAtStart();
		SCHEDULE_ACTIVATE_AT_MID.start();
		SCHEDULE_ACTIVATE_AT_END.start();
	}
	
	private void activateAtStart() {
		BattleQueue.delay(source, ability.calcAdditionalDelay(source.getModifier()));
		source.tickStatusEffects(ability.getDelay());
		source.face(target);
		source.animate(ability);
		source.damage(source.getStatusEffectModifier(FLAT_BONUS.HP_DAMAGE_PER_SECOND, target) * ability.getDelay() / 1000);
		source.heal(source.getStatusEffectModifier(FLAT_BONUS.HP_HEALED_PER_SECOND, target) * ability.getDelay() / 1000);
	}
	
	private void activateAtMid() {
		//TODO: use calcSuccessChance
		if (ability.getAreaOfEffectDistance() > 0) {
			int distance = ability.getAreaOfEffectDistance();
			GridPosition pos = target.getPos();
			GridRectangle rect = new GridRectangle(pos.getX() - distance, pos.getY() - distance, 
					2 * distance + 1, 2 * distance + 1);
			ArrayList<Unit> targets = World.getSortedContentsWithin(rect, Unit.class);
			for (Unit unit : targets) {
				if (affectsTarget(unit)) {
					effectTargetWith(source, unit, ability);
				}
			}
		} else {
			effectTargetWith(source, target, ability);
		}
		if (doAtMid != null) doAtMid.run();
	}
	
	private void activateAtEnd() {
		if (doAtEnd != null) doAtEnd.run();
		BattleQueue.setActionComplete();
	}
	
	private boolean affectsTarget(Unit unit) {
		Ability.TARGET_TYPE affectsType = ability.getAffectsTargetType();
		if (affectsType == Ability.TARGET_TYPE.ALL) {
			return true;
		} else if (affectsType == Ability.TARGET_TYPE.ALLY) {
			return source.isAllyOf(unit);
		} else if (affectsType == Ability.TARGET_TYPE.ENEMY) {
			return source.isEnemyOf(unit);
		} else {
			System.err.println("Warning: AoE applied with unexpected affectsTarget type. This may result in unintended functionality");
			return true;
		}
	}
	
	private void effectTargetWith(Unit source, ITargetable target, Ability ability) {
		if (target instanceof TallObject) {
			TallObject targetObject = (TallObject) target;
			if (RAND.nextDouble() <= ability.calcChanceToHit(source.getModifier(), targetObject.getModifier())) {
				targetObject.damage(ability.calcDamage(source.getModifier(), targetObject.getModifier()));
				if (targetObject.isAlive() && targetObject instanceof Unit) {
					Unit targetUnit = (Unit) targetObject;
					Iterator<StatusEffect> appliedStatusEffects = ability.getStatusEffectIterator();
					while (appliedStatusEffects.hasNext()) {
						targetUnit.addStatusEffect(new StatusEffect(appliedStatusEffects.next()));
					}
					BattleQueue.delay(targetUnit, ability.getDelayOpponent());
				}
			}	
		}
	}

	public Ability getAbility() {
		return ability;
	}

	public Unit getSource() {
		return source;
	}

	public ITargetable getTarget() {
		return target;
	}
	
	public int getStartTime() {
		return startTime;
	}

	public void setDoAtMid(Runnable doAtMid) {
		this.doAtMid = doAtMid;
	}

	public void setDoAtEnd(Runnable doAtEnd) {
		this.doAtEnd = doAtEnd;
	}
}
