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
import model.GridPosition;
import model.GroundTarget;
import model.ITargetable;
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
	private static final Comparator<PathingGridPosition> LOWEST_COST = new Comparator<PathingGridPosition>(){
		@Override
		public int compare(PathingGridPosition pos1, PathingGridPosition pos2) {
			return pos1.getCostEstimate() - pos2.getCostEstimate();
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
					//TODO: when AI using quick actions, AI doesn't trigger (even though its most ready), kicks in after player selects an action 
					synchronized(actionQueue) {
						if (!pause && !performingAction && !actionQueue.isEmpty()
								&& actionQueue.peek().getStartTime() <= getMostReadyCombatantReadyness()) {
							performNextAction();
						}
					}
					Thread.sleep(PERFORM_ACTION_CHECK_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private static BattleQueue me;
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
		synchronized(actionQueue) {
			Iterator<ReadiedAction> actions = actionQueue.iterator();
			while (actions.hasNext()) {
				if (unit.equals(actions.next().getSource())) {
					actions.remove();
				}
			}
		}
		if (exitAbility != null) {
			Ability death = Ability.get(Ability.ID.DEATH);
			insertFirstAction(death, unit, unit, null, new Runnable() {
				@Override
				public void run() {
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
			});
		}
		lastScheduledTimes.remove(unit);
		completionTimes.remove(unit);
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
		synchronized(actionQueue) {	
			actionQueue.clear();
		}
		battleDuration = 0;
	}
	
	public static void queueAction(Ability ability, Unit source, ITargetable target) {
		ReadiedAction action = new ReadiedAction(ability, source, target, completionTimes.get(source));
		lastScheduledTimes.put(source, completionTimes.get(source));
		completionTimes.put(source, action.getStartTime() + ability.getDelay());
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has prepared " + ability + " for " + action.getTarget());
		synchronized(actionQueue) {
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public static void insertFirstAction(Ability ability, Unit source, ITargetable target) {
		insertFirstAction(ability, source, target, null, null);
	}
	
	public static void insertFirstAction(Ability ability, Unit source, ITargetable target, Runnable doAtMid, Runnable doAtEnd) {
		BattleQueue.delay(source, ability.getDelay());
		ReadiedAction action = new ReadiedAction(ability, source, target, battleDuration);
		action.setDoAtMid(doAtMid);
		action.setDoAtEnd(doAtEnd);
		lastScheduledTimes.put(source, lastScheduledTimes.get(source) + ability.getDelay());
		completionTimes.put(source, completionTimes.get(source) + ability.getDelay());
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has immediately prepared " + ability + " for " + action.getTarget());
		synchronized(actionQueue) {
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public static void dequeueAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		lastScheduledTimes.put(source, lastScheduledTimes.get(source) - ability.getDelay());
		completionTimes.put(source, completionTimes.get(source) - ability.getDelay());
		synchronized(actionQueue) {
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
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}		
		System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has decided not to use " + ability);
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
		synchronized(actionQueue) {
			ReadiedAction nextAction = actionQueue.peek();
			battleDuration = nextAction.getStartTime();
			Unit source = nextAction.getSource();
			if (!isValidAction(nextAction)) {
				System.out.println("Warning: " + source + "'s " + nextAction.getAbility() + " is now unusable!");
				dequeueAction(nextAction);
				performNextAction();
				return;
			} else if (!targetReachable(nextAction)) {
				PathingGridPosition path = getPathToUseAction(nextAction);
				if (path != null && path.getFirstMove() != null) {
					GridPosition moveTo = path.getFirstMove();
					BattleQueue.insertFirstAction(Ability.get(Ability.ID.MOVE), source, new GroundTarget(moveTo));
					performNextAction();
				} else {
					BattleQueue.insertFirstAction(Ability.get(Ability.ID.DELAY), source, source);
					performNextAction();
				}
			} else {
				actionQueue.poll().activate(); //remove from the queue
			}
		}
	}
	
	private static boolean isValidAction(ReadiedAction action) {
		boolean validity = true;
		ITargetable target = action.getTarget();
		if (action.getAbility().getSelectionTargetType() != Ability.TARGET_TYPE.DEAD && target instanceof TallObject) {
			validity = ((TallObject)target).isAlive();
		}
		return validity;
	}
	
	private static boolean targetReachable(ReadiedAction action) {
		return targetReachable(action, action.getSource().getPos());
	}
	
	private static boolean targetReachable(ReadiedAction action, GridPosition sourcePos) {
		GridPosition targetPos = action.getTarget().getPos();
		int range = action.getAbility().getRange();
		return sourcePos.getDistanceTo(targetPos) <= range;
	}
	
	private static PathingGridPosition getPathToUseAction(ReadiedAction action) {
		if (me == null) {
			me = new BattleQueue();
		}
		Unit source = action.getSource();
		GridPosition targetPos = action.getTarget().getPos();
		PathingGridPosition node = me.new PathingGridPosition(source.getPos(), null, targetPos);
		LinkedList<PathingGridPosition> frontier = new LinkedList<PathingGridPosition>();
		frontier.add(node);
		LinkedList<PathingGridPosition> explored = new LinkedList<PathingGridPosition>();
		while(node.getCostEstimate() < 100) {
			if (frontier.isEmpty()) {
				return null;
			}
			Collections.sort(frontier, LOWEST_COST);
			node = frontier.poll();
			if (targetReachable(action, node)) {
				return node;
			}
			explored.add(node);
			for(GridPosition n : World.getOpenNeighbors(node)) {
				PathingGridPosition neighbor = me.new PathingGridPosition(n, node, targetPos);
				if (!explored.contains(neighbor)) {
					if (!frontier.contains(neighbor)) {
						frontier.add(neighbor);
					}
					else if (frontier.get(frontier.indexOf(neighbor)).getCostEstimate() > neighbor.getCostEstimate()) {
						frontier.remove(neighbor);
						frontier.add(neighbor);
					}
				}
			}
		}
		return null;
	}
	
	public static void delay(Unit unit, int delay) {
		if (delay > 0) {
			synchronized((actionQueue)) {
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
	
	private class PathingGridPosition extends GridPosition {
		private int cost;
		private int expectedCost;
		private PathingGridPosition history;
		
		public PathingGridPosition(GridPosition pos, PathingGridPosition history, GridPosition target) {
			super(pos.getX(), pos.getY());
			this.history = history;
			if (history == null) {
				cost = 1;
			} else {
				cost = history.cost + 1;
			}
			calcExpectedCost(target);
		}
		
		private void calcExpectedCost(GridPosition target) {
			int dx = getX() - target.getX();
			if (dx < 0) dx = -dx;
			int dy = getY() - target.getY();
			if (dy < 0) dy = -dy;
			expectedCost = dx + dy;
		}
		
		public int getCostEstimate() {
			return cost + expectedCost;
		}
		
		public GridPosition getFirstMove() {
			if (history == null) {
				return null;
			} else if (history.getFirstMove() == null) {
				return this;
			} else {
				return history.getFirstMove();
			}
		}
	}

	public static void setActionComplete() {
		performingAction = false;
	}
}
