package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class World {
	
	private static Map<Position,TallObject> contents = new ConcurrentHashMap<Position, TallObject>();
	
	private World() {
	}
	
	public static void addTallObject(TallObject tallObject, int x, int y) {
		Position pos = new Position(x, y, 1, 1);
		while (contents.get(pos) != null) {
			pos.setY(pos.getY() + 1);
		}
		contents.put(pos, tallObject);
		tallObject.setPos(pos.getX(), pos.getY());
	}
	
	public static void remove(int x, int y) {
		contents.remove(new Position(x, y, 1, 1));
	}
	
	public static void remove(TallObject tallObject) {
		contents.remove(tallObject.getPos());
	}
	
	public static boolean moveObject(TallObject tallObject, int x, int y) {
		Position pos = new Position(x, y, 1, 1);
		if (contents.get(pos) == null) {
			contents.put(pos, tallObject);
			contents.remove(tallObject.getPos());
			tallObject.setPos(x, y);
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> getSortedContentsWithin(Position pos, Class<T> theClass) {
		ArrayList<T> subset = new ArrayList<T>();
		for (int y = pos.getY(); y < pos.getY() + pos.getHeight(); y++) {
			for (int x = pos.getX(); x < pos.getX() + pos.getWidth(); x++) {
				TallObject tallObject = contents.get(new Position(x, y, 1, 1));
				if (tallObject != null && theClass.isAssignableFrom(tallObject.getClass())) {
					subset.add((T)tallObject);
				}
			}
		}
		return subset;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> ArrayList<T> getSortedLivingContentsWithin(Position pos, Class<T> theClass) {
		ArrayList<T> subset = new ArrayList<T>();
		for (int y = pos.getY(); y < pos.getY() + pos.getHeight(); y++) {
			for (int x = pos.getX(); x < pos.getX() + pos.getWidth(); x++) {
				TallObject tallObject = contents.get(new Position(x, y, 1, 1));
				if (tallObject != null && tallObject.isAlive() && theClass.isAssignableFrom(tallObject.getClass())) {
					subset.add((T)tallObject);
				}
			}
		}
		return subset;
	}
	
	public static ArrayList<Unit> getTargets(Unit source, Ability ability, Position pos) {
		Unit.TEAM team = source.getTeam();
		Ability.OUTCOME outcome = ability.getOutcome();
		ArrayList<Unit.TEAM> targetTeams = new ArrayList<Unit.TEAM>();
		ArrayList<Unit> targets = new ArrayList<Unit>();
		
		if (outcome == Ability.OUTCOME.PERSONAL) {
			targets.add(source);
			return targets;
		} else if (outcome == Ability.OUTCOME.UBIQUITOUS) {
			return getSortedLivingContentsWithin(pos, Unit.class);
		} else if (outcome == Ability.OUTCOME.HARMFUL) {
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
		} else if (outcome == Ability.OUTCOME.HELPFUL) {
			if (team == Unit.TEAM.PLAYER || team == Unit.TEAM.ALLY) {
				targetTeams.add(Unit.TEAM.PLAYER);
				targetTeams.add(Unit.TEAM.ALLY);
			} else if (team == Unit.TEAM.ENEMY1) {
				targetTeams.add(Unit.TEAM.ENEMY1);
			} else if (team == Unit.TEAM.ENEMY2) {
				targetTeams.add(Unit.TEAM.ENEMY2);
			}
		}
		
		ArrayList<Unit> units = getSortedLivingContentsWithin(pos, Unit.class);
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

	public static TallObject getTallObject(Position pos) {
		return contents.get(pos);
	}
}
