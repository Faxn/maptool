package net.rptools.maptool.client.ui.render;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.geom.Area;

import org.junit.Test;

import junit.framework.TestCase;
import net.rptools.maptool.client.ui.zone.DrawableLight;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;

public class DrawableLightTest extends TestCase {

	
	
	
	private DrawablePaint white;
	private Area area;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		white = new DrawableColorPaint(Color.WHITE);
		area = new Area();
	}

	@Test
	public void testCompareLumens() {
		
		DrawableLight l1, l2, l3;
		l1 = new DrawableLight(LightSource.Type.NORMAL, white, area, 1);
		l2 = new DrawableLight(LightSource.Type.NORMAL, white, area, 10);
		l3 = new DrawableLight(LightSource.Type.NORMAL, white, area, -10);
		
		assertTrue("identiy compare is 0.", l1.compareTo(l1) == 0);
		assertTrue("Lower absolute lumens goes first.", l1.compareTo(l2) < 0);
		assertTrue("Lower absolute lumens goes first.", l1.compareTo(l3) < 0);
		
		assertTrue("light goes before anti-light.", l2.compareTo(l3) < 0);
		
	}

}
