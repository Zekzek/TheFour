package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import view.GraphicsPanel;

public class GroundTarget implements ITargetable {

	private GridPosition pos;
	private boolean inTargetList;
	
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

	public void paint(Graphics2D g2) {
		if (isInTargetList()) {
			AffineTransform savedTransorm = g2.getTransform();
			GridRectangle screenRectangle = GraphicsPanel.getScreenRectangle();

			g2.translate(GraphicsPanel.CELL_WIDTH * (pos.getX()-screenRectangle.getX()), 
					GraphicsPanel.TERRAIN_CELL_HEIGHT * (pos.getY()-screenRectangle.getY()));
			if (GraphicsPanel.isHoverGrid(pos)) {
				g2.setColor(new Color(255, 172, 0, 100));
				g2.fillRect(0, 0, GraphicsPanel.CELL_WIDTH, GraphicsPanel.TERRAIN_CELL_HEIGHT);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(3));
				g2.setColor(Color.ORANGE);
				g2.drawRect(0, 0, GraphicsPanel.CELL_WIDTH, GraphicsPanel.TERRAIN_CELL_HEIGHT);
				g2.setStroke(oldStroke);
			} else {
				g2.setColor(new Color(200, 200, 200, 100));
				g2.fillRect(0, 0, GraphicsPanel.CELL_WIDTH, GraphicsPanel.TERRAIN_CELL_HEIGHT);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(3));
				g2.setColor(Color.LIGHT_GRAY);
				g2.drawRect(0, 0, GraphicsPanel.CELL_WIDTH, GraphicsPanel.TERRAIN_CELL_HEIGHT);
				g2.setStroke(oldStroke);
			}
			g2.setTransform(savedTransorm);
		}
	}
}
