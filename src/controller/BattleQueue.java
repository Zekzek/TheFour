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

import view.GameFrame;
import model.Ability;
import model.ReadiedAction;
import model.Unit;

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
	
	public static ReadiedAction queueAction(Ability ability, Unit source, Unit target) {
		return queueAction(ability, source, new Unit[] {target});
	}
	
	public static ReadiedAction queueAction(Ability ability, Unit source, Unit[] targets) {
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
		ReadiedAction nextAction = actionQueue.poll();
		battleDuration = nextAction.getStartTime();
		System.out.println(nextAction.getSource() + "(" + battleDuration + ") uses " + nextAction.getAbility() + " on " + nextAction.getTargetsDescription());
		nextAction.getSource().animate(nextAction.getAbility().getStance(), ABILITY_DELAY * 9 / 10, true);
		nextAction.getSource().face(nextAction.getTargets()[0]);
		for (Unit target : nextAction.getTargets()) {
			target.damage(nextAction.getAbility().getDamage());
			delay(target, nextAction.getAbility().getDelayOpponent());
		}
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
