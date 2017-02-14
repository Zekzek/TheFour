package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import model.Ability;
import model.GameObject;
import model.GameObject.TEAM;
import model.GridPosition;
import model.GroundTarget;
import model.ITargetable;
import model.ReadiedAction;
import model.Unit;
import model.World;

public class ActionQueue implements IGameObjectListener{
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
//	private static final Random RAND = new Random();
	private static final int PERFORM_ACTION_CHECK_DELAY = 50;
	private static final LinkedList<ReadiedAction> actionQueue = new LinkedList<ReadiedAction>();

	private World world;
	private Thread playNextAction;
	private boolean playingActions = false;
	private long battleTime = 0;
	private boolean pause = true;
	private Set<Unit.TEAM> activeTeams;
	private Unit activePlayer = null;
	private Unit mostReadyPlayer = null;
	private Set<IBattleListener> battleListeners;
	private Set<IPlayerListener> playerListeners;
	private Unit soleActingUnit;
	private Set<Unit> actingUnits;
	
	public ActionQueue(World world) {
		this.world = world;
		activeTeams = new HashSet<Unit.TEAM>();
		battleListeners = new HashSet<IBattleListener>();
		playerListeners = new HashSet<IPlayerListener>();
		actingUnits = new HashSet<Unit>();
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
						getActivePlayer();
						checkNextAction();
					}
					reset();
				}
			};
			playNextAction.setDaemon(true);
			playNextAction.start();
		}
	}
	
	public void stopPlayingActions() {
		playingActions = false;
	}
	
	
	public void addGameObject(GameObject gameObject, int x, int y) {
		System.out.print("addGameObject(" + gameObject + ")");
		world.addTallObject(gameObject, x, y);
		gameObject.addGameObjectListener(this);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;
			activeTeams.add(unit.getTeam());
			unitAdded(unit);
		}
	}
	
	public void defeatGameObject(Unit unit) {
		clearUnitActions(unit);
		queueAction(Ability.get(Ability.ID.DEATH), unit, unit);
		reportDefeat(unit);
	}
	
	public void removeGameObject(GameObject gameObject) {
		System.out.print("removeGameObject(" + gameObject + ")");
		world.remove(gameObject);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;	
			clearUnitActions(unit);
			unitRemoved(unit);
		}
	}
	
	public void clearUnitActions(Unit unit) {
		synchronized(this) {
			Iterator<ReadiedAction> actions = actionQueue.iterator();
			while (actions.hasNext()) {
				if (unit.equals(actions.next().getSource())) {
					actions.remove();
				}
			}
		}
	}
	
	private void reportDefeat(Unit unit) {
		unitDefeated(unit);
		unitRemoved(unit);
		// Determine if the team was defeated
		Unit.TEAM team = unit.getTeam();
		boolean teamDefeat = true;
		synchronized(this) {
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
		synchronized(this) {
			//TODO: add combat delays
//			Iterator<Unit> combatants = lastScheduledTimes.keySet().iterator();
//			while (combatants.hasNext()) {
//				delay(combatants.next(), RAND.nextInt(1000));
//			}
		}
	}
	
	public void queueAction(Ability ability, Unit source, ITargetable target) {
		System.out.print("\t" + source + " has prepared " + ability + " for " + target);
		synchronized(this) {
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
	public void insertFirstAction(Ability ability, Unit source, ITargetable target, Runnable doAtMid, Runnable doAtEnd) {
		delay(source, ability.getDelay());
		ReadiedAction action = new ReadiedAction(ability, source, target, battleTime);
		action.addDoAtMid(doAtMid);
		action.addDoAtEnd(doAtEnd);
		synchronized(this) {
			System.out.println("\t" + source + " has immediately prepared " + ability + " for " + action.getTarget());
			actionQueue.add(action);
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		}
	}
	
	public void dequeueAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		synchronized(this) {
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
		long unitBusyness = mostReadyPlayer==null ? Long.MAX_VALUE : getCompletionTime(mostReadyPlayer);
		boolean change = false;
		synchronized(this) {
			Set<GameObject> playerObjects = world.getContentsOnTeam(TEAM.PLAYER);
			for (GameObject playerObject : playerObjects) {
				if (playerObject instanceof Unit) {
					Unit combatant = (Unit) playerObject;
					long busyness = getCompletionTime(combatant);
					if (combatant.getTeam() == TEAM.PLAYER && busyness < unitBusyness) {
						change = true;
						mostReadyPlayer = combatant;
						unitBusyness = busyness;
					}
				}
			}
		}
		if (change)
			changedMostReadyPlayer(mostReadyPlayer);
		return mostReadyPlayer;
	}
	
	public Unit getActivePlayer() {
		if (activePlayer == null) {
			setActivePlayer(getMostReadyPlayer());
		}
		return activePlayer;
	}
	
	public boolean blockingAction(Unit unit) {
		return soleActingUnit != null || actingUnits.contains(unit);
	}
	
	private void checkNextAction() {
		if (pause)
			return; //paused, do nothing
		if (actionQueue.isEmpty())
			return; //no more actions queued, do nothing
		ReadiedAction nextAction = actionQueue.peek();
		if (nextAction.getStartTime() >= getCompletionTime(getActivePlayer()) - 1)
			return; //waiting for player, do nothing
		Unit source = nextAction.getSource();
		if (blockingAction(source))
			return; //already busy performing an action, do nothing
		
		if (!isValidAction(nextAction)) {
			System.out.println(source + " abandoning " + nextAction.getAbility());
			dequeueAction(nextAction);
		}
		
		ReadiedAction firstAction = getFirstStepToUseAction(nextAction);
		if (firstAction == nextAction) {
			actionQueue.poll(); //next action is ready to use, remove it from the queue
		}
		else {
			delay(source, firstAction.getAbility().getDelay()); //inserting another action, delay future actions
		}
		
		perform(getFirstStepToUseAction(nextAction));
		checkNextAction();
	}

	/**
	 * Perform the action: animate, effect other game objects, and update internal scheduling
	 * @param action
	 */
	private void perform(final ReadiedAction action) {
		Unit source = action.getSource();
		Ability ability = action.getAbility();
		ITargetable target = action.getTarget();
		System.out.println(source + " uses " + ability + " on " + target);
		
		actingUnits.add(source);
		if (source.isInCombat())
			soleActingUnit = source;
		
		battleTime = action.getStartTime();
		
		//Apply any speed modifier to all future actions
		delay(source, ability.calcAdditionalDelay(source.getModifier()));
		
		//Fast out-of-combat healing
		if(!source.isInCombat()) {
			source.heal(source.getMaxHp() / 10 * ability.getDelay() / 1000);
		}
		
		action.activateAtStart();
		source.animate(ability, world);
		if (source.equals(activePlayer))
			activePlayerAbilityQueuedChanged();	
		ActionQueue actionQueue = this;
		Thread activateAtMid = new Thread() {
			@Override
			public void run(){
				try {
					Thread.sleep(ability.calcDelay(source.getModifier()) / 2);
				} catch (InterruptedException e) { e.printStackTrace(); }
				action.activateAtMid(actionQueue);
			}
		};
		Thread activateAtEnd = new Thread() {
			@Override
			public void run(){
				try {
					Thread.sleep(ability.calcDelay(source.getModifier()));
				} catch (InterruptedException e) { e.printStackTrace(); }
				action.activateAtEnd();
				actingUnits.remove(source);
				if (source.equals(soleActingUnit))
					soleActingUnit = null;
				if (source.getTeam() == TEAM.PLAYER)
					playerUsedAbility(action);
			}
		};
		activateAtMid.start();
		activateAtEnd.start();
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
		return sourcePos.getDistanceFromCenterTo(targetPos) <= range;
	}
	
	public ReadiedAction getFirstStepToUseAction(ReadiedAction action) {
		Ability.ID abilityId = action.getAbility().getId();
		Unit source = action.getSource();
		
		if (abilityId == Ability.ID.AI_TURN) {
			return getFirstStepToUseAction(source.aiGetAction(action.getStartTime(), world));				
		}
		else if (abilityId == Ability.ID.FLEE) {
			PathingGridPosition path = getPathToScreenEdge(source);
			if (path != null && path.getFirstMove() != null) {
				GridPosition pos = path.getFirstMove();
				return new ReadiedAction(Ability.get(Ability.ID.MOVE), source, new GroundTarget(pos), action.getStartTime());
			}
		}
		else if (!targetReachable(action)) {
			PathingGridPosition path = getPathToUseAction(action);
			if (path != null && path.getFirstMove() != null) {
				GridPosition pos = path.getFirstMove();
				return new ReadiedAction(Ability.get(Ability.ID.MOVE), source, new GroundTarget(pos), action.getStartTime());
			}
		}
		return action;
	}
	
	private PathingGridPosition getPathToUseAction(ReadiedAction action) {
		Unit source = action.getSource();
		GridPosition targetPos = action.getTarget().getPos();
		PathingGridPosition node = new PathingGridPosition(source.getPos(), null, targetPos);
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
			for(GridPosition n : world.getTraversableNeighbors(node)) {
				PathingGridPosition neighbor = new PathingGridPosition(n, node, targetPos);
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
		PathingGridPosition node = new PathingGridPosition(source.getPos(), null, null);
		LinkedList<PathingGridPosition> frontier = new LinkedList<PathingGridPosition>();
		frontier.add(node);
		LinkedList<PathingGridPosition> explored = new LinkedList<PathingGridPosition>();
		while(node.getCostEstimate() < 100) {
			if (frontier.isEmpty()) {
				return null;
			}
			Collections.sort(frontier, LOWEST_COST);
			node = frontier.poll();
			if (!world.getMainScreenRectangle().intersects(node)) {
				return node;
			}
			explored.add(node);
			for(GridPosition n : world.getTraversableNeighbors(node)) {
				PathingGridPosition neighbor = new PathingGridPosition(n, node, null);
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
		synchronized(this) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(unit)) {
					action.delay(delay);
				}
			}
			Collections.sort(actionQueue, SOONEST_READIED_ACTION);
			getMostReadyPlayer();
		}
	}

	//INFORM LISTENERS
	public void addBattleListener(IBattleListener listener) {
		battleListeners.add(listener);
	}
	
	public void clearBattleListeners() {
		battleListeners.clear();
	}
	
	public void addPlayerListener(IPlayerListener listener) {
		synchronized(this) {
			playerListeners.add(listener);
		}
	}
	
	public void clearPlayerListeners() {
		synchronized(this) {
			playerListeners.clear();
		}
	}
	
	private void unitAdded(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitAdded(unit);
		}
	}
	
	private void unitRemoved(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitRemoved(unit);
		}
	}
	
	private void unitDefeated(Unit unit) {
		for (IBattleListener listener : battleListeners) {
			listener.onUnitDefeated(unit);
		}
	}
	
	private void teamDefeated(Unit.TEAM team) {
		for (IBattleListener listener : battleListeners) {
			listener.onTeamDefeated(team);
		}
	}
	
	private void activePlayerAbilityQueuedChanged() {
		List<ReadiedAction> actions = new ArrayList<ReadiedAction>();
		synchronized(this) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(activePlayer)) {
					actions.add(action);
				}
			}
			for (IPlayerListener listener : playerListeners) {
				listener.onActivePlayerAbilityQueueChanged(actions.iterator());
			}
		}
	}
	
	private void changedActivePlayer(Unit unit) {
		world.setFocusTarget(unit);
		synchronized(this) {
			for (IPlayerListener listener : playerListeners) {
				listener.onChangedActivePlayer(unit);
			}
		}
		activePlayerAbilityQueuedChanged();
	}
	
	private void changedMostReadyPlayer(Unit unit) {
		synchronized(this) {
			for (IPlayerListener listener : playerListeners) {
				listener.onChangedMostReadyPlayer(unit);
			}
		}
		activePlayerAbilityQueuedChanged();
	}
	
	private void playerUsedAbility(ReadiedAction action) {
		synchronized(this) {
			for (IPlayerListener listener : playerListeners) {
				listener.onPlayerUsedAbility(action);
			}
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

	public Iterator<ReadiedAction> getActionQueueIterator() {
		return actionQueue.iterator();
	}
	
	public long getLastScheduledTime(Unit unit) {
		Long time = battleTime - 1;
		synchronized(this) {
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
		Long time = battleTime;
		synchronized(this) {
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
		synchronized(this) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(source) && action.getAbility().equals(ability) && action.getTarget().equals(target)) {
					return true;
				}
			}
		}
		return false;
	}

	public void finishPlanningAction(Unit unit) {
		synchronized(this) {
			if (activePlayer != null && activePlayer.equals(unit)) {
				activePlayer = null;
			}
			else {
				System.err.println("Request to finish planning for " + unit + " when " + activePlayer + "was planning (IGNORED)");
			}
		}
	}

	public boolean isPaused() {
		return pause;
	}
	
	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public void setActivePlayer(Unit unit) {
		activePlayer = unit;
		changedActivePlayer(unit);
	}

	public void reset() {
		clearBattleListeners();
		clearPlayerListeners();
		actionQueue.clear();
		world.reset();
		battleTime = 0;
		pause = true;
		activePlayer = null;
		battleListeners.clear();
		activeTeams.clear();
	}

	@Override
	public void onObjectDeath(GameObject object) {
		if (object instanceof Unit) {
			Unit unit = (Unit) object;
			defeatGameObject(unit);
		}
	}

	@Override
	public void onObjectTeamChange(GameObject object) {
		Set<GameObject> playerObjects = world.getContentsOnTeam(TEAM.PLAYER);
		synchronized(this) {
			for (IPlayerListener listener : playerListeners) {
				listener.onChangedPlayerTeam(playerObjects);
			}
		}
	}

	public void addObjectToNorth(Unit unit, int x){
		GridPosition focusPos = world.getFocusTarget().getPos();
		x += focusPos.getX();
		int y = focusPos.getY() + world.getNorth();
		int count = 1;
		while (count < 50) {
			int relativePos = count/2 * count%2==0?1:-1;
			GridPosition pos = new GridPosition(x + relativePos, y);
			if (world.isTraversable(pos)) {
				addGameObject(unit, pos.getX(), pos.getY());
				break;
			}
			count++;
		}
	}
	
	public void addObjectToEast(Unit unit, int y){
		GridPosition focusPos = world.getFocusTarget().getPos();
		int x = focusPos.getX() + world.getEast();
		y += focusPos.getY();
		int count = 1;
		while (count < 50) {
			int relativePos = count/2 * count%2==0?1:-1;
			GridPosition pos = new GridPosition(x, y+ relativePos);
			if (world.isTraversable(pos)) {
				addGameObject(unit, pos.getX(), pos.getY());
				break;
			}
			count++;
		}
	}
	
	public void addObjectToSouth(Unit unit, int x){
		GridPosition focusPos = world.getFocusTarget().getPos();
		x += focusPos.getX();
		int y = focusPos.getY() + world.getSouth();
		int count = 1;
		while (count < 50) {
			int relativePos = count/2 * count%2==0?1:-1;
			GridPosition pos = new GridPosition(x + relativePos, y);
			if (world.isTraversable(pos)) {
				addGameObject(unit, pos.getX(), pos.getY());
				break;
			}
			count++;
		}
	}
	
	public void addObjectToWest(Unit unit, int y){
		GridPosition focusPos = world.getFocusTarget().getPos();
		int x = focusPos.getX() + world.getWest();
		y += focusPos.getY();
		int count = 1;
		while (count < 50) {
			int relativePos = count/2 * count%2==0?1:-1;
			GridPosition pos = new GridPosition(x, y + relativePos);
			System.out.println(relativePos);
			if (world.isTraversable(pos)) {
				addGameObject(unit, pos.getX(), pos.getY());
				System.out.println(unit + "@" + pos);
				break;
			}
			count++;
		}
	}
}
