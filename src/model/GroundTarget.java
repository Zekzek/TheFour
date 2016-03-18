package model;

public class GroundTarget implements ITargetable {

	private GridPosition pos;
	
	public GroundTarget(int x, int y) {
		this(new GridPosition(x, y));
	}
	
	public GroundTarget(GridPosition pos) {
		this.pos = pos;
	}
	
	@Override
	public GridPosition getPos() {
		return pos;
	}
	
	@Override
	public String toString() {
		return "GroundTarget (" + pos.getX() + "," + pos.getY() + ")";
	}
}
