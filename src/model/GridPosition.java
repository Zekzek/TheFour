package model;

public class GridPosition extends GridRectangle{

	public GridPosition(int x, int y) {
		super(x, y, 1, 1);
	}
	
	public GridPosition(float x, float y) {
		super(Math.round(x), Math.round(y), 1, 1);
		setxOffset(x - getX());
		setyOffset(y - getY());
	}
	
	public GridPosition getPosDifference(GridPosition other) {
		return (GridPosition) super.getPosDifference(other);
	}
	
	public GridPosition getPosSum(GridPosition other) {
		return (GridPosition) super.getPosSum(other);
	}
	
	public GridPosition getPosProduct(float multiplier) {
		return (GridPosition) super.getPosProduct(multiplier);
	}
	
}
