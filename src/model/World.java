package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class World {
	
	private static Map<GridPosition,TallObject> contents = new ConcurrentHashMap<GridPosition, TallObject>();
	
	private World() {
	}
	
	public static void addTallObject(TallObject tallObject, int x, int y) {
		GridPosition pos = new GridPosition(x, y);
		while (contents.get(pos) != null) {
			pos.setY(pos.getY() + 1);
		}
		contents.put(pos, tallObject);
		tallObject.updateeWorldPos(pos.getX(), pos.getY());
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
			tallObject.updateeWorldPos(x, y);
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
	
	@SuppressWarnings("unchecked")
	private static <T> ArrayList<T> getSortedLivingContentsWithin(GridRectangle pos, Class<T> theClass) {
		ArrayList<T> subset = new ArrayList<T>();
		for (int y = pos.getY(); y < pos.getY() + pos.getHeight(); y++) {
			for (int x = pos.getX(); x < pos.getX() + pos.getWidth(); x++) {
				TallObject tallObject = contents.get(new GridPosition(x, y));
				if (tallObject != null && tallObject.isAlive() && theClass.isAssignableFrom(tallObject.getClass())) {
					subset.add((T)tallObject);
				}
			}
		}
		return subset;
	}
	
	public static ArrayList<Unit> getTargets(Unit source, Ability ability, GridRectangle rect) {
		Unit.TEAM team = source.getTeam();
		Ability.TARGET_TYPE outcome = ability.getSelectionTargetType();
		ArrayList<Unit.TEAM> targetTeams = new ArrayList<Unit.TEAM>();
		ArrayList<Unit> targets = new ArrayList<Unit>();
		
		if (outcome == Ability.TARGET_TYPE.SELF) {
			targets.add(source);
			return targets;
		} else if (outcome == Ability.TARGET_TYPE.ALL) {
			return getSortedLivingContentsWithin(rect, Unit.class);
		} else if (outcome == Ability.TARGET_TYPE.ENEMY) {
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
		
		ArrayList<Unit> units = getSortedLivingContentsWithin(rect, Unit.class);
		for (Unit unit : units){
			if (targetTeams.contains(unit.getTeam())) {
				targets.add(unit);
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

	public static Collection<GridPosition> getOpenNeighbors(GridPosition pos) {
		Set<GridPosition> openNeighbors = new HashSet<GridPosition>();
		GridPosition above = new GridPosition(pos.getX(), pos.getY() - 1);
		GridPosition below = new GridPosition(pos.getX(), pos.getY() + 1);
		GridPosition left = new GridPosition(pos.getX() - 1, pos.getY());
		GridPosition right = new GridPosition(pos.getX() + 1, pos.getY());
		if (getTallObject(above) == null) {
			openNeighbors.add(above);
		}
		if (getTallObject(below) == null) {
			openNeighbors.add(below);
		}
		if (getTallObject(left) == null) {
			openNeighbors.add(left);
		}
		if (getTallObject(right) == null) {
			openNeighbors.add(right);
		}
		return openNeighbors;
	}
}
