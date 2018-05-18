/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 * 
 * Author: Hal Mirsky (I think!)
 */

/* Map Legend Version 1 */

package org.sahsu.rif.services.graphics;

import org.sahsu.rif.generic.util.RIFLogger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DirectLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;

/**
 * Creates a legend at a fixed location on the map.
 * @author Hal Mirsky, hmirsky at aseg dot com
 *
 */
public class LegendLayer extends DirectLayer {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private static String DEFAULT_TITLE   = "Legend";
	
	/* 
	 * The legend is drawn within the "bounds" rectangle.  The following pixel values 
	 * are used to lay out legend elements within this rectangle.
	 */
	private int HOR_MARGIN   =  4;  // horizontal margin between edge and contents
	private int HOR_SPACE    =  7;  // horizontal space between legend components (e.g. between a label and icon)
	private int VERT_MARGIN  =  4;  // vertical margin between edge and contents
	private int LINESPACE    =  5;  // vertical space between lines of text
	private int LEFTX_OFFSET = 20;  // X coord val of left edge of bounds rectangle
	private int LEFTY_OFFSET = 20;  // Y coord val of bottom edge of bounds rectangle
	private int RIGHTX_OFFSET = 20;  // X coord val of right edge of bounds rectangle
	private int RIGHTY_OFFSET = 20;  // Y coord val of bottom edge of bounds rectangle
	
	private int FONT_SIZE_TITLE  = 36; // Points
	private int FONT_SIZE_ITEM   = 30;
	
	private int scaleFactor=1;
	
	private Font titlef           = null;
	private Font itemf            = null;
//	private Color backgroundColor = Color.LIGHT_GRAY;
	private Color backgroundColor = Color.decode("#fafafa"); // Gray98 [very light gray!!!]

	private List<LegendItem> legendItems;
	
	private int boundsw, boundsh;       // Width and height of the "bounds" rectangle
	private int th;                     // height of title label
	private int ih;                     // height of legend item label and icon
	
	private int imageWidth = 0;			// Width of the image in pixels
	
	private String legendTitle = DEFAULT_TITLE;

	/* 
	 * Information about a legend item
	 */
	public static class LegendItem
	{
		private String label;
		private Color color;
		private Geometries shape;
		public LegendItem(String l, Color c, Geometries s)
		{
			label = l;
			color = c;
			shape = s;
		}
	}
	
	public LegendLayer(
		final String title, 
		final Color bgColor, 
		final List<LegendItem> featureInfo, 
		final int imageWidth)
	{
		legendTitle = title;
		this.imageWidth=imageWidth;// 7480 pixels at 100 dpi - 74 pixels per scaleFactor
		this.scaleFactor=(int)(imageWidth/1870); // 4 @ 7480 pixels
		
		this.HOR_MARGIN   *= this.scaleFactor;  // horizontal margin between edge and contents
		this.HOR_SPACE    *= this.scaleFactor;  // horizontal space between legend components (e.g. between a label and icon)
		this.VERT_MARGIN  *= this.scaleFactor;  // vertical margin between edge and contents
		this.LINESPACE    *= this.scaleFactor;  // vertical space between lines of text
		this.LEFTX_OFFSET *= this.scaleFactor;  // X coord val of left edge of bounds rectangle
		this.LEFTY_OFFSET *= this.scaleFactor;  // Y coord val of bottom edge of bounds rectangle
		this.RIGHTX_OFFSET *= this.scaleFactor;  // X coord val of right edge of bounds rectangle
		this.RIGHTY_OFFSET *= this.scaleFactor;  // Y coord val of bottom edge of bounds rectangle
	
		this.FONT_SIZE_TITLE *= this.scaleFactor;
		this.FONT_SIZE_ITEM  *= this.scaleFactor;
		
		titlef = new Font("Arial", Font.BOLD, this.FONT_SIZE_TITLE);
		itemf = new Font("Arial", Font.BOLD, this.FONT_SIZE_ITEM);
		
		backgroundColor = bgColor;
		legendItems = featureInfo;
		buildLegendParams();
	}

	private void buildLegendParams()
	{
		Rectangle2D titleLabelRect = titlef.getStringBounds(legendTitle, new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)); 

		// use length of longest label string as basis for computing rectangle width
		
		String longestLabel = "";
		int slen = 0;
		for (int idx = 0; idx < legendItems.size(); idx++)
		{
			LegendLayer.LegendItem li = legendItems.get(idx);
			if (li.label.length() > slen)
			{
				slen = li.label.length();
				longestLabel = li.label;
			}
		}
		
