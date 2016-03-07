package controller;

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
import model.AppliedStatusEffect;
import model.ITargetable;
import model.Position;
import model.ReadiedAction;
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
	private static final int ABILITY_DELAY = 1000;
	private static final int READ_CHECK_DELAY = 100;
	
	private static final LinkedList<ReadiedAction> actionQueue = new LinkedList<ReadiedAction>();
	private static final Map<Unit,Integer> lastScheduledTimes = new HashMap<Unit, Integer>();
	private static final Map<Unit,Integer> completionTimes = new HashMap<Unit, Integer>();
	private static final Thread playNextAction = new Thread() {
		@Override
		public void run() {
			while(true) {
				try {
					if (!pause && !actionQueue.isEmpty() && actionQueue.peek().getStartTime() <= getMostReadyCombatantReadyness()) {
						performNextAction();
						Thread.sleep(ABILITY_DELAY);
						//TODO: always sleep READ_CHECK_DELAY and set variable for active animation? not pause, would conflict
					}
					else {
						Thread.sleep(READ_CHECK_DELAY);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private static int battleDuration = 0;
	private static boolean pause = true;
	
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
	
	public static void removeCombatant(Unit unit) {
		for (int i = actionQueue.size() - 1; i >= 0; i--) {
			ReadiedAction action = actionQueue.get(i);
			if (action.getSource() == unit) {
				actionQueue.remove(action);
				continue;
			}
			//TODO if getTargets() includes this unit, react accordingly 
		}
		lastScheduledTimes.remove(unit);
		completionTimes.remove(unit);
		
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
	
	public static ReadiedAction queueAction(Ability ability, Unit source, ITargetable target) {
		return queueAction(ability, source, new ITargetable[] {target});
	}
	
	public static ReadiedAction queueAction(Ability ability, Unit source, ITargetable[] targets) {
		lastScheduledTimes.put(source, completionTimes.get(source));
		ReadiedAction action = new ReadiedAction(ability, source, targets, completionTimes.get(source));
		actionQueue.add(action);
		completionTimes.put(source, action.getEndTime());
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has prepared " + ability + " for " + action.getTargetsDescription());
		Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		return action;
	}

	public static Unit getMostReadyCombatant() {
		Unit unit = null;
		int unitBusyness = Integer.MAX_VALUE;
		Iterator<Unit> combatants = completionTimes.keySet().iterator();
		while (combatants.hasNext()) {
			Unit combatant = combatants.next();
			if (completionTimes.get(combatant) < unitBusyness) {
				unit = combatant;
				unitBusyness = completionTimes.get(combatant);
			}
		}
		return unit;
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
		ReadiedAction nextAction = actionQueue.peek();
		battleDuration = nextAction.getStartTime();
		Unit source = nextAction.getSource();
		ITargetable[] targets = nextAction.getTargets();
		if (targetReachable(nextAction)) {
			actionQueue.poll(); //remove from the queue
			Ability ability = nextAction.getAbility();
			source.tickStatusEffects(ability.getDelay());
			System.out.println(source + "(" + battleDuration + ") uses " + ability + " on " + nextAction.getTargetsDescription());
			source.face(targets[0]);
			source.animate(ability.getStance(), ABILITY_DELAY * 9 / 10, true, ability.getMoveDistance());
			for (ITargetable target : targets) {
				if (target instanceof TallObject) {
					TallObject targetObject = (TallObject) target;
					targetObject.damage(ability.getDamage());
					if (targetObject instanceof Unit) {
						Unit targetUnit = (Unit) targetObject;
						if (ability.getStatusEffect() != null)
							targetUnit.addStatusEffect(new AppliedStatusEffect(ability.getStatusEffect()));
						delay(targetUnit, ability.getDelayOpponent());		
					}
				}
			}			
		} else {
			Ability move = Ability.MOVE;
			source.tickStatusEffects(move.getDelay());
			Position moveTo = getMoveTowards(source, targets[0]);
			System.out.println(source + "(" + battleDuration + ") moves to " + moveTo);
			source.face(moveTo);
			source.animate(move.getStance(), ABILITY_DELAY * 9 / 10, true, move.getMoveDistance());
			
			//delay source appropriately
			lastScheduledTimes.put(source, lastScheduledTimes.get(source) + move.getDelay());
			completionTimes.put(source, completionTimes.get(source) + move.getDelay());
			for (int i = actionQueue.size() - 1; i >= 0; i--) {
				ReadiedAction action = actionQueue.get(i);
				if (source.equals(action.getSource())) {
					action.delay(move.getDelay());
				}
			}
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	private static boolean targetReachable(ReadiedAction action) {
		Position sourcePos = action.getSource().getPos();
		Position targetPos = action.getTargets()[0].getPos();
		int range = action.getAbility().getRange();
		return sourcePos.getDistanceTo(targetPos) <= range;
	}
	
	private static Position getMoveTowards(Unit source, ITargetable target) {
		//TODO: perform actual pathing, return first step
		Position sourcePos = source.getPos();
		Position targetPos = target.getPos();
		// Look for a beneficial horizontal move
		if (sourcePos.getX() < targetPos.getX()) {
			Position firstStep = new Position(sourcePos.getX() + 1, sourcePos.getY());
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		} else if (sourcePos.getX() > targetPos.getX()) {
			Position firstStep = new Position(sourcePos.getX() - 1, sourcePos.getY());
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		}
		// Look for a beneficial vertical move
		if (sourcePos.getY() < targetPos.getY()) {
			Position firstStep = new Position(sourcePos.getX(), sourcePos.getY() + 1);
			if (World.getTallObject(firstStep) == null) {
				return firstStep;
			}
		} else if (sourcePos.getY() > targetPos.getY()) {
			Position firstStep = new Position(sourcePos.getX(), sourcePos.getY() - 1);
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
