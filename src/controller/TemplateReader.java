package controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import view.GraphicsPanel;
import controller.plot.Plot;

public class TemplateReader {
	public static enum BYTE_TO_PIXEL {A, B, G, R};
	private static final int BYTES_PER_PIXEL = BYTE_TO_PIXEL.values().length;
	
	private final BufferedImage TEMPLATE;
	private final byte[] TEMPLATE_BYTES;
	
	public TemplateReader(String pathToResource) {
		TEMPLATE = GraphicsPanel.loadImage(Plot.class.getResource(pathToResource));
		TEMPLATE_BYTES = ((DataBufferByte)TEMPLATE.getRaster().getDataBuffer()).getData();
	}
	
	public Color getColorAt(int x, int y) {
		int pixelIndex = y * TEMPLATE.getWidth() + x;
		int byteIndex = pixelIndex * BYTES_PER_PIXEL;
		try {
			return new Color(
				(TEMPLATE_BYTES[byteIndex + BYTE_TO_PIXEL.R.ordinal()]) & 0xff,
				(TEMPLATE_BYTES[byteIndex + BYTE_TO_PIXEL.G.ordinal()]) & 0xff,
				(TEMPLATE_BYTES[byteIndex + BYTE_TO_PIXEL.B.ordinal()]) & 0xff,
				(TEMPLATE_BYTES[byteIndex + BYTE_TO_PIXEL.A.ordinal()]) & 0xff);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public int getWidth() {
		return TEMPLATE.getWidth();
	}
	
	public int getHeight() {
		return TEMPLATE.getHeight();
	}
}
