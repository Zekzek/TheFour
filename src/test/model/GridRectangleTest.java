package test.model;

import static org.junit.Assert.*;
import model.GridPosition;
import model.GridRectangle;

import org.junit.Test;

public class GridRectangleTest {

	private static final float DELTA = 0.0001f;
	
	@Test
	public void testGridRectangleIntIntIntInt() {
		GridRectangle rect = new GridRectangle(4, 5, 10, 20);
		assertEquals(rect.getX(), 4);
		assertEquals(rect.getY(), 5);
		assertEquals(rect.getWidth(), 10);
		assertEquals(rect.getHeight(), 20);
		assertEquals(rect.getxOffset(), 0, DELTA);
		assertEquals(rect.getxOffset(), 0, DELTA);
	}

	@Test
	public void testGridRectangleFloatFloatIntInt() {
		GridRectangle rect = new GridRectangle(4.7f, 6.2f, 10, 20);
		assertEquals(rect.getX(), 5);
		assertEquals(rect.getY(), 6);
		assertEquals(rect.getWidth(), 10);
		assertEquals(rect.getHeight(), 20);
		assertEquals(rect.getxOffset(), -0.3f, DELTA);
		assertEquals(rect.getyOffset(), 0.2f, DELTA);
	}

	@Test
	public void testGridRectangleGridPositionIntInt() {
		GridPosition pos = new GridPosition(4.7f, 6.2f);
		GridRectangle rect = new GridRectangle(pos, 10, 20);
		assertEquals(5, rect.getX());
		assertEquals(6, rect.getY());
		assertEquals(10, rect.getWidth());
		assertEquals(20, rect.getHeight());
		assertEquals(-0.3, rect.getxOffset(), DELTA);
		assertEquals(0.2, rect.getyOffset(), DELTA);
	}

	@Test
	public void testIntersects() {
		GridRectangle north = new GridRectangle(5, 0, 6, 6);
		GridRectangle east = new GridRectangle(10, 5, 6, 6);
		GridRectangle south = new GridRectangle(5, 10, 6, 6);
		GridRectangle west = new GridRectangle(0, 5, 6, 6);
		GridRectangle northeast = new GridRectangle(0.49f, 0f, 5, 6);
		assertTrue(north.intersects(east));
		assertTrue(east.intersects(south));
		assertTrue(south.intersects(west));
		assertTrue(west.intersects(north));
		assertFalse(north.intersects(south));
		assertFalse(east.intersects(west));
		assertFalse(north.intersects(northeast));
	}

	@Test
	public void testGetDistanceFromCenterTo() {
		GridRectangle north = new GridRectangle(5, 0, 6, 6);
		GridRectangle south = new GridRectangle(5, 10, 6, 6);
		assertEquals(0, north.getDistanceFromCenterTo(north));
		assertEquals(10, north.getDistanceFromCenterTo(south));
	}

	@Test
	public void testRound() {
		GridRectangle rect = new GridRectangle(0.49f, 0.51f, 5, 6);
		rect.round();
		assertEquals(0, rect.getX());
		assertEquals(0, rect.getxOffset(), DELTA);
		assertEquals(1, rect.getY());
		assertEquals(0, rect.getyOffset(), DELTA);
	}

	@Test
	public void testGetPosDifference() {
		GridRectangle north = new GridRectangle(5, 0, 6, 6);
		GridRectangle south = new GridRectangle(5, 10, 6, 6);
		GridRectangle difference = south.getPosDifference(north);
		assertEquals(0, difference.getX());
		assertEquals(10, difference.getY());
	}

	@Test
	public void testGetPosSum() {
		GridRectangle north = new GridRectangle(5, 0, 6, 6);
		GridRectangle south = new GridRectangle(5, 12, 6, 6);
		GridRectangle difference = south.getPosSum(north);
		assertEquals(10, difference.getX());
		assertEquals(12, difference.getY());
	}

	@Test
	public void testGetPosProduct() {
		GridRectangle rect = new GridRectangle(4, -4, 6, 6);
		GridRectangle triple = rect.getPosProduct(3.0f);
		assertEquals(12, triple.getX());
		assertEquals(-12, triple.getY());
		GridRectangle half = rect.getPosProduct(0.5f);
		assertEquals(2, half.getX());
		assertEquals(-2, half.getY());
	}

	@Test
	public void testGetGlidePositionFromCenter() {
		GridRectangle north = new GridRectangle(5, 0, 6, 6);
		GridRectangle south = new GridRectangle(5, 12, 6, 6);
		GridRectangle halfway = north.getGlidePositionFromCenter(south, 0.5f);
		assertEquals(5, halfway.getX());
		assertEquals(6, halfway.getY());
	}

	@Test
	public void testGetCenter() {
		GridRectangle rect = new GridRectangle(5, 0, 5, 6);
		GridPosition center = rect.getCenter();
		assertEquals(7, center.getX());
		assertEquals(3, center.getY());
	}

	@Test
	public void testSetCenter() {
		GridRectangle rect = new GridRectangle(0, 0, 10, 20);
		GridPosition newCenter = new GridPosition(10, 10);
		rect.setCenter(newCenter);
		assertEquals(5, rect.getX());
		assertEquals(0, rect.getY());
		assertEquals(10, rect.getWidth());
		assertEquals(20, rect.getHeight());
	}

}
