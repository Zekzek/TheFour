package model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import controller.Watcher;
import model.GameObject.TEAM;

public class ActionQueue {
	private static final SoonestActionComparator SOONEST_READIED_ACTION = new SoonestActionComparator();
	private static final LinkedList<ReadiedAction> actionQueue = new LinkedList<ReadiedAction>();
	private static World world;
	
	private Unit mostReadyPlayer = null;
	private long time;

	public ActionQueue( World world ) {
		ActionQueue.world = world;
		ReadiedAction.setActionQueue( this );	
	}
	
	public static void setWorld(World world) {
		ActionQueue.world = world;
	}
	
	/**
	 * Add an action to the end of the queue.
	 * @param ability ability to perform
	 * @param source unit to perform the ability
	 * @param target target for the ability
	 */
	public void appendAction(Ability ability, Unit source, ITargetable target) {
		System.out.println("\t" + source + " has prepared " + ability + " for " + target);
		synchronized(this) {
			ReadiedAction action = new ReadiedAction(ability, source, target, getCompletionTime(source));
			actionQueue.add(action);
			sort();
//			if (source.equals(activePlayer))
//				activePlayerAbilityQueuedChanged();
		}
	}
	
	/**
	 * Insert an action at the front of the queue. 
	 *   Warning: This call bypasses setting the action start time
	 * @param action action to insert
	 */
	public void prependAction(ReadiedAction action) {
		System.out.println("\t" + action.getSource() + " has immediately prepared " + action.getAbility() + " for " + action.getTarget());
		synchronized(this) {
			delay(action.getSource(), action.calcDelay());
			actionQueue.add(action);
			sort();
		}
	};
	
	/**
	 * Insert an action at the front of the queue.
	 * @param ability ability to perform
	 * @param source unit to perform the ability
	 * @param target target for the ability
	 */
	public void prependAction(Ability ability, Unit source, ITargetable target) {
		System.out.println("\t" + source + " has immediately prepared " + ability + " for " + target);
		ReadiedAction action = new ReadiedAction(ability, source, target, time);
//		action.addDoAtMid(doAtMid);
//		action.addDoAtEnd(doAtEnd);
		synchronized(this) {
			delay(source, action.calcDelay());
			actionQueue.add(action);
			sort();
		}
	};

	/**
	 * Mark the action as complete, removing it from the queue
	 * @param action
	 */
	public void completeAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		System.out.println("\t" + source + " is done using " + ability);
		synchronized(this) {
			actionQueue.remove(action);
			sort();
		}
		if (source.isPlayerTeam())
			Watcher.playerUsedAbility(action);
	};
	
	/**
	 * Abandon an action in the queue, recovering the time spent.
	 * @param action
	 */
	public void removeAction(ReadiedAction action) {
		Ability ability = action.getAbility();
		Unit source = action.getSource();
		System.out.println("\t" + source + " has decided not to use " + ability);
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
					otherAction.delay(-action.calcDelay());
				}
			}
			sort();
