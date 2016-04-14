package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import model.Unit.TEAM;
import model.World;

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
	private static Thread playNextAction;
	private static BattleQueue me;
	private static boolean playingActions = false;
	private static int battleDuration = 0;
	private static boolean pause = true;
	private static boolean performingAction = false;
	private static Unit activePlayer = null;
	private static Set<IBattleListener> battleListeners = new HashSet<IBattleListener>();
	private static Set<Unit.TEAM> activeTeams = new HashSet<Unit.TEAM>();
	
	private static BattleQueue getMe() {
		if (me == null) {
			me = new BattleQueue();
		}
		return me;
	}
	
	public static void startPlayingActions() {
		playingActions = true;
		if (playNextAction == null || !playNextAction.isAlive()) {
			playNextAction = new Thread() {
				@Override
				public void run() {
					BattleQueue me = getMe();
					while(playingActions) {
						try {
							Unit readyUnit = getMostReadyPlayer();
							if (readyUnit == null) {
								continue;
							}
							synchronized(me) {
								if (activePlayer == null) {
									setActivePlayer(readyUnit);
								}
								// if not paused, not already doing something, and not waiting for someone to plan an action
								if (!pause && !performingAction && !actionQueue.isEmpty()
										&& actionQueue.peek().getStartTime() <= completionTimes.get(readyUnit)) {
									performNextAction();
								}
							}
							Thread.sleep(PERFORM_ACTION_CHECK_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					battleDuration = 0;
					pause = true;
					performingAction = false;
					activePlayer = null;
					battleListeners.clear();
					activeTeams.clear();
				}
			};
			playNextAction.setDaemon(true);
			playNextAction.start();
		}
	}
	
	public static void stopPlayingActions() {
		playingActions = false;
	}
	
	public static void setPause(boolean pause) {
		BattleQueue.pause = pause;
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
		System.out.print("addCombatant(" + unit + ")");
		synchronized(getMe()) {
			lastScheduledTimes.put(unit, battleDuration);
			completionTimes.put(unit, battleDuration);
		}
		activeTeams.add(unit.getTeam());
		if (unit.getTeam() != TEAM.PLAYER) {
			queueAction(Ability.get(Ability.ID.AI_TURN), unit, unit);
		}
		unitAdded(unit);
	}
	
	public static void removeCombatant(Unit unit, Ability.ID exitAbility) {
		BattleQueue me = getMe();
		synchronized(me) {			
			if (lastScheduledTimes.get(unit) == null) {
				return;
			}
			Iterator<ReadiedAction> actions = actionQueue.iterator();
			while (actions.hasNext()) {
				if (unit.equals(actions.next().getSource())) {
					actions.remove();
				}
			}
			if (activePlayer != null && activePlayer.equals(unit)) {
				activePlayer = null;
			}
		}
		if (exitAbility != null) {
			Ability death = Ability.get(Ability.ID.DEATH);
			insertFirstAction(death, unit, unit, null, new Runnable() {
				@Override
				public void run() {
					reportDefeat(unit);
				}
			});
		}
		else {
			reportDefeat(unit);
		}
		synchronized(me) {
			lastScheduledTimes.remove(unit);
			completionTimes.remove(unit);
		}
	}
	
	private static void reportDefeat(Unit unit) {
		unitDefeated(unit);
		unitRemoved(unit);
		// Determine if the team was defeated
		Unit.TEAM team = unit.getTeam();
		boolean teamDefeat = true;
		synchronized(getMe()) {
			Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
			while (teamDefeat && combatants.hasNext()) {
				teamDefeat = !(combatants.next().getTeam() == team);
			}
		}
		if (teamDefeat) {
			activeTeams.remove(team);
			teamDefeated(team);
		}
	}
	
	/**
	 * Delay all active combatants a random amount between 0 and 1000 ms
	 */
	public static void addRandomCombatDelays() {
		synchronized(getMe()) {
			Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
			while (combatants.hasNext()) {
				delay(combatants.next(), RAND.nextInt(1000));
			}
		}
	}
	
	/**
	 * Clear the action queue and remove all combatants from combat
	 */
	public static void endCombat() {
		synchronized(getMe()) {
			lastScheduledTimes.clear();
			completionTimes.clear();
			actionQueue.clear();
			//TODO: keep players, but reset to 0?
		}
		battleDuration = 0;
	}
	
	public static void queueAction(Ability ability, Unit source, ITargetable target) {
		System.out.print("\t" + source + " has prepared " + ability + " for " + target);
		synchronized(getMe()) {
			ReadiedAction action = new ReadiedAction(ability, source, target, completionTimes.get(source));
			lastScheduledTimes.put(source, completionTimes.get(source));
			completionTimes.put(source, action.getStartTime() + ability.getDelay());
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
			if (source.equals(activePlayer))
				activePlayerAbilityQueuedChanged();
		}
	}
	
	/**
	 * Insert an ability at the front of the queue, delaying other abilities as appropriate.
	 * 
	 * @param ability ability to perform
	 * @param source unit to perform the ability
	 * @param target target for the ability
	 */
	public static void insertFirstAction(Ability ability, Unit source, ITargetable target) {
		insertFirstAction(ability, source, target, null, null);
	}
	
	/**
	 * Insert an ability at the front of the queue, delaying other abilities as appropriate. Set the special doAtMid and 
	 * doAtEnd runnable actions on this new action
	 * 
	 * @param ability ability to perform
	 * @param source unit to perform the ability
	 * @param target target for the ability
	 * @param doAtMid action to perform part way through the ability
	 * @param doAtEnd action to perform after the ability completes
	 */
	public static void insertFirstAction(Ability ability, Unit source, ITargetable target, Runnable doAtMid, Runnable doAtEnd) {
		delay(source, ability.getDelay());
		ReadiedAction action = new ReadiedAction(ability, source, target, battleDuration);
		action.setDoAtMid(doAtMid);
		action.setDoAtEnd(doAtEnd);
		synchronized(getMe()) {
			System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has immediately prepared " + ability + " for " + action.getTarget());
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public static void dequeueAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		synchronized(getMe()) {
			lastScheduledTimes.put(source, lastScheduledTimes.get(source) - ability.getDelay());
			completionTimes.put(source, completionTimes.get(source) - ability.getDelay());
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
			System.out.println("\t" + source + "(" + lastScheduledTimes.get(source) + ") has decided not to use " + ability);
			if (source.equals(activePlayer))
				activePlayerAbilityQueuedChanged();
		}
	}

	/**
	 * Search the completion times of all players to find the player needing to schedule another action soonest
	 * @return the player needing to schedule another action soonest
	 */
	public static Unit getMostReadyPlayer() {
		Unit mostReadyPlayer = null;
		int unitBusyness = Integer.MAX_VALUE;
		synchronized(getMe()) {
			Iterator<Unit> combatants = completionTimes.keySet().iterator();
			while (combatants.hasNext()) {
				Unit combatant = combatants.next();
				if (combatant.getTeam() == TEAM.PLAYER && completionTimes.get(combatant) < unitBusyness) {
					mostReadyPlayer = combatant;
					unitBusyness = completionTimes.get(combatant);
				}
			}
		}
		return mostReadyPlayer;
	}
	
	/**
	 * perform the next queued action
	 */
	public static void performNextAction() {
		performingAction = true;
		synchronized(getMe()) {
			ReadiedAction nextAction = actionQueue.peek();
			battleDuration = nextAction.getStartTime();
			Unit source = nextAction.getSource();
			if (!isValidAction(nextAction)) {
				System.out.println(source + " abandoning " + nextAction.getAbility());
				dequeueAction(nextAction);
				performNextAction();
				return;
			}
			
			if (nextAction.getAbility().getId() == Ability.ID.AI_TURN) {
				nextAction = source.aiGetAction(nextAction.getStartTime());				
				delay(source, nextAction.getAbility().getDelay());
				nextAction.activate();
			} else if (!targetReachable(nextAction)) {
				PathingGridPosition path = getPathToUseAction(nextAction);
				if (path != null && path.getFirstMove() != null) {
					GridPosition moveTo = path.getFirstMove();
					BattleQueue.insertFirstAction(Ability.get(Ability.ID.MOVE), source, new GroundTarget(moveTo));
					performNextAction();
				} else {
					actionQueue.poll(); //remove from the queue
					if (source.equals(activePlayer))
						activePlayerAbilityQueuedChanged();
				}
			} else {
				nextAction.activate();
				actionQueue.poll(); //remove from the queue
				if (source.equals(activePlayer))
					activePlayerAbilityQueuedChanged();
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
	
	public static ReadiedAction getFirstStepToUseAction(ReadiedAction action) {
		if (targetReachable(action))
			return action;
		else {
			PathingGridPosition path = getPathToUseAction(action);
			if (path != null && path.getFirstMove() != null) {
				GridPosition pos = path.getFirstMove();
				return new ReadiedAction(Ability.get(Ability.ID.MOVE), action.getSource(), new GroundTarget(pos), action.getStartTime());
			}
		}
		return null;
	}
	
	//TODO: sometimes takes forever / arbitrarily long
	private static PathingGridPosition getPathToUseAction(ReadiedAction action) {
		BattleQueue me = getMe();
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
			for(GridPosition n : World.getTraversableNeighbors(node)) {
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
		synchronized(getMe()) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(unit)) {
					action.delay(delay);
				}
			}
			Integer lastScheduledTime = lastScheduledTimes.get(unit);
			if (lastScheduledTime != null)
				lastScheduledTimes.put(unit, lastScheduledTime + delay);
			Integer completionTime = completionTimes.get(unit);
			if (completionTime != null)
				synchronized(completionTimes) {
					completionTimes.put(unit, completionTime + delay);
				}
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public static void setActionComplete() {
		performingAction = false;
	}

	//INFORM LISTENERS
	public static void addBattleListener(IBattleListener listener) {
		battleListeners.add(listener);
	}
	
	public static void clearBattleListeners() {
		battleListeners.clear();
	}
	
	private static void unitAdded(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitAdded(unit);
		}
	}
	
	private static void unitRemoved(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitRemoved(unit);
		}
	}
	
	private static void unitDefeated(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitDefeated(unit);
		}
	}
	
	private static void teamDefeated(Unit.TEAM team) {
		for (IBattleListener listener : battleListeners) {
			listener.onTeamDefeated(team);
		}
	}
	
	private static void activePlayerAbilityQueuedChanged() {
		List<ReadiedAction> actions = new ArrayList<ReadiedAction>();
		for (ReadiedAction action : actionQueue) {
			if (action.getSource().equals(activePlayer)) {
				actions.add(action);
			}
		}
		for (IBattleListener listener : battleListeners) {
			listener.onActivePlayerAbilityQueueChanged(actions.iterator());
		}
	}
	
	private static void changedActivePlayer(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onChangedActivePlayer(unit);
		}
		activePlayerAbilityQueuedChanged();
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
	
	public static Iterator<ReadiedAction> getActionQueueIterator() {
		return actionQueue.iterator();
	}
	
	public static int getLastScheduledTime(Unit unit) {
		Integer time;
		synchronized(getMe()) {
			time = lastScheduledTimes.get(unit);
		}
		return time == null ? -1 : time;
	}
	
	public static int getCompletionTime(Unit unit) {
		Integer time;
		synchronized(getMe()) {
			time = completionTimes.get(unit);
		}
		return time == null ? -1 : time;
	}
	
	public static String getState() {
		String state = "@" + battleDuration;
		state +="\nLast Scheduled/Completion: ";
		synchronized(getMe()) {
					for(Unit unit : lastScheduledTimes.keySet()) {
				state += "\t" + unit + ":" + lastScheduledTimes.get(unit) + "/" + completionTimes.get(unit);  
			}
			state += "\nReady to schedule: " + getMostReadyPlayer();
			for (ReadiedAction action :actionQueue) {
				state += "\n\t(" + action.getStartTime() + ")\t"+ action.getSource() 
						+ " ---" + action.getAbility() + "--> " + action.getTarget();
			}
		}
		
		return state;
	}

	public static boolean isQueued(Unit source, Ability ability, ITargetable target) {
		synchronized(getMe()) {
			for (ReadiedAction action :actionQueue) {
				if (action.getSource().equals(source) && action.getAbility().equals(ability) && action.getTarget().equals(target)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void finishPlanningAction(Unit unit) {
		synchronized(getMe()) {
			if (activePlayer != null && activePlayer.equals(unit)) {
				activePlayer = null;
			}
			else {
				System.err.println("Request to finish planning for " + unit + " when " + activePlayer + "was planning (IGNORED)");
			}
		}
	}

	public static void setActivePlayer(Unit unit) {
		activePlayer = unit;
		changedActivePlayer(unit);
	}
}
