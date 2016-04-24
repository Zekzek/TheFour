package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import model.Ability;
import model.GridPosition;
import model.GroundTarget;
import model.ITargetable;
import model.ReadiedAction;
import model.Structure;
import model.GameObject;
import model.GameObject.TEAM;
import model.Unit;
import model.World;

public class BattleQueue implements IGameObjectListener{
	private static final Comparator<ReadiedAction> SOONEST_READIED_ACTION = new Comparator<ReadiedAction>(){
		@Override
		public int compare(ReadiedAction action1, ReadiedAction action2) {
			return (int)(action1.getStartTime() - action2.getStartTime());
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
//	private static final Map<Unit,Long> lastScheduledTimes = new HashMap<Unit, Long>();
//	private static final Map<Unit,Long> completionTimes = new HashMap<Unit, Long>();

	private static BattleQueue me;
	private World world;
	private static Thread playNextAction;
	private static boolean playingActions = false;
	private static long battleDuration = 0;
	private static boolean inBattle = false;
	private static boolean pause = true;
	private static boolean performingAction = false;
	private static Set<Unit.TEAM> activeTeams = new HashSet<Unit.TEAM>();
	private static Unit activePlayer = null;
	private static Set<IBattleListener> battleListeners = new HashSet<IBattleListener>();
	private static Set<IPlayerListener> playerListeners = new HashSet<IPlayerListener>();
	
	public BattleQueue(World world) {
		this.world = world;
		me = this;
	}

	public void startPlayingActions() {
		playingActions = true;
		if (playNextAction == null || !playNextAction.isAlive()) {
			playNextAction = new Thread() {
				@Override
				public void run() {
					while(playingActions) {
						try {
							Thread.sleep(PERFORM_ACTION_CHECK_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Unit readyUnit = getMostReadyPlayer();
						if (readyUnit == null) {
							continue;
						}
						synchronized(me) {
							if (activePlayer == null) {
								setActivePlayer(readyUnit);
							}
							// if not paused, and not waiting for someone to plan an action
							if (!pause && !actionQueue.isEmpty()
									&& actionQueue.peek().getStartTime() <= getCompletionTime(readyUnit)) {
								if (inBattle) {
									if (!performingAction) {
										performNextAction();
									}
								} else {
									while (!actionQueue.isEmpty() && actionQueue.peek().getStartTime() < battleDuration) {
										performNextAction();
									}
									battleDuration += PERFORM_ACTION_CHECK_DELAY;
								}
							}
						}
					}
					battleDuration = 0;
					pause = true;
					performingAction = false;
					activePlayer = null;
					inBattle = false;
					battleListeners.clear();
					activeTeams.clear();
				}
			};
			playNextAction.setDaemon(true);
			playNextAction.start();
		}
	}
	
	public void stopPlayingActions() {
		playingActions = false;
	}
	
	public void setPause(boolean pause) {
		BattleQueue.pause = pause;
	}
	
	public void addGameObject(GameObject gameObject, int x, int y) {
		System.out.print("addGameObject(" + gameObject + ")");
		me.world.addTallObject(gameObject, x, y);
		gameObject.addGameObjectListener(this);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;
			activeTeams.add(unit.getTeam());
			if (unit.getTeam() != TEAM.PLAYER) {
				queueAction(Ability.get(Ability.ID.AI_TURN), unit, unit);
			}
			unitAdded(unit);
		}
	}
	
	
	public void removeCombatant(Unit unit, Ability exitAbility, ITargetable target) {
		synchronized(me) {
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
			if (target == null) 
				target = unit;
			insertFirstAction(exitAbility, unit, target, null, new Runnable() {
				@Override
				public void run() {
					reportDefeat(unit);
					me.world.remove(unit);
				}
			});
		}
		else {
			reportDefeat(unit);
			me.world.remove(unit);
		}
	}
	
	private void reportDefeat(Unit unit) {
		unitDefeated(unit);
		unitRemoved(unit);
		// Determine if the team was defeated
		Unit.TEAM team = unit.getTeam();
		boolean teamDefeat = true;
		synchronized(me) {
			//TODO: report team defeat
//			Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
//			while (teamDefeat && combatants.hasNext()) {
//				teamDefeat = !(combatants.next().getTeam() == team);
//			}
		}
		if (teamDefeat) {
			activeTeams.remove(team);
			teamDefeated(team);
		}
	}
	
	/**
	 * Delay all active combatants a random amount between 0 and 1000 ms
	 */
	public void addRandomCombatDelays() {
		synchronized(me) {
			//TODO: add combat delays
//			Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
//			while (combatants.hasNext()) {
//				delay(combatants.next(), RAND.nextInt(1000));
//			}
		}
	}
	
	public void startCombat() {
		inBattle = true;
	}
	
	/**
	 * Clear the action queue and remove all combatants from combat
	 */
	public void endCombat() {
		inBattle = false;
	}
	
//	public static void nonCombatantQueueAction(Ability ability, Unit source, ITargetable target) {
//		addCombatant(source);
//		removeCombatant(source, ability, target);
//	}
	
	public void queueAction(Ability ability, Unit source, ITargetable target) {
		System.out.print("\t" + source + " has prepared " + ability + " for " + target);
		synchronized(me) {
			ReadiedAction action = new ReadiedAction(ability, source, target, getCompletionTime(source));
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
	public void insertFirstAction(Ability ability, Unit source, ITargetable target) {
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
		action.addDoAtMid(doAtMid);
		action.addDoAtEnd(doAtEnd);
		synchronized(me) {
			System.out.println("\t" + source + " has immediately prepared " + ability + " for " + action.getTarget());
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public void dequeueAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		synchronized(me) {
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
			System.out.println("\t" + source + " has decided not to use " + ability);
			if (source.equals(activePlayer))
				activePlayerAbilityQueuedChanged();
		}
	}

	/**
	 * Search the completion times of all players to find the player needing to schedule another action soonest
	 * @return the player needing to schedule another action soonest
	 */
	public Unit getMostReadyPlayer() {
		Unit mostReadyPlayer = null;
		long unitBusyness = Long.MAX_VALUE;
		synchronized(me) {
			Iterator<Unit> combatants = me.world.getTeamUnits(TEAM.PLAYER);
			while (combatants.hasNext()) {
				Unit combatant = combatants.next();
				long busyness = getCompletionTime(combatant);
				if (combatant.getTeam() == TEAM.PLAYER && busyness < unitBusyness) {
					mostReadyPlayer = combatant;
					unitBusyness = busyness;
				}
			}
		}
		return mostReadyPlayer;
	}
	
	/**
	 * perform the next queued action
	 */
	public void performNextAction() {
		performingAction = true;
		synchronized(me) {
			ReadiedAction nextAction = actionQueue.peek();
			if (inBattle)
				battleDuration = nextAction.getStartTime();
			Unit source = nextAction.getSource();
			if (!isValidAction(nextAction)) {
				System.out.println(source + " abandoning " + nextAction.getAbility());
				dequeueAction(nextAction);
				performNextAction();
				return;
			}
			
			if (nextAction.getAbility().getId() == Ability.ID.AI_TURN) {
				nextAction = source.aiGetAction(nextAction.getStartTime(), me.world);				
				delay(source, nextAction.getAbility().getDelay());
				nextAction.activate(me.world, me);
			} else if (!targetReachable(nextAction)) {
				ReadiedAction firstAction = getFirstStepToUseAction(nextAction);
				if (firstAction != null) {
					insertFirstAction(Ability.get(Ability.ID.MOVE), source, firstAction.getTarget());
					performNextAction();
				} else {
					actionQueue.poll(); //remove from the queue
					if (source.equals(activePlayer))
						activePlayerAbilityQueuedChanged();
				}
			} else {
				if (source.getTeam() == TEAM.PLAYER) {
					final ReadiedAction playerAction = nextAction;
					nextAction.addDoAtEnd(new Runnable() {
						@Override
						public void run() {
							playerUsedAbility(playerAction);
						}
					});
				}
				nextAction.activate(me.world, me);
				actionQueue.poll(); //remove from the queue
				if (source.equals(activePlayer))
					activePlayerAbilityQueuedChanged();
			}
		}
	}
	
	private boolean isValidAction(ReadiedAction action) {
		boolean validity = true;
		ITargetable target = action.getTarget();
		if (action.getAbility().getSelectionTargetType() != Ability.TARGET_TYPE.DEAD && target instanceof GameObject) {
			validity = ((GameObject)target).isAlive();
		}
		return validity;
	}
	
	private boolean targetReachable(ReadiedAction action) {
		return targetReachable(action, action.getSource().getPos());
	}
	
	private boolean targetReachable(ReadiedAction action, GridPosition sourcePos) {
		GridPosition targetPos = action.getTarget().getPos();
		int range = action.getAbility().getRange();
		return sourcePos.getDistanceTo(targetPos) <= range;
	}
	
	public ReadiedAction getFirstStepToUseAction(ReadiedAction action) {
		long startTime = System.currentTimeMillis();
		if (action.getAbility().getId() == Ability.ID.FLEE) {
			PathingGridPosition path = getPathToScreenEdge(action.getSource());
			if (path != null && path.getFirstMove() != null) {
				GridPosition pos = path.getFirstMove();
				action = new ReadiedAction(Ability.get(Ability.ID.MOVE), action.getSource(), new GroundTarget(pos), action.getStartTime());
			}
		}
		else if (!targetReachable(action)) {
			PathingGridPosition path = getPathToUseAction(action);
			if (path != null && path.getFirstMove() != null) {
				GridPosition pos = path.getFirstMove();
				action = new ReadiedAction(Ability.get(Ability.ID.MOVE), action.getSource(), new GroundTarget(pos), action.getStartTime());
			}
		}
		System.out.println("Found first step in: " + (System.currentTimeMillis() - startTime) + ": " + action.getTarget());
		return action;
	}
	
	//TODO: sometimes gets stuck in a queue-not valid-queue loop?
	private PathingGridPosition getPathToUseAction(ReadiedAction action) {
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
			for(GridPosition n : me.world.getTraversableNeighbors(node)) {
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
	
	private PathingGridPosition getPathToScreenEdge(Unit source) {
		PathingGridPosition node = me.new PathingGridPosition(source.getPos(), null, null);
		LinkedList<PathingGridPosition> frontier = new LinkedList<PathingGridPosition>();
		frontier.add(node);
		LinkedList<PathingGridPosition> explored = new LinkedList<PathingGridPosition>();
		while(node.getCostEstimate() < 100) {
			if (frontier.isEmpty()) {
				return null;
			}
			Collections.sort(frontier, LOWEST_COST);
			node = frontier.poll();
			if (!me.world.getTargetableRectangle().intersects(node)) {
				return node;
			}
			explored.add(node);
			for(GridPosition n : me.world.getTraversableNeighbors(node)) {
				PathingGridPosition neighbor = me.new PathingGridPosition(n, node, null);
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
	
	public void delay(Unit unit, int delay) {
		synchronized(me) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(unit)) {
					action.delay(delay);
				}
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
	
	public static void addPlayerListener(IPlayerListener listener) {
		playerListeners.add(listener);
	}
	
	public static void clearPlayerListeners() {
		playerListeners.clear();
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
		for (IPlayerListener listener : playerListeners) {
			listener.onActivePlayerAbilityQueueChanged(actions.iterator());
		}
	}
	
	private static void changedActivePlayer(Unit unit) {
		for (IPlayerListener listener : playerListeners) {
			listener.onChangedActivePlayer(unit);
		}
		activePlayerAbilityQueuedChanged();
	}
	
	private static void playerUsedAbility(ReadiedAction action) {
		for (IPlayerListener listener : playerListeners) {
			listener.onPlayerUsedAbility(action);
		}
	}
		
	private class PathingGridPosition extends GridPosition {
		private int cost;
		private int expectedCost;
		private GridPosition firstMove;
		
		public PathingGridPosition(GridPosition pos, PathingGridPosition history, GridPosition target) {
			super(pos.getX(), pos.getY());
			if (history == null) {
				cost = 1;
			} else {
				cost = history.cost + 1;
				firstMove = history.getFirstMove();
				if (firstMove == null) {
					firstMove = this;
				}
			}
			calcExpectedCost(target);
		}
		
		private void calcExpectedCost(GridPosition target) {
			if (target == null) {
				expectedCost = 0;
			} 
			else {
				int dx = getX() - target.getX();
				if (dx < 0) dx = -dx;
				int dy = getY() - target.getY();
				if (dy < 0) dy = -dy;
				expectedCost = dx + dy;
			}
		}
		
		public int getCostEstimate() {
			return cost + expectedCost;
		}
		
		public GridPosition getFirstMove() {
			return firstMove;
		}
	}

	public boolean isInBattle() {
		return inBattle;
	}
	
	public Iterator<ReadiedAction> getActionQueueIterator() {
		return actionQueue.iterator();
	}
	
	public long getLastScheduledTime(Unit unit) {
		Long time = battleDuration;
		synchronized(me) {
			Iterator<ReadiedAction> iterator = actionQueue.iterator();
			while (iterator.hasNext()) {
				ReadiedAction action = iterator.next();
				if (action.getSource().equals(unit)) {
					time = action.getStartTime();
				}
			}
		}
		return time;
	}
	
	public long getCompletionTime(Unit unit) {
		Long time = battleDuration;
		synchronized(me) {
			Iterator<ReadiedAction> iterator = actionQueue.iterator();
			while (iterator.hasNext()) {
				ReadiedAction action = iterator.next();
				if (action.getSource().equals(unit)) {
					time = action.getStartTime() + action.getAbility().calcDelay(action.getSource().getModifier());
				}
			}
		}
		return time;
	}
	
	public boolean isQueued(Unit source, Ability ability, ITargetable target) {
		synchronized(me) {
			for (ReadiedAction action :actionQueue) {
				if (action.getSource().equals(source) && action.getAbility().equals(ability) && action.getTarget().equals(target)) {
					return true;
				}
			}
		}
		return false;
	}

	public void finishPlanningAction(Unit unit) {
		synchronized(me) {
			if (activePlayer != null && activePlayer.equals(unit)) {
				activePlayer = null;
			}
			else {
				System.err.println("Request to finish planning for " + unit + " when " + activePlayer + "was planning (IGNORED)");
			}
		}
	}

	public void setActivePlayer(Unit unit) {
		activePlayer = unit;
		changedActivePlayer(unit);
	}

	public void reset() {
		endCombat();
		clearBattleListeners();
		clearPlayerListeners();
		actionQueue.clear();
		me.world.reset();
	}

	@Override
	public void onObjectDeath(GameObject object) {
		if (object instanceof Unit) {
			Unit unit = (Unit) object;
			removeCombatant(unit, Ability.get(Ability.ID.DEATH), unit);
		}
	}
}
