package model;

public class GridRectangle {
	private int x;
	private int y;
	private float xOffset;
	private float yOffset;
	private int width;
	private int height;
	
	public GridRectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public GridRectangle(float x, float y, int width, int height) {
		this.x = Math.round(x);
		this.y = Math.round(y);
		setxOffset(x - getX());
		setyOffset(y - getY());
		this.width = width;
		this.height = height;
	}
	
	public GridRectangle(GridPosition pos, int width, int height) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.width = width;
		this.height = height;
		this.xOffset = pos.getxOffset();
		this.yOffset = pos.getyOffset();
	}

	public boolean intersects(GridRectangle otherPosition) {
		return x + width > otherPosition.x &&
				y + height > otherPosition.y &&
				x < otherPosition.x + otherPosition.width &&
				y < otherPosition.y + otherPosition.height;
	}
	
	public int getDistanceFromCenterTo(GridRectangle other) {
		GridPosition center = getCenter();
		GridPosition otherCenter = other.getCenter();
		
		int dx = center.getX() - otherCenter.getX();
		int dy = center.getY() - otherCenter.getY();
		
		if (dx < 0) dx = -dx;
		if (dy < 0) dy = -dy;
		
		return dx + dy;
	}
	
	public void round() {
		setxOffset(0);
		setyOffset(0);
	}
	
	public GridRectangle getPosDifference(GridRectangle other) {
		return new GridRectangle((getX() + getxOffset()) - (other.getX() + other.getxOffset()),
				(getY() + getyOffset()) - (other.getY() + other.getyOffset()), width, height);
	}
	
	public GridRectangle getPosSum(GridRectangle other) {
		return new GridRectangle((getX() + getxOffset()) + (other.getX() + other.getxOffset()),
				(getY() + getyOffset()) + (other.getY() + other.getyOffset()), width, height);
	}
	
	public GridRectangle getPosProduct(float multiplier) {
		return new GridRectangle(getX() * multiplier, getY() * multiplier, width, height);
	}
	
	public GridRectangle getGlidePositionFromCenter(GridRectangle end, float progress) {
		GridRectangle difference = end.getPosDifference(this);
		GridRectangle scaledDifference = difference.getPosProduct(progress);
		return getPosSum(scaledDifference);
	}
	
	public GridPosition getCenter() {
		GridPosition center = new GridPosition(getX() + getWidth()/2, getY() + getHeight()/2);
		center.setxOffset(getxOffset());
		center.setyOffset(getyOffset());
		return center;
	}
	
	public void setCenter(GridPosition position) {
		setX(position.getX() - getWidth()/2);
		setY(position.getY() - getHeight()/2);
		setxOffset(position.getxOffset());
		setyOffset(position.getyOffset());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GridRectangle other = (GridRectangle) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + "]";
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public float getxOffset() {
		return xOffset;
	}
	public void setxOffset(float xOffset) {
		this.xOffset = xOffset;
	}
	public float getyOffset() {
		return yOffset;
	}
	public void setyOffset(float yOffset) {
		this.yOffset = yOffset;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
}
