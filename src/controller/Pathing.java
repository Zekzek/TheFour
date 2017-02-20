package controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import model.Ability;
import model.GridPosition;
import model.GroundTarget;
import model.ReadiedAction;
import model.Unit;
import model.World;

public class Pathing {
	
	private static final LowestCostPathComparator LOWEST_COST_PATH = new LowestCostPathComparator();
	private static World world;

	public static void setWorld(World world) {
		Pathing.world = world;
	}
	
	public static ReadiedAction getFirstStepToUseAction(ReadiedAction action) {
		Ability.ID abilityId = action.getAbility().getId();
		Unit source = action.getSource();
		
		if (abilityId == Ability.ID.AI_TURN) {
			return getFirstStepToUseAction(source.aiGetAction(action.getStartTime()));
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

	private static boolean targetReachable(ReadiedAction action) {
		return targetReachable(action, action.getSource().getPos());
	}
	
	private static boolean targetReachable(ReadiedAction action, GridPosition sourcePos) {
		GridPosition targetPos = action.getTarget().getPos();
		int range = action.getAbility().getRange();
		return sourcePos.getDistanceFromCenterTo(targetPos) <= range;
	}
	
	private static PathingGridPosition getPathToUseAction(ReadiedAction action) {
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
	
	private static PathingGridPosition getPathToScreenEdge(Unit source) {
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
	
	private static class PathingGridPosition extends GridPosition {
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
	
	private static class LowestCostPathComparator implements Comparator<PathingGridPosition> {
		@Override
		public int compare(PathingGridPosition pos1, PathingGridPosition pos2) {
			return pos1.getCostEstimate() - pos2.getCostEstimate();
		}
	}
}
