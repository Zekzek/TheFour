package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.Ability;
import model.GridPosition;
import model.GridRectangle;
import model.GroundTarget;
import model.ITargetable;
import model.ReadiedAction;
import model.StatusEffect;
import model.TallObject;
import model.Unit;
import model.World;
import view.GameFrame;

public class BattleQueue {
	private static final Comparator<ReadiedAction> SOONEST_READIED_ACTION = new Comparator<ReadiedAction>(){
		@Override
		public int compare(ReadiedAction action1, ReadiedAction action2) {
			return action1.getStartTime() - action2.getStartTime();
		}
	};
	private static final Random RAND = new Random();
	private static final int PERFORM_ACTION_CHECK_DELAY = 100;
	
	private static final LinkedList<ReadiedAction> actionQueue = new LinkedList<ReadiedAction>();
	private static final Map<Unit,Integer> lastScheduledTimes = new HashMap<Unit, Integer>();
	private static final Map<Unit,Integer> completionTimes = new HashMap<Unit, Integer>();
	private static final Thread playNextAction = new Thread() {
		@Override
		public void run() {
			while(true) {
				try {
					if (!pause && !performingAction && !actionQueue.isEmpty()
							&& actionQueue.peek().getStartTime() <= getMostReadyCombatantReadyness()) {
						performNextAction();
					}
					Thread.sleep(PERFORM_ACTION_CHECK_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private static int battleDuration = 0;
	private static boolean pause = true;
	private static boolean performingAction = false;
	
	private static Set<BattleListenerInterface> battleListeners = new HashSet<BattleListenerInterface>();
	private static Set<Unit.TEAM> activeTeams = new HashSet<Unit.TEAM>();
	
	public static void startPlayingActions() {
		if (!playNextAction.isAlive()) {
			playNextAction.setDaemon(true);
			playNextAction.start();
		}
		GameFrame.updateMenu();
	}
	
	public static void pauseBattle() {
		pause = true;
	}
	
	public static void resumeBattle() {
		pause = false;
	}
	
	/**
	 * Add all combatants in the iterator (Unit.TEAM.NONCOMBATANT are not added. Use BattleQueue.addCombatant directly)
	 * @param units units to add
	 */
	public static void addCombatants(Iterator<Unit> units) {
		while (units.hasNext()) {
			Unit unit = units.next();
			if (unit.getTeam() != Unit.TEAM.NONCOMBATANT) {
				BattleQueue.addCombatant(unit);
			}
        }
    }
	
	public static void addCombatant(Unit unit) {
		lastScheduledTimes.put(unit, battleDuration);
		completionTimes.put(unit, battleDuration);
		activeTeams.add(unit.getTeam());
	}
	
	public static void removeCombatant(Unit unit, Ability.ID exitAbility) {
		if (lastScheduledTimes.get(unit) == null) {
			return;
		}
		Iterator<ReadiedAction> actions = actionQueue.iterator();
		while (actions.hasNext()) {
			if (unit.equals(actions.next().getSource())) {
				actions.remove();
			}
		}
		if (exitAbility != null) {
			insertFirstAction(Ability.get(Ability.ID.DEATH), unit, unit);
		}
		lastScheduledTimes.remove(unit);
		completionTimes.remove(unit);
		
		//TODO: wait until after exitAbility completes to call unitDefeated, etc
		unitDefeated(unit);
		// Determine if the team was defeated
		Unit.TEAM team = unit.getTeam();
		boolean teamDefeat = true;
		Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
		while (teamDefeat && combatants.hasNext()) {
			teamDefeat = !(combatants.next().getTeam() == team);
		}
		if (teamDefeat) {
			activeTeams.remove(team);
			teamDefeated(team);
		}
	}
	
	public static void addRandomCombatDelays() {
		Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
		while (combatants.hasNext()) {
			int delay = RAND.nextInt(1000);
			Unit combatant = combatants.next();
			lastScheduledTimes.put(combatant, delay);
			completionTimes.put(combatant, delay);
		}
	}
	
	public static void endCombat() {
		lastScheduledTimes.clear();
		completionTimes.clear();
		actionQueue.clear();
		battleDuration = 0;
	}
	
	public static void queueAction(Ability ability, Unit source, ITargetable target) {
		ReadiedAction action = new ReadiedAction(ability, source, target, completionTimes.get(source));
		actionQueue.add(action);
		lastScheduledTimes.put(source, completionTimes.get(source));
		completionTimes.put(source, action.getStartTime() + ability.getDelay());
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has prepared " + ability + " for " + action.getTarget());
		Collections.sort(actionQueue, SOONEST_READIED_ACTION);
	}
	
	public static void insertFirstAction(Ability ability, Unit source, ITargetable target) {
		BattleQueue.delay(source, ability.getDelay());
		ReadiedAction action = new ReadiedAction(ability, source, target, battleDuration);
		actionQueue.add(action);
		lastScheduledTimes.put(source, lastScheduledTimes.get(source) + ability.getDelay());
		completionTimes.put(source, completionTimes.get(source) + ability.getDelay());
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has immediately prepared " + ability + " for " + action.getTarget());
		Collections.sort(actionQueue, SOONEST_READIED_ACTION);
	}
	
	public static void dequeueAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		for (int i = actionQueue.size() - 1; i >= 0; i--) {
			ReadiedAction otherAction = actionQueue.get(i);
			if (otherAction.equals(action)) {
				//remove the requested action
				actionQueue.remove(i);
				break;
			} 
			else if (otherAction.getSource().equals(source)) {
				//move up all future actions
				otherAction.delay(-action.getAbility().getDelay());
			}
		}
		lastScheduledTimes.put(source, lastScheduledTimes.get(source) - ability.getDelay());
		completionTimes.put(source, completionTimes.get(source) - ability.getDelay());
		
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has decided not to use " + ability);
		Collections.sort(actionQueue, SOONEST_READIED_ACTION);
	}

	public static Unit getMostReadyCombatant() {
		Unit mostReadyUnit = null;
		int unitBusyness = Integer.MAX_VALUE;
		Iterator<Unit> combatants = completionTimes.keySet().iterator();
		while (combatants.hasNext()) {
			Unit combatant = combatants.next();
			if (completionTimes.get(combatant) < unitBusyness) {
				mostReadyUnit = combatant;
				unitBusyness = completionTimes.get(combatant);
			}
		}
		return mostReadyUnit;
	}
	
	private static int getMostReadyCombatantReadyness() {
		int unitBusyness = Integer.MAX_VALUE;
		Iterator<Unit> combatants = completionTimes.keySet().iterator();
		while (combatants.hasNext()) {
			Unit combatant = combatants.next();
			if (completionTimes.get(combatant) < unitBusyness) {
				unitBusyness = completionTimes.get(combatant);
			}
		}
		return unitBusyness;
	}
	
	public static void performNextAction() {
		performingAction = true;
		ReadiedAction nextAction = actionQueue.peek();
		battleDuration = nextAction.getStartTime();
		Unit source = nextAction.getSource();
		ITargetable target = nextAction.getTarget();
		Ability ability;
		if (!isValidAction(nextAction)) {
			System.out.println("Warning: " + source + "'s " + nextAction.getAbility() + " is now unusable!");
			dequeueAction(nextAction);
			performNextAction();
			return;
		} else if (!targetReachable(nextAction)) {
			ability = Ability.get(Ability.ID.MOVE);
			GridPosition moveTo = getMoveTowards(source, target);
			BattleQueue.insertFirstAction(ability, source, new GroundTarget(moveTo));
			performNextAction();
			return;
		} else {
			actionQueue.poll(); //remove from the queue
			ability = nextAction.getAbility();
			performAction(source, ability, target);
			
			source.tickStatusEffects(ability.getDelay());
			source.animate(ability.getStance(), ability.getName(), ability.getDelay() * 9 / 10, true, ability.getMoveDistance());
			Thread donePerforming = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(ability.getDelay());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					performingAction = false;
				}
			};
			donePerforming.start();
		}
	}
	
	private static void performAction(Unit source, Ability ability, ITargetable target) {
		System.out.println(source + "(" + battleDuration + ") uses " + ability + " on " + target);
		delayUnit(source, ability.calcAdditionalDelay(source.getModifier()));
		source.face(target);
		//TODO: use calcSuccessChance
		if (ability.getAreaOfEffectDistance() > 0) {
			int distance = ability.getAreaOfEffectDistance();
			GridPosition pos = target.getPos();
			GridRectangle rect = new GridRectangle(pos.getX() - distance, pos.getY() - distance, 
					2 * distance + 1, 2 * distance + 1);
			ArrayList<Unit> targets = World.getSortedContentsWithin(rect, Unit.class);
			for (Unit unit : targets) {
				effectTargetWith(source, unit, ability);
			}
		} else {
			effectTargetWith(source, target, ability);
		}
	}
	
	private static void effectTargetWith(Unit source, ITargetable target, Ability ability) {
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
					delay(targetUnit, ability.getDelayOpponent());
				}
			}
		}
	}
	
	private static void delayUnit(Unit unit, int delay) {
		if (delay != 0) {
			lastScheduledTimes.put(unit, lastScheduledTimes.get(unit) + delay);
			completionTimes.put(unit, completionTimes.get(unit) + delay);
			for (int i = actionQueue.size() - 1; i >= 0; i--) {
				ReadiedAction action = actionQueue.get(i);
				if (unit.equals(action.getSource())) {
					action.delay(delay);
				}
			}
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	private static boolean isValidAction(ReadiedAction action) {
		boolean validity = true;
		ITargetable target = action.getTarget();
		if (action.getAbility().getTargetType() != Ability.TARGET_TYPE.DEAD && target instanceof TallObject) {
			validity = ((TallObject)target).isAlive();
		}
		return validity;
	}
	
	private static boolean targetReachable(ReadiedAction action) {
		GridPosition sourcePos = action.getSource().getPos();
		GridPosition targetPos = action.getTarget().getPos();
		int range = action.getAbility().getRange();
		return sourcePos.getDistanceTo(targetPos) <= range;
	}
	
	private static GridPosition getMoveTowards(Unit source, ITargetable target) {
		//TODO: perform actual pathing, return first step
		GridPosition sourcePos = source.getPos();
		GridPosition targetPos = target.getPos();
		// Look for a beneficial horizontal move
		if (sourcePos.getX() < targetPos.getX()) {
			GridPosition firstStep = new GridPosition(sourcePos.getX() + 1, sourcePos.getY());
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		} else if (sourcePos.getX() > targetPos.getX()) {
			GridPosition firstStep = new GridPosition(sourcePos.getX() - 1, sourcePos.getY());
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		}
		// Look for a beneficial vertical move
		if (sourcePos.getY() < targetPos.getY()) {
			GridPosition firstStep = new GridPosition(sourcePos.getX(), sourcePos.getY() + 1);
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		} else if (sourcePos.getY() > targetPos.getY()) {
			GridPosition firstStep = new GridPosition(sourcePos.getX(), sourcePos.getY() - 1);
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		}
		// Just stay still
		return sourcePos;
	}
	
	private static void delay(Unit unit, int delay) {
		if (delay > 0) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(unit)) {
					action.delay(delay);
				}
			}
			lastScheduledTimes.put(unit, lastScheduledTimes.get(unit) + delay);
			completionTimes.put(unit, completionTimes.get(unit) + delay);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public static void addBattleListener(BattleListenerInterface listener) {
		battleListeners.add(listener);
	}
	
	public static void clearBattleListeners() {
		battleListeners.clear();
	}
	
	private static void unitDefeated(Unit unit) {
		for (BattleListenerInterface listener : battleListeners) {
			listener.onUnitDefeated(unit);
		}
	}
	
	private static void teamDefeated(Unit.TEAM team) {
		for (BattleListenerInterface listener : battleListeners) {
			listener.onTeamDefeated(team);
		}
	}
}
