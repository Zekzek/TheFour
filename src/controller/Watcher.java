package controller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.GameObject;
import model.GameObject.TEAM;
import model.GridPosition;
import model.ReadiedAction;
import model.Unit;
import view.IGridClickListener;
import view.IMenuListener;

public class Watcher {
	
	private static Set<IBattleListener> battleListeners = new HashSet<IBattleListener>();
	private static Set<IPlayerListener> playerListeners = new HashSet<IPlayerListener>();
	private static Set<IGameObjectListener> gameObjectListeners = new HashSet<IGameObjectListener>();
	private static Set<IMenuListener> menuListeners = new HashSet<IMenuListener>();
	private static Set<IGridClickListener> gridClickListeners = new HashSet<IGridClickListener>();
	
	// MANAGE LISTENERS
	
	public static void registerBattleListener(IBattleListener listener) {
		synchronized(battleListeners) {
			battleListeners.add(listener);
		}
	}
	
	public static void unregisterBattleListener(IBattleListener listener) {
		synchronized(battleListeners) {
			battleListeners.remove(listener);
		}
	}
	
	public static void registerPlayerListener(IPlayerListener listener) {
		synchronized(playerListeners) {
			playerListeners.add(listener);
		}
	}
	
	public static void unregisterPlayerListener(IPlayerListener listener) {
		synchronized(playerListeners) {
			playerListeners.remove(listener);
		}
	}
	
	public static void registerGameObjectListener(IGameObjectListener listener) {
		synchronized(gameObjectListeners) {
			gameObjectListeners.add(listener);
		}
	}
	
	public static void unregisterGameObjectListener(IGameObjectListener listener) {
		synchronized(gameObjectListeners) {
			gameObjectListeners.remove(listener);
		}
	}
	
	public static void registerMenuListener(IMenuListener listener) {
		synchronized(menuListeners) {
			menuListeners.add(listener);
		}
	}
	
	public static void unregisterMenuListener(IMenuListener listener) {
		synchronized(menuListeners) {
			menuListeners.remove(listener);
		}
	}
	
	public static void registerGridClickListener(IGridClickListener listener) {
		synchronized(gridClickListeners) {
			gridClickListeners.add(listener);
		}
	}
	
	public static void unregisterGridClickListener(IGridClickListener listener) {
		synchronized(gridClickListeners) {
			gridClickListeners.remove(listener);
		}
	}
	
	public static void reset() {
		synchronized(battleListeners) {
			battleListeners.clear();
		}
		synchronized(playerListeners) {
			playerListeners.clear();
		}
		synchronized(gameObjectListeners) {
			gameObjectListeners.clear();
		}
		synchronized(menuListeners) {
			menuListeners.clear();
		}
		synchronized(gridClickListeners) {
			gridClickListeners.clear();
		}
	}

	// ALERT BATTLE LISTENERS
	
	public static void unitAdded(Unit unit) {
		synchronized(battleListeners) {
			for (IBattleListener listener : battleListeners)
				listener.onUnitAdded(unit);
		}
	}

	public static void unitRemoved(Unit unit) {
		synchronized(battleListeners) {
			for (IBattleListener listener : battleListeners)
				listener.onUnitRemoved(unit);
		}
	}

	public static void unitDefeated(Unit unit) {
		synchronized(battleListeners) {
			for (IBattleListener listener : battleListeners)
				listener.onUnitDefeated(unit);
		}
	}

	public static void teamDefeated(TEAM team) {
		synchronized(battleListeners) {
			for (IBattleListener listener : battleListeners)
				listener.onTeamDefeated(team);
		}
	}
	
	public static void unitChangedTeam(Unit unit) {
		synchronized(battleListeners) {
			for (IBattleListener listener : battleListeners)
				listener.onUnitChangedTeam(unit);
		}
	}

	// ALERT PLAYER LISTENERS
	
	public static void changedActivePlayer(Unit unit) {
		synchronized(playerListeners) {
			for (IPlayerListener listener : playerListeners)
				listener.onChangedActivePlayer(unit);
		}
	}

	public static void changedMostReadyPlayer(Unit unit) {
		synchronized(playerListeners) {
			for (IPlayerListener listener : playerListeners)
				listener.onChangedMostReadyPlayer(unit);
		}
	}

	public static void activePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {
		synchronized(playerListeners) {
			for (IPlayerListener listener : playerListeners)
				listener.onActivePlayerAbilityQueueChanged(actions);
		}
	}

	public static void playerUsedAbility(ReadiedAction action) {
		synchronized(playerListeners) {
			for (IPlayerListener listener : playerListeners)
				listener.onPlayerUsedAbility(action);
		}
	}

	public static void changedPlayerTeam(Set<GameObject> playerObjects) {
		synchronized(playerListeners) {
			for (IPlayerListener listener : playerListeners)
				listener.onChangedPlayerTeam(playerObjects);
		}
	}
	
	// ALERT GAME OBJECT LISTENERS
	
	public static void objectDeath(GameObject object) {
		synchronized(gameObjectListeners) {
			for (IGameObjectListener listener : gameObjectListeners)
				listener.onObjectDeath(object);
		}
	}

	public static void objectTeamChange(GameObject object) {
		synchronized(gameObjectListeners) {
			for (IGameObjectListener listener : gameObjectListeners)
				listener.onObjectTeamChange(object);
		}
	}
	
	// ALERT MENU LISTENERS
	
	public static void sceneComplete() {
		synchronized(menuListeners) {
			for (IMenuListener listener : menuListeners)
				listener.onSceneComplete();
		}
	}
	
	// ALERT GRID CLICK LISTENERS
	
	public static void gridClick(GridPosition pos) {
		synchronized(gridClickListeners) {
			for (IGridClickListener listener : gridClickListeners)
				listener.onGridClick(pos);
		}
	}
}
