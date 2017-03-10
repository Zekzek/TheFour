package controller;

import java.util.HashSet;
import java.util.Set;

import model.Ability;
import model.ActionQueue;
import model.GameObject;
import model.GameObject.TEAM;
import model.GridPosition;
import model.ITargetable;
import model.ReadiedAction;
import model.Unit;
import model.World;

public class ActionRunner implements IGameObjectListener{
	private static final int PERFORM_ACTION_CHECK_DELAY = 50;
	
	private World world;
	private ActionLoop actionLoop;
	private ActionQueue actionQueue;
	private Set<Unit.TEAM> activeTeams;
	private Unit activePlayer = null;
	
	public ActionRunner(World world) {
		this.world = world;
		actionQueue = new ActionQueue( world );
		activeTeams = new HashSet<Unit.TEAM>();
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
		Watcher.registerGameObjectListener(this);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;
			activeTeams.add(unit.getTeam());
			Watcher.unitAdded(unit);
		}
	}
	
	public void defeatGameObject(Unit unit) {
		actionQueue.clearUnitActions(unit);
		actionQueue.appendAction(Ability.get(Ability.ID.DEATH), unit, unit);
//		if (unit.equals(activePlayer))
//			Watcher.activePlayerAbilityQueuedChanged(actionQueue.getQueueFor(activePlayer));
		reportDefeat(unit);
	}
	
	public void removeGameObject(GameObject gameObject) {
		System.out.print("removeGameObject(" + gameObject + ")");
		world.remove(gameObject);
		if (gameObject instanceof Unit) {
			Unit unit = (Unit) gameObject;	
			actionQueue.clearUnitActions(unit);
			Watcher.unitRemoved(unit);
		}
	}
	
	public void clearUnitActions(Unit unit) {
		actionQueue.clearUnitActions(unit);
	}
	
	private void reportDefeat(Unit unit) {
		Watcher.unitDefeated(unit);
		Watcher.unitRemoved(unit);
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
			Watcher.teamDefeated(team);
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
		ReadiedAction[] actions = null;
		boolean firstPass = true;
		boolean foundInvalid = false;
		while (firstPass || foundInvalid) {
			firstPass = false;
			foundInvalid = false;
			actions = actionQueue.getReadyActions();
			for (ReadiedAction action : actions) {
				if (!isValidAction(action) || !isValidAction(Pathing.getFirstStepToUseAction(action))) {
					actionQueue.removeAction(action);
					foundInvalid = true;
				}
			}
		}
		for (ReadiedAction action : actions) {
			ReadiedAction firstStep = Pathing.getFirstStepToUseAction(action);
			if (!isValidAction(firstStep)) {
				actionQueue.removeAction(action);
			}
			if (firstStep != action) {
				actionQueue.prependAction(firstStep);
			}
			perform(firstStep);
		}
	}

	/**
	 * Perform the action: animate, effect other game objects, and update internal scheduling
	 * @param action
	 */
	private void perform(final ReadiedAction action) {
		Unit source = action.getSource();
		action.activate();
		if (source.equals(activePlayer))
			Watcher.activePlayerAbilityQueueChanged(actionQueue.getQueueFor(activePlayer));
		if (action.isComplete() ) {
			actionQueue.completeAction(action); //Complete, so remove from queue
		}
	}
	
	public void completeAction( ReadiedAction action ) {
		Unit source = action.getSource();
		if (source.getTeam() == TEAM.PLAYER)
			Watcher.playerUsedAbility(action);
	}

	private boolean isValidAction(ReadiedAction action) {
		boolean validity = true;
		ITargetable target = action.getTarget();
		if (action.getAbility().getSelectionTargetType() != Ability.TARGET_TYPE.DEAD && target instanceof GameObject) {
			validity = ((GameObject)target).isAlive();
		}
		return validity;
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
		world.setFocusTarget(unit);
		Watcher.changedActivePlayer(unit);
		Watcher.activePlayerAbilityQueueChanged(actionQueue.getQueueFor(activePlayer));
	}

	public void reset() {
		Watcher.reset(); //here ???
		actionQueue.clear();
		world.reset();
		actionLoop.pause();
		activePlayer = null;
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
		Watcher.changedPlayerTeam(playerObjects);
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
	
	private class ActionLoop {
		private Thread thread;
		private boolean running;
		
		public void play() {
			if (thread == null || !thread.isAlive()) {
				running = true;
				thread = new Thread() {
					@Override
					public void run() {
						long lastLoopDuration = 0;
						while(running) {
							if (lastLoopDuration < PERFORM_ACTION_CHECK_DELAY) {
								try {
									Thread.sleep(PERFORM_ACTION_CHECK_DELAY - lastLoopDuration);
								} catch (InterruptedException e) {
									break;
								}
							}
							else {
								System.out.println("Warning: Last loop took too long: " + lastLoopDuration + "ms");	
							}
							if (running) {
								long startTime = System.currentTimeMillis();
								actionQueue.incrementTimeWaitForUnit(PERFORM_ACTION_CHECK_DELAY, calcActivePlayer());
								ActionRunner.this.checkNextAction();
								lastLoopDuration = System.currentTimeMillis() - startTime;
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
