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
import model.ActionQueue;
import model.GameObject;
import model.GameObject.TEAM;
import model.GridPosition;
import model.GroundTarget;
import model.ITargetable;
import model.ReadiedAction;
import model.Unit;
import model.World;

public class ActionPlayer implements IGameObjectListener{
	private static final LowestCostPathComparator LOWEST_COST_PATH = new LowestCostPathComparator();
	private static final int PERFORM_ACTION_CHECK_DELAY = 50;
	
	private World world;
	private ActionLoop actionLoop;
	private ActionQueue actionQueue;
	private Set<Unit.TEAM> activeTeams;
	private Unit activePlayer = null;
	private Set<IBattleListener> battleListeners;
	private Set<IPlayerListener> playerListeners;
	private Set<Unit> actingUnits;
	
	public ActionPlayer(World world) {
		this.world = world;
		actionQueue = new ActionQueue( world );
		activeTeams = new HashSet<Unit.TEAM>();
		battleListeners = new HashSet<IBattleListener>();
		playerListeners = new HashSet<IPlayerListener>();
		actingUnits = new HashSet<Unit>();
		actionLoop = new ActionLoop();
		ReadiedAction.setActionPlayer( this );
	}

	public void startPlayingActions() {
		actionLoop.play();
	}
	
	public void stopPlayingActions() {
		actionLoop.pause();
	}

	public boolean isPaused() {
		return !actionLoop.isPlaying();
	}
	
	public void setPause(boolean pause) {
		if (pause)
			actionLoop.pause();
		else
			actionLoop.play();
	}

	
	public void addGameObject(GameObject gameObject, int x, int y) {
		System.out.println("addGameObject(" + gameObject + ")");
		world.addTallObject(gameObject, x, y);
		gameObject.addGameObjectListener(this);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;
			activeTeams.add(unit.getTeam());
			unitAdded(unit);
		}
	}
	
	public void defeatGameObject(Unit unit) {
		actionQueue.clearUnitActions(unit);
		actionQueue.appendAction(Ability.get(Ability.ID.DEATH), unit, unit);
		reportDefeat(unit);
	}
	
	public void removeGameObject(GameObject gameObject) {
		System.out.print("removeGameObject(" + gameObject + ")");
		world.remove(gameObject);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;	
			actionQueue.clearUnitActions(unit);
			unitRemoved(unit);
		}
	}
	
	public void clearUnitActions(Unit unit) {
		actionQueue.clearUnitActions(unit);
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
		actionQueue.appendAction(ability, source, target);
	}
	
	public void dequeueAction(ReadiedAction action) {
		actionQueue.removeAction(action);
	}
	
	public Unit calcActivePlayer() {
		if (activePlayer == null) {
			setActivePlayer(actionQueue.getMostReadyPlayer());
		}
		return activePlayer;
	}
	
	private void checkNextAction() {
		Unit activePlayer = calcActivePlayer();
		actionQueue.incrementTimeWaitForUnit(PERFORM_ACTION_CHECK_DELAY, activePlayer);
		ReadiedAction[] actions = actionQueue.getReadyActions();
		boolean foundInvalid = false;
		for (ReadiedAction action : actions) {
			if (!isValidAction(action)) {
				actionQueue.removeAction(action);
				foundInvalid = true;
			}
		}
		if (foundInvalid)
			actions = actionQueue.getReadyActions();
		for (ReadiedAction action : actions) {
			ReadiedAction firstStep = getFirstStepToUseAction(action);
			if (firstStep != action) {
				actionQueue.delay(action.getSource(), firstStep.calcDelay()); //inserting first step, delay future actions
			}
			perform(firstStep, firstStep == action);
		}
	}

	/**
	 * Perform the action: animate, effect other game objects, and update internal scheduling
	 * @param action
	 */
	private void perform(final ReadiedAction action, boolean isQueued) {
		Unit source = action.getSource();
		Ability ability = action.getAbility();
		ITargetable target = action.getTarget();
		
		System.out.println(source + " uses " + ability + " on " + target);
		actingUnits.add(source);
		action.activate();
		source.animate(ability, world);
		if (source.equals(activePlayer))
			activePlayerAbilityQueuedChanged();
		if (isQueued && action.isComplete() )
			actionQueue.completeAction(action); //Complete, so remove from queue
	}
	
	public void completeAction( ReadiedAction action ) {
		Unit source = action.getSource();
		actingUnits.remove(source);
		if (source.getTeam() == TEAM.PLAYER)
			playerUsedAbility(action);
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
			Collections.sort(frontier, LOWEST_COST_PATH);
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
			Collections.sort(frontier, LOWEST_COST_PATH);
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
			actions = actionQueue.getQueueFor(activePlayer);
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

	public void setActivePlayer(Unit unit) {
		activePlayer = unit;
		changedActivePlayer(unit);
	}

	public void reset() {
		clearBattleListeners();
		clearPlayerListeners();
		actionQueue.clear();
		world.reset();
		actionLoop.pause();
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
	
	private static class LowestCostPathComparator implements Comparator<PathingGridPosition> {
		@Override
		public int compare(PathingGridPosition pos1, PathingGridPosition pos2) {
			return pos1.getCostEstimate() - pos2.getCostEstimate();
		}
	}
	
	private class ActionLoop {
		private Thread thread;
		private boolean running;
		
		public void play() {
			if (thread == null || !thread.isAlive()) {
				running = true;
				thread = new Thread() {
					@Override
					public void run() {
						while(running) {
							try {
								Thread.sleep(PERFORM_ACTION_CHECK_DELAY);
							} catch (InterruptedException e) {
								break;
							}
							if (running) {
								ActionPlayer.this.checkNextAction();
							}
						}
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		}
		
		public void pause() {
			running = false;
			thread.interrupt();
		}
		
		public boolean isPlaying() {
			return running;
		}
	}
}