		Rectangle2D itemLabelRect = itemf.getStringBounds(longestLabel.toUpperCase(), new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)); 
			
		// Bounds rectangle dimensions (pixels) are calculated as follows:
		// height: vmargin + ht of Legend Label + linespace + height of site label + num regions * (height of region label + linespace) + vmargin 
		// width: hmargin + width of longest label + hspace + icon size + hmargin
		
		th = (int) titleLabelRect.getHeight();
		ih = (int) itemLabelRect.getHeight();  // height of site label.  Also used as icon size	
		
		int iw=((int) titleLabelRect.getWidth());
		int tw=((int) itemLabelRect.getWidth());
		if (iw> tw) {
			boundsw   = iw+2*HOR_MARGIN+HOR_SPACE+ih;
		}
		else {
			boundsw   = tw+2*HOR_MARGIN+HOR_SPACE+ih;
		}
		boundsh   = VERT_MARGIN+th+LINESPACE+ih+LINESPACE+((LINESPACE+(int) itemLabelRect.getHeight()))*legendItems.size()+VERT_MARGIN;
	}
	
	@Override
	public void draw(Graphics2D graphics, MapContent map, MapViewport viewport) {
		
		try {	
			// Get the screen area to handle a window resize
			Rectangle scrRect = viewport.getScreenArea();	

			// y coord of upper left corner of bounds rectangle is offset from edge
			int ulcy  = (int)scrRect.getHeight()-(LEFTY_OFFSET+boundsh);
//			int lrcy  = (int)scrRect.getWidth()-(RIGHTY_OFFSET+boundsh);
			
			int iconRightBase = LEFTX_OFFSET+boundsw-HOR_MARGIN;
			//int iconRightBase = (int)scrRect.getWidth()+boundsw-HOR_MARGIN;
		
			// Draw the bounds rectangle and title
			// Rectangle(int x, int y, int width, int height)
	    	Shape shape = new Rectangle(LEFTX_OFFSET,ulcy,boundsw,boundsh); 	// Bottom left
	    	// Shape shape = new Rectangle(lrcy,ulcy,boundsw,boundsh); 			// BOttom right
			rifLogger.info(this.getClass(), legendTitle + " scrRect: " + scrRect.toString() +
				"; shape: " + shape.toString() +
				"; scaleFactor: " + scaleFactor +
				"; FONT_SIZE_ITEM: " + FONT_SIZE_ITEM + 
				"; boundsw: " + boundsw);
				
			drawShape(graphics, shape, backgroundColor);
	    	graphics.setFont(titlef);
	    	graphics.setColor(Color.BLACK);
	    	graphics.drawString(legendTitle, LEFTX_OFFSET+HOR_MARGIN, ulcy+th+VERT_MARGIN);  //The baseline of the leftmost character is at position (x, y) in this graphics context's coordinate system.

	    	// Draw label and icon for each item in the legend
	    	graphics.setFont(itemf);
	    	for (int rnum = 0; rnum < legendItems.size(); rnum++)
	    	{
	    		drawLegendItem(graphics, legendItems.get(rnum), rnum, ulcy, iconRightBase);
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void drawLegendItem(Graphics2D graphics, LegendItem item, int legendRow, int ulcy, int iconRightBase)
	{
		Shape shape=null;
		
		// top of area containing legend icons 
		int iconTopBase = ulcy+VERT_MARGIN+th+VERT_MARGIN;
		// right side of area containing icons
		// int iconRightBase = LEFTX_OFFSET+boundsw-HOR_MARGIN;
		
		// Line geometries are shown on the legend with a vertical line. We'll draw a 
		// rectangle with width 20% of height.  The rectangle is centered vertically 
		// on the other legend icons
		long lineIconWidth = (long) (.2*((double)ih));
		int lineWidthOffset = (int) (.6 * (float) ih);
		
		
		// Point geometries are shown with a circle on the legend.  
		// Circle icon diameter is 50% of rectangle icon width and vertically centered
		// with other legend icons
		int circleWidth = (int) (.5 * ((float) ih));
		int circleUpperLeft = (int) (.75 * ((float) ih));
		int circleTop = ih - circleUpperLeft;

		graphics.setColor(Color.BLACK);
		graphics.drawString(item.label, LEFTX_OFFSET+HOR_MARGIN, iconTopBase+(legendRow+1)*(LINESPACE+ih)+ih);
//		graphics.drawString(item.label, iconRightBase-boundsw, iconTopBase+(legendRow+1)*(LINESPACE+ih)+ih);
		
		switch (item.shape)
		{
		case POLYGON:
		case MULTIPOLYGON:
			shape = new Rectangle(iconRightBase-ih, iconTopBase + (legendRow+1)*(LINESPACE+ih), ih, ih);
	    	drawShape(graphics, shape, item.color);
			break;
			
		case LINESTRING:
		case MULTILINESTRING:	    			
			shape = new Rectangle(iconRightBase-lineWidthOffset,iconTopBase + (legendRow+1)*(LINESPACE+ih), (int) lineIconWidth, ih);
			drawShape(graphics, shape, item.color);
			break;
			
        case POINT:
        case MULTIPOINT:
			shape = new Ellipse2D.Double(iconRightBase-circleUpperLeft,iconTopBase + (legendRow+1)*(LINESPACE+ih) + circleTop, circleWidth, circleWidth);
			drawShape(graphics, shape, item.color);
	    	break;
	    	
		default:
			break;
		}
		
		if (shape != null) {
			rifLogger.info(this.getClass(), item.label + "; shape: " + shape.toString());
		}
	}

	private void drawShape(Graphics2D graphics, Shape shape, Color color)
	{
		if (color != null) {
			graphics.draw(shape);
			graphics.setColor(color);
			graphics.fill(shape);
		}
	}
	
	@Override
	public ReferencedEnvelope getBounds() {
		return null;
	}
}
