package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.GameObject.TEAM;
import view.SpriteSheet.TERRAIN;
import controller.MapBuilder;
import controller.ProximityTrigger;
import controller.TemplateReader;
import controller.Trigger;

public class World {
	private static final GridRectangle MAIN_SCREEN_RECTANGLE = new GridRectangle(0, 0, 24, 22);
	private Map<GridPosition,GameObject> contents = new ConcurrentHashMap<GridPosition, GameObject>();
	
	private Set<Trigger> triggers = new HashSet<Trigger>();
	private ITargetable focusTarget;
	private ITargetable questTarget;
	
	public World() {
		initializeStructures();
	}
	
	private void initializeStructures() {
		TemplateReader objectTemplate = new TemplateReader("/resource/img/templates/objects.png");
		for (int x = 0; x < objectTemplate.getWidth(); x++) {
			for (int y = 0; y < objectTemplate.getHeight(); y++) {
				Color color = objectTemplate.getColorAt(x, y);
				boolean reddish = color.getRed() > 125;
				boolean greenish = color.getGreen() > 125;
				boolean blueish = color.getBlue() > 125;
				boolean visible = color.getAlpha() > 125;
				
				if (visible) {
					if (greenish) {
						if (!reddish && !blueish) {//green
							addTallObject(Structure.get(Structure.ID.TREE, MapBuilder.getClimateType(x, y)), x, y);
						}
					} else if (!reddish && !blueish) {//black
						addTallObject(Structure.get(Structure.ID.WALL, MapBuilder.getClimateType(x, y)), x, y);
					}
				}
			}
		}
	}
	
	public void addTallObject(GameObject tallObject, int x, int y) {
		GridPosition pos = new GridPosition(x, y);
		while (contents.get(pos) != null) {
			pos.setY(pos.getY() + 1);
		}
		contents.put(pos, tallObject);
		tallObject.updateWorldPos(this, pos.getX(), pos.getY());
	}
	
	public boolean moveObject(GameObject tallObject, int x, int y) {
		GridPosition pos = new GridPosition(x, y);
		if (contents.get(pos) == null) {
			contents.put(pos, tallObject);
			contents.remove(tallObject.getPos());
			tallObject.updateWorldPos(this, x, y);
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

	public void remove(GameObject tallObject) {
		contents.remove(tallObject.getPos());
	}
	
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getSortedContentsWithin(GridRectangle pos, Class<T> theClass) {
		ArrayList<T> subset = new ArrayList<T>();
		for (int y = pos.getY(); y < pos.getY() + pos.getHeight(); y++) {
			for (int x = pos.getX(); x < pos.getX() + pos.getWidth(); x++) {
				GameObject tallObject = contents.get(new GridPosition(x, y));
				if (tallObject != null && theClass.isAssignableFrom(tallObject.getClass())) {
					subset.add((T)tallObject);
				}
			}
		}
		return subset;
	}
	
	public ArrayList<ITargetable> getTargets(Unit source, Ability ability) {
		MAIN_SCREEN_RECTANGLE.setCenter(focusTarget.getPos());
		Ability.TARGET_TYPE outcome = ability.getSelectionTargetType();
		ArrayList<ITargetable> targets = new ArrayList<ITargetable>();
		
		if (ability.getId() == Ability.ID.MOVE) {
			for (GridPosition pos : getTraversableCells(MAIN_SCREEN_RECTANGLE)) {
				targets.add(new GroundTarget(pos));
			}
			return targets;
		}
		if (outcome == Ability.TARGET_TYPE.SELF) {
			targets.add(source);
			return targets;
		}
		if (outcome == Ability.TARGET_TYPE.ALL) {
			return getSortedContentsWithin(MAIN_SCREEN_RECTANGLE, ITargetable.class);
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
		
		ArrayList<GameObject> objects = getSortedContentsWithin(MAIN_SCREEN_RECTANGLE, GameObject.class);
		for (GameObject object : objects){
			if (targetTeams.contains(object.getTeam()) && object.isAlive()) {
				targets.add(object);
			}
		}
		
		return targets;
	}

	public void reset() {
		contents.clear();
	}

	public GameObject getTallObject(GridPosition pos) {
		return contents.get(pos);
	}

	private Collection<GridPosition> getTraversableCells(GridRectangle rect) {
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
	
	public Collection<GridPosition> getTraversableNeighbors(GridPosition pos) {
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
	
	private boolean isTraversable(GridPosition pos) {
		return getTallObject(pos) == null && MapBuilder.getTerrainType(pos) != TERRAIN.WATER;
	}

	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}
	
	public ITargetable getFocusTarget() {
		return focusTarget;
	}

	public void setFocusTarget(ITargetable focusTarget) {
		this.focusTarget = focusTarget;
	}

	public GridPosition getQuestTargetPosition() {
		if (questTarget == null)
			return null;
		return questTarget.getPos();
	}

	public void setQuestTarget(ITargetable questTarget) {
		this.questTarget = questTarget;
	}
	
	public Set<GameObject> getContentsOnTeam(TEAM team) {
		Set<GameObject> teamObjects = new HashSet<GameObject>();
		for (GameObject object : contents.values()) {
			if (object.getTeam() == team) {
				teamObjects.add(object);
			}
		}
		return teamObjects;
	}

	public GridRectangle getMainScreenRectangle() {
		MAIN_SCREEN_RECTANGLE.setCenter(focusTarget.getPos());
		return MAIN_SCREEN_RECTANGLE;
	}
}
