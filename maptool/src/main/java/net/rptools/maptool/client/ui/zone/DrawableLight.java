/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.zone;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Area;

import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.drawing.DrawablePaint;

public class DrawableLight implements Comparable<DrawableLight>{

	private DrawablePaint paint;
	private Area area;
	private LightSource.Type type;
	private int lumens;

	public DrawableLight(LightSource.Type type, DrawablePaint paint, Area area, int lumens) {
		super();
		this.paint = paint;
		this.area = area;
		this.type = type;
		this.lumens = lumens;
	}

	public DrawablePaint getPaint() {
		return paint;
	}

	public Area getArea() {
		return area;
	}

	public LightSource.Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "DrawableLight[" + area.getBounds() + ", " + paint.getClass().getName() + "]";
	}

	@Override
	/**
	 * Compares based on draw order, with the intent that greater(later) lights will be drawn on top of earlier lights.
	 */
	public int compareTo(DrawableLight other) {
		// -1 means this goes first.
		if(this.lumens != other.lumens) {
			int lta, loa;
			lta = Math.abs(this.lumens);
			loa = Math.abs(other.lumens);
			if (lta == loa) {
				// lumens: -10 and 10
				// 10 goes first because -10 takes precedence.
				return this.lumens < 0 ? 1 : -1;
			}
			// lowest absolute lumens goes first.
			return lta-loa;
		}
		Paint paintT, paintO;
		paintT = this.paint.getPaint();
		paintO = other.paint.getPaint();
		if(! paintT.equals(paintO)) {
			if(paintT instanceof Color && paintO instanceof Color) {
				Color colorT, colorO;
				colorT = (Color) paintT;
				colorO = (Color) paintO;
				int sumT, sumO;
				sumT = colorT.getRed() + colorT.getGreen() + colorT.getBlue();
				sumO = colorO.getRed() + colorO.getGreen() + colorO.getBlue();
				//Darker goes first
				return sumT-sumO;
			}
		}
		
		return 0;
	}

}