//			if (source.equals(activePlayer))
//				activePlayerAbilityQueuedChanged();
		}
	}

	/**
	 * Remove all actions with unit as the source
	 * @param unit
	 */
	public void clearUnitActions(Unit unit) {
		synchronized(this) {
			Iterator<ReadiedAction> actions = actionQueue.iterator();
			while (actions.hasNext()) {
				if (unit.equals(actions.next().getSource())) {
					actions.remove();
				}
			}
			sort();
		}
	}

	/**
	 * Delay all actions performed by a unit.
	 * @param unit
	 * @param delay
	 */
	public void delay(Unit unit, int delay) {
		synchronized(this) {
			for (ReadiedAction action : actionQueue) {
				if (action.getSource().equals(unit)) {
					action.delay(delay);
				}
			}
			sort();
		}
	}
	
	private void sort() {
		Collections.sort(actionQueue, SOONEST_READIED_ACTION);
		calcMostReadyPlayer();
	}


	private void calcMostReadyPlayer() {
		long lowestBusyness = mostReadyPlayer==null ? Long.MAX_VALUE : getCompletionTime(mostReadyPlayer);
		boolean changed = false;
		synchronized(this) {
			Set<GameObject> playerObjects = world.getContentsOnTeam(TEAM.PLAYER);
			for (GameObject playerObject : playerObjects) {
				if (playerObject instanceof Unit) {
					Unit combatant = (Unit) playerObject;
					long busyness = getCompletionTime(combatant);
					if (busyness < lowestBusyness) {
						changed = true;
						mostReadyPlayer = combatant;
						lowestBusyness = busyness;
					}
				}
			}
		}
		if (changed)
			Watcher.changedMostReadyPlayer(mostReadyPlayer);
	}

	/**
	 * Get the player needing to schedule another action soonest
	 * @return
	 */
	public Unit getMostReadyPlayer() {
		return mostReadyPlayer;
	}
	
	public Iterator<ReadiedAction> getActionQueueIterator() {
		return actionQueue.iterator();
	}
	
	/**
	 * Get start time for the last action queued for unit
	 * @param unit
	 * @return
	 */
	public long getLastScheduledTime(Unit unit) {
		long lastTime = time - 1;
		synchronized(this) {
			Iterator<ReadiedAction> iterator = actionQueue.iterator();
			while (iterator.hasNext()) {
				ReadiedAction action = iterator.next();
				if (action.getSource().equals(unit)) {
					lastTime = action.getStartTime();
				}
			}
		}
		return lastTime;
	}
	
	/**
	 * Get time of completion for the last action queued for unit
	 * @param unit
	 * @return
	 */
	public long getCompletionTime(Unit unit) {
		long completionTime = time;
		synchronized(this) {
			Iterator<ReadiedAction> iterator = actionQueue.iterator();
			while (iterator.hasNext()) {
				ReadiedAction action = iterator.next();
				if (action.getSource().equals(unit)) {
					completionTime = action.getStartTime() + action.calcDelay();
				}
			}
		}
		return completionTime;
	}
	
	/**
	 * Determine if the action is queued
	 * @param source
	 * @param ability
	 * @param target
	 * @return
	 */
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
	
	public boolean isEmpty() {
		return actionQueue.isEmpty();
	}
	
	/**
	 * Attempt to increment the time, but wait for a unit to queue actions
	 * @param timeIncrement
	 * @param unit
	 */
	public void incrementTimeWaitForUnit(int timeIncrement, Unit unit) {
		long lastScheduledTime = unit == null ? Long.MAX_VALUE : getLastScheduledTime(unit);
		time = Math.max(time, Math.min(time + timeIncrement, lastScheduledTime));
	}
	
	/**
	 * Collect a list of all actions ready to be performed
	 * @return
	 */
	public ReadiedAction[] getReadyActions() {
		Vector<ReadiedAction> readyActions = new Vector<ReadiedAction>();
		Iterator<ReadiedAction> actions = getActionQueueIterator();
		while (actions.hasNext()) {
			ReadiedAction nextAction = actions.next();
			if (nextAction.getStartTime() <= time)
				readyActions.add(nextAction);
			else
				break;
		}
		return readyActions.toArray(new ReadiedAction[0]);
	}
	
	/**
	 * Collect all actions with unit as the source
	 * @param unit
	 * @return
	 */
	public List<ReadiedAction> getQueueFor(Unit unit) {
		List<ReadiedAction> actions = new LinkedList<ReadiedAction>();
		for (ReadiedAction action : actionQueue) {
			if (action.getSource().equals(unit)) {
				actions.add(action);
			}
		}
		return actions;
	}
	
	/**
	 * Reset all parameters to begin anew
	 */
	public void clear() {
		time = 0;
		actionQueue.clear();
		mostReadyPlayer = null;
	}
	
	private static class SoonestActionComparator implements Comparator<ReadiedAction> {
		@Override
		public int compare(ReadiedAction action1, ReadiedAction action2) {
			return (int)(action1.getStartTime() - action2.getStartTime());
		}
	}
}
