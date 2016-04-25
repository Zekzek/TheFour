package model;

import java.util.Iterator;
import java.util.Random;

import model.Modifier.FLAT_BONUS;
import controller.BattleQueue;

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
	private long startTime;
	private BattleQueue battleQueue;
	
	public ReadiedAction(Ability ability, Unit source, ITargetable target, long startTime) {
		super();
		this.ability = ability;
		this.source = source;
		this.target = target;
		this.startTime = startTime;
	}
	
	public void delay(int delay) {
		startTime += delay;
	}
	
	public void activate(World world, BattleQueue battleQueue) {
		System.out.println(source + " uses " + ability + " on " + target);
		this.battleQueue = battleQueue;
		if (!battleQueue.isInBattle()) {
			source.heal(source.getMaxHp() / 10 * ability.getDelay() / 1000);		
		}
		activateAtStart();
		source.animate(ability, world);
		SCHEDULE_ACTIVATE_AT_MID.start();
		SCHEDULE_ACTIVATE_AT_END.start();
	}
	
	private void activateAtStart() {
		battleQueue.delay(source, ability.calcAdditionalDelay(source.getModifier()));
		source.tickStatusEffects(ability.getDelay());
		source.face(target);
		source.damage(source.getStatusEffectModifier(FLAT_BONUS.HP_DAMAGE_PER_SECOND, target) * ability.getDelay() / 1000);
		source.heal(source.getStatusEffectModifier(FLAT_BONUS.HP_HEALED_PER_SECOND, target) * ability.getDelay() / 1000);
	}
	
	private void activateAtMid() {
		//TODO: use calcSuccessChance
//		if (ability.getAreaOfEffectDistance() > 0) {
//			int distance = ability.getAreaOfEffectDistance();
//			GridPosition pos = target.getPos();
//			GridRectangle rect = new GridRectangle(pos.getX() - distance, pos.getY() - distance, 
//					2 * distance + 1, 2 * distance + 1);
//			ArrayList<Unit> targets = World.getSortedContentsWithin(rect, Unit.class);
//			for (Unit unit : targets) {
//				if (affectsTarget(unit)) {
//					effectTargetWith(source, unit, ability);
//				}
//			}
//		} else {
			effectTargetWith(source, target, ability, battleQueue);
//		}
		if (doAtMid != null) doAtMid.run();
	}
	
	private void activateAtEnd() {
		if (doAtEnd != null) doAtEnd.run();
		battleQueue.setActionComplete();
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
	
	private void effectTargetWith(Unit source, ITargetable target, Ability ability, BattleQueue battleQueue) {
		if (target instanceof GameObject) {
			GameObject targetObject = (GameObject) target;
			if (RAND.nextDouble() <= ability.calcChanceToHit(source.getModifier(), targetObject.getModifier())) {
				targetObject.damage(ability.calcDamage(source.getModifier(), targetObject.getModifier()));
				if (targetObject.isAlive() && targetObject instanceof Unit) {
					Unit targetUnit = (Unit) targetObject;
					Iterator<StatusEffect> appliedStatusEffects = ability.getStatusToTargetEffectIterator();
					while (appliedStatusEffects.hasNext()) {
						targetUnit.addStatusEffect(new StatusEffect(appliedStatusEffects.next()));
					}
					battleQueue.delay(targetUnit, ability.getDelayOpponent());
				}
			}	
		}
	}
	
	public boolean sourceAbilityTargetEquals(ReadiedAction otherAction) {
		return source.equals(otherAction.source) && ability.equals(otherAction.ability) && target.equals(otherAction.target);
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
	
	public long getStartTime() {
		return startTime;
	}

	public void addDoAtMid(Runnable doAtMid) {
		if (this.doAtMid == null) {
			this.doAtMid = doAtMid;
		}
		else {
			final Runnable oldDoAtMid = this.doAtMid;
			this.doAtMid = new Runnable() {
				@Override
				public void run() {
					oldDoAtMid.run();
					doAtMid.run();
				}
			};
		}
	}

	public void addDoAtEnd(Runnable doAtEnd) {
		if (this.doAtEnd == null) {
			this.doAtEnd = doAtEnd;
		}
		else {
			final Runnable oldDoAtEnd = this.doAtEnd;
			this.doAtEnd = new Runnable() {
				@Override
				public void run() {
					oldDoAtEnd.run();
					doAtEnd.run();
				}
			};
		}
	}
}
