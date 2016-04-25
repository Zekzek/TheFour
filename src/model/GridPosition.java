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
}
