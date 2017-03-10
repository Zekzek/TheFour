package model;

import java.util.Iterator;
import java.util.Random;

import controller.ActionRunner;
import model.Modifier.FLAT_BONUS;

public class ReadiedAction {
	private static final Random RAND = new Random();
	private static ActionRunner actionPlayer;
	private static ActionQueue actionQueue;
	
	private final Ability ability;
	private final Unit source;
	private final ITargetable target;
	private int stage = 0;
	private Runnable doAtMid;
	private Runnable doAtEnd;
	private long startTime;
	
	public ReadiedAction(Ability ability, Unit source, ITargetable target, long startTime) {
		this.ability = ability;
		this.source = source;
		this.target = target;
		this.startTime = startTime;
	}
	
	public void delay(int delay) {
		startTime += delay;
	}
	
	public void activate() {
//		System.out.print(source + " uses " + ability + " on " + target + " ~ " + stage);

		if (stage == 0) {
			activateAtStart();
			source.face(target);			
		}
		else if (stage == Unit.ANIMATION_LENGTH / 2) {
			activateAtMid();
		}
		else if (stage == Unit.ANIMATION_LENGTH - 1) {
//			System.out.println(": " + startTime );
			activateAtEnd();
			source.setAnimationFrame( Unit.ANIMATION_LENGTH, ability );
			stage = -1;
			return;
		}
		
//		System.out.print( ": " + startTime + " - " );
		startTime += ability.calcDelay( source.getModifier() ) / Unit.ANIMATION_LENGTH;
//		System.out.println( startTime );
		source.setAnimationFrame( stage, ability );
		stage++;
		
	}
	
	public boolean isComplete() {
		return stage < 0;
	}
	
	private void activateAtStart() {
		source.damage(source.getStatusEffectModifier(FLAT_BONUS.HP_DAMAGE_PER_SECOND, target) * ability.getDelay() / 1000);
		source.tickStatusEffects(ability.getDelay());
		source.heal(source.getStatusEffectModifier(FLAT_BONUS.HP_HEALED_PER_SECOND, target) * ability.getDelay() / 1000);
		
		//Apply any speed modifier to all future actions
		int additionalDelay = ability.calcAdditionalDelay(source.getModifier());
		if (actionQueue != null && additionalDelay != 0)
			actionQueue.delay(source, additionalDelay);
		
		//Fast out-of-combat healing
		if(!source.isInCombat()) {
			source.heal(source.getMaxHp() * ability.getDelay() / 10000);
		}
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
			effectTargetWith(source, target, ability);
//		}
		if (doAtMid != null) doAtMid.run();
	}
	
	private void activateAtEnd() {
		if (doAtEnd != null) doAtEnd.run();
		actionPlayer.completeAction( this );
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
					actionQueue.delay(targetUnit, ability.getDelayOpponent());
				}
			}	
		}
	}
	
	public int calcDelay() {
		return ability.calcDelay(source.getModifier());
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

	public static void setActionPlayer(ActionRunner actionPlayer) {
		ReadiedAction.actionPlayer = actionPlayer;
	}
	
	public static void setActionQueue(ActionQueue actionQueue) {
		ReadiedAction.actionQueue = actionQueue;
	}
}
