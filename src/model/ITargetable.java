package model;

public interface ITargetable {
	public GridPosition getPos();
	public boolean isInTargetList();
	public void setInTargetList(boolean selectedTarget);
	public boolean isSelecetedTarget();
	public void setSelectedTarget(boolean selectedTarget);
}
