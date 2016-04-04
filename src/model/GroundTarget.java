package model;

public class GroundTarget implements ITargetable {

	private GridPosition pos;
	private boolean inTargetList;
	private boolean selectedTarget;
	
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

	@Override
	public boolean isInTargetList() {
		return inTargetList;
	}

	@Override
	public void setInTargetList(boolean inTargetList) {
		this.inTargetList = inTargetList;
	}

	@Override
	public boolean isSelecetedTarget() {
		return selectedTarget;
	}

	@Override
	public void setSelectedTarget(boolean selectedTarget) {
		this.selectedTarget = selectedTarget;
	}
}
