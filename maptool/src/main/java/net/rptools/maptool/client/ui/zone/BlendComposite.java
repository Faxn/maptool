package net.rptools.maptool.client.ui.zone;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import net.rptools.lib.CodeTimer;

public class BlendComposite implements Composite {
	
	public static enum BlendMode{
		ADD, MULTIPLY;
	}
	
	static CodeTimer timer = new CodeTimer("BlendComposite");
	
	
	//Force alpha of every pixel to this.
	float alpha;

	private BlendMode blendMode;
	
	public BlendComposite(float alpha) {
		this.blendMode = BlendMode.ADD;
		this.alpha=alpha;
	}
	
	public BlendComposite(BlendMode mode, float alpha) {
		this.blendMode = mode;
		this.alpha=alpha;
	}
	
	public BlendComposite(BlendMode mode) {
		this.blendMode = mode;
		this.alpha=-1f;
	}

	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
		CompositeContext ctx; 
		if(srcColorModel == dstColorModel) {
			ctx = new NoNormBlendCompositeContext(dstColorModel, hints);
		} else {
			ctx = new BlendCompositeContext(srcColorModel, dstColorModel, hints);
		}
		return ctx;
	}
	
	private static Rectangle getBounds(Raster src, Raster dstIn, WritableRaster dstOut) {
		timer.start("bounds-b");		
		Rectangle bSrc, bDstIn, bDstOut, bb;
		int x1, y1, x2, y2;
		bSrc = src.getBounds();
		bDstIn = dstIn.getBounds();
		bDstOut = dstOut.getBounds();
		x1 = Math.min(bSrc.x, Math.min(bDstIn.x, bDstOut.x));
		y1 = Math.min(bSrc.x, Math.min(bDstIn.x, bDstOut.x));
		x2 = Math.min(bSrc.x+bSrc.width, Math.min(bDstIn.x+bDstIn.width, bDstOut.x+bDstOut.width));
		y2 = Math.min(bSrc.y+bSrc.height, Math.min(bDstIn.y+bDstIn.height, bDstOut.y+bDstOut.height));
		bb = new Rectangle(x1, y1, x2-x1, y2-y1);			
		timer.stop("bounds-b");
		return bb;
	}
	
	/**
	 *	Composite context for when the Color models are different. 
	 */
	private class BlendCompositeContext implements java.awt.CompositeContext {
		
		private ColorModel srcColorModel, dstColorModel;

		public BlendCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
			this.srcColorModel = srcColorModel;
			this.dstColorModel = dstColorModel;
		}

		@Override
		public void dispose() {
			System.out.println(timer);
		}

		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
				
			Rectangle bb = getBounds(src, dstIn, dstOut);  
			
			timer.start("init");
			Object pix = null;
			Object pixAdd = null;
			float[] pixNorm = null;
			float[] pixAddNorm = null;
			timer.stop("init");
			
			timer.start("draw");
			for(int x = bb.x; x < bb.x+bb.width; x++) {
				for(int y = bb.y; y < bb.y+bb.height; y++){
					pix = dstIn.getDataElements(x, y, pix);
					pixAdd = src.getDataElements(x, y, pixAdd);
					
					//Proper handling of normalized components
					pixNorm = dstColorModel.getNormalizedComponents(pix, pixNorm, 0);
					pixAddNorm = srcColorModel.getNormalizedComponents(pixAdd, pixAddNorm, 0);
					
					int components = Math.min(pixNorm.length, pixAddNorm.length);
					
					
					for(int i=0; i<components; i++) { //FIXME: use shorter components Array
						if(blendMode == BlendMode.ADD) {
							pixNorm[i] = Math.min(pixNorm[i]+pixAddNorm[i], 1.0f);
						}else {
							pixNorm[i] = pixNorm[i] * pixAddNorm[i];
						}
					}
					if(dstColorModel.hasAlpha() && alpha > 0) {
						pixNorm[pixNorm.length-1] = alpha;
					}
					pix = dstColorModel.getDataElements(pixNorm, 0, pix);
					dstOut.setDataElements(x, y, pix);
				
				}
			}
			timer.stop("draw");
		}
	}
	
	
	
	/**
	 *	Composite context for when the Color models are the same. 
	 */
	private class NoNormBlendCompositeContext implements java.awt.CompositeContext {
		
		private ColorModel dstColorModel;

		public NoNormBlendCompositeContext(ColorModel dstColorModel, RenderingHints hints) {
			this.dstColorModel = dstColorModel;
		}

		@Override
		public void dispose() {
			System.out.println(timer);
		}

		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
				
			Rectangle bb = getBounds(src, dstIn, dstOut);  
			
			timer.start("init");
			Object pix = null;
			float[] pixi = null;
			float[] pixiAdd = null;
			timer.stop("init");
			
			timer.start("draw");
			for(int x = bb.x; x < bb.x+bb.width; x++) {
				for(int y = bb.y; y < bb.y+bb.height; y++){
					pixi = dstIn.getPixel(x, y, pixi);
					pixiAdd = src.getPixel(x, y, pixiAdd);
					
					for(int i=0; i<3; i++) {
						if(blendMode == BlendMode.ADD) {
							pixi[i] = Math.min(pixi[i]+pixiAdd[i], 1f);
						} else {
							pixi[i] = pixi[i] * pixiAdd[i];
						}
					}
					if(dstColorModel.hasAlpha()) {
						if(alpha > 0) {
							pixi[pixi.length-1] = alpha;
						} else {
							pixi[pixi.length-1] = 1f;
						}
					}
					pix = dstColorModel.getDataElements(pixi, 0, pix);
					dstOut.setDataElements(x, y, pix);

				}
			}
			timer.stop("draw");
		}
	}
}
