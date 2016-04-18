package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import view.SpriteSheet.TERRAIN;
import controller.MapBuilder;
import controller.ProximityTrigger;
import controller.Trigger;

public class World {
	
	private static Map<GridPosition,TallObject> contents = new ConcurrentHashMap<GridPosition, TallObject>();
	private static Set<Trigger> triggers = new HashSet<Trigger>();
	private World() {
	}
	
	public static void addTallObject(TallObject tallObject, int x, int y) {
		GridPosition pos = new GridPosition(x, y);
		while (contents.get(pos) != null) {
			pos.setY(pos.getY() + 1);
		}
		contents.put(pos, tallObject);
		tallObject.updateWorldPos(pos.getX(), pos.getY());
	}
	
	public static void remove(int x, int y) {
		contents.remove(new GridPosition(x, y));
	}
	
	public static void remove(TallObject tallObject) {
		contents.remove(tallObject.getPos());
	}
	
	public static boolean moveObject(TallObject tallObject, int x, int y) {
		GridPosition pos = new GridPosition(x, y);
		if (contents.get(pos) == null) {
			contents.put(pos, tallObject);
			contents.remove(tallObject.getPos());
			tallObject.updateWorldPos(x, y);
			for (Trigger trigger : triggers) {
				if (trigger instanceof ProximityTrigger) {
					((ProximityTrigger)trigger).checkTrigger(pos);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> getSortedContentsWithin(GridRectangle pos, Class<T> theClass) {
		ArrayList<T> subset = new ArrayList<T>();
		for (int y = pos.getY(); y < pos.getY() + pos.getHeight(); y++) {
			for (int x = pos.getX(); x < pos.getX() + pos.getWidth(); x++) {
				TallObject tallObject = contents.get(new GridPosition(x, y));
				if (tallObject != null && theClass.isAssignableFrom(tallObject.getClass())) {
					subset.add((T)tallObject);
				}
			}
		}
		return subset;
	}
	
	public static ArrayList<ITargetable> getTargets(Unit source, Ability ability, GridRectangle rect) {
		Ability.TARGET_TYPE outcome = ability.getSelectionTargetType();
		ArrayList<ITargetable> targets = new ArrayList<ITargetable>();
		
		if (ability.getId() == Ability.ID.MOVE) {
			for (GridPosition pos : getTraversableCells(rect)) {
				targets.add(new GroundTarget(pos));
			}
			return targets;
		}
		if (outcome == Ability.TARGET_TYPE.SELF) {
			targets.add(source);
			return targets;
		}
		if (outcome == Ability.TARGET_TYPE.ALL) {
			return getSortedContentsWithin(rect, ITargetable.class);
		}
		
		Unit.TEAM team = source.getTeam();
		ArrayList<Unit.TEAM> targetTeams = new ArrayList<Unit.TEAM>();
		if (outcome == Ability.TARGET_TYPE.ENEMY) {
			if (team == Unit.TEAM.PLAYER || team == Unit.TEAM.ALLY) {
				targetTeams.add(Unit.TEAM.ENEMY1);
				targetTeams.add(Unit.TEAM.ENEMY2);
			} else if (team == Unit.TEAM.ENEMY1) {
				targetTeams.add(Unit.TEAM.PLAYER);
				targetTeams.add(Unit.TEAM.ALLY);
				targetTeams.add(Unit.TEAM.ENEMY2);
			} else if (team == Unit.TEAM.ENEMY2) {
				targetTeams.add(Unit.TEAM.PLAYER);
				targetTeams.add(Unit.TEAM.ALLY);
				targetTeams.add(Unit.TEAM.ENEMY1);
			}	
		} else if (outcome == Ability.TARGET_TYPE.ALLY) {
			if (team == Unit.TEAM.PLAYER || team == Unit.TEAM.ALLY) {
				targetTeams.add(Unit.TEAM.PLAYER);
				targetTeams.add(Unit.TEAM.ALLY);
			} else if (team == Unit.TEAM.ENEMY1) {
				targetTeams.add(Unit.TEAM.ENEMY1);
			} else if (team == Unit.TEAM.ENEMY2) {
				targetTeams.add(Unit.TEAM.ENEMY2);
			}
		}
		
		ArrayList<TallObject> objects = getSortedContentsWithin(rect, TallObject.class);
		for (TallObject object : objects){
			if (targetTeams.contains(object.getTeam())) {
				targets.add(object);
			}
		}
		
		return targets;
	}

	public static void reset() {
		contents.clear();
	}

	public static TallObject getTallObject(GridPosition pos) {
		return contents.get(pos);
	}


	private static Collection<GridPosition> getTraversableCells(GridRectangle rect) {
		Set<GridPosition> openCells = new HashSet<GridPosition>();
		for (int x = 0; x < rect.getWidth(); x++) {
			for (int y = 0; y < rect.getHeight(); y++) {
				GridPosition pos = new GridPosition(rect.getX() + x, rect.getY() + y);
				if (isTraversable(pos)) {
					openCells.add(pos);
				}
			}
		}
		return openCells;
	}
	
	public static Collection<GridPosition> getTraversableNeighbors(GridPosition pos) {
		Set<GridPosition> openNeighbors = new HashSet<GridPosition>();
		GridPosition above = new GridPosition(pos.getX(), pos.getY() - 1);
		GridPosition below = new GridPosition(pos.getX(), pos.getY() + 1);
		GridPosition left = new GridPosition(pos.getX() - 1, pos.getY());
		GridPosition right = new GridPosition(pos.getX() + 1, pos.getY());
		if (isTraversable(above)) {
			openNeighbors.add(above);
		}
		if (isTraversable(below)) {
			openNeighbors.add(below);
		}
		if (isTraversable(left)) {
			openNeighbors.add(left);
		}
		if (isTraversable(right)) {
			openNeighbors.add(right);
		}
		return openNeighbors;
	}
	
	private static boolean isTraversable(GridPosition pos) {
		return getTallObject(pos) == null && MapBuilder.getTerrainType(pos) != TERRAIN.WATER;
	}

	public static void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}
}
