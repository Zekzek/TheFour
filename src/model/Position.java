package model;

public class Position {
	private int x = 0;
	private int y = 0;
	private int width = 1;
	private int height = 1;
	
	public Position(int x, int y) {
		this(x, y, 1, 1);
	}
	
	public Position(int x, int y, int width, int height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean intersects(Position otherPosition) {
		return x + width > otherPosition.x &&
				y + height > otherPosition.y &&
				x < otherPosition.x + otherPosition.width &&
				y < otherPosition.y + otherPosition.height;
	}
	
	public int getDistanceTo(Position otherPosition) {
		int dx = x - otherPosition.getX();
		int dy = y - otherPosition.getY();
		
		if (dx < 0) dx = -dx;
		if (dy < 0) dy = -dy;
		
		return dx + dy;
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
		Position other = (Position) obj;
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
