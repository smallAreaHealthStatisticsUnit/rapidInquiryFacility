package rifJobSubmissionTool.desktop.interactive;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

final class WorkFlowStep 
	extends JPanel {
	
	// ==========================================
	// Section Constants
	// ==========================================	
	private static final long serialVersionUID = 7272178477145990958L;
	/** The Constant HORIZONTAL_GAP. */
	private static final int HORIZONTAL_GAP = 2;
	/** The Constant VERTICAL_GAP. */
	private static final int VERTICAL_GAP = 2;

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The name. */
	private String name;
	/** The is selected. */
	private boolean isSelected;	
	/** The rectangle. */
	private RoundRectangle2D.Float rectangle;	
	/** The width. */
	private int width;	
	/** The height. */
	private int height;	
	/** The name location. */
	private Point nameLocation;
		
	
	//GUI Components
	/** The selected gradient paint. */
	private GradientPaint selectedGradientPaint;
	/** The unselected gradient paint. */
	private GradientPaint unselectedGradientPaint;
	/** The selected start colour. */
	private Color selectedStartColour;
	/** The selected end colour. */
	private Color selectedEndColour;	
	/** The unselected start colour. */
	private Color unselectedStartColour;	
	/** The unselected end colour. */
	private Color unselectedEndColour;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new work flow step.
	 *
	 * @param name the name
	 */
	public WorkFlowStep(
		String name) {
	
		this.name = name;
		
		unselectedStartColour = new Color(220, 220, 220);
		unselectedEndColour = new Color(180, 180, 180);
		
		//light orange
		selectedStartColour = new Color(255, 211, 168);
		//dark orange
		selectedEndColour = new Color(255, 128, 0);		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Sets the selected.
	 *
	 * @param isSelected the new selected
	 */
	public void setSelected(
		boolean isSelected) {

		this.isSelected = isSelected;
	}
	
	/**
	 * Sets the selected colour range.
	 *
	 * @param selectedStartColour the selected start colour
	 * @param selectedEndColour the selected end colour
	 */
	public void setSelectedColourRange(
		Color selectedStartColour, 
		Color selectedEndColour) {
	
		this.selectedStartColour = selectedStartColour;
		this.selectedEndColour = selectedEndColour;
	}
	
	/**
	 * Sets the unselected colour range.
	 *
	 * @param unselectedStartColour the unselected start colour
	 * @param unselectedEndColour the unselected end colour
	 */
	public void setUnselectedColourRange(
		Color unselectedStartColour, 
		Color unselectedEndColour) {
		
		this.unselectedStartColour = unselectedStartColour;
		this.unselectedEndColour = unselectedEndColour;
	}	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(
		Graphics graphics) {

		super.paintComponent(graphics);
		Graphics2D graphics2D = (Graphics2D) graphics;
		
		if (rectangle == null) {
			initialiseWorkFlowDiagram(graphics);
		}
		
		//draw the rectangle
		if (isSelected) {
			graphics2D.setPaint(selectedGradientPaint);
			graphics2D.fill(rectangle);			
		}
		else {
			graphics2D.setPaint(unselectedGradientPaint);
			graphics2D.fill(rectangle);			
		}

		graphics2D.setColor(Color.BLACK);
		BasicStroke basicStroke 
			= new BasicStroke(
				2, 
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		graphics2D.setStroke(basicStroke);
		graphics2D.draw(rectangle);
		
		//now draw text
		graphics2D.drawString(
			name, 
			(int) nameLocation.getX(), 
			(int) nameLocation.getY());		
	}
	
	/**
	 * Initialise work flow diagram.
	 *
	 * @param graphics the graphics
	 */
	private void initialiseWorkFlowDiagram(
		Graphics graphics) {

		FontMetrics fontMetrics = graphics.getFontMetrics();
		
		int textWidth 
			= fontMetrics.charsWidth(
				name.toCharArray(), 
				0, 
				name.length());
		int textHeight = fontMetrics.getHeight();
		
		width = 2 * HORIZONTAL_GAP + textWidth;
		height = 2 * VERTICAL_GAP + textHeight;
		
		rectangle 
			= new RoundRectangle2D.Float(
				(float) 0,
				(float) 0,
				(float) width,
				(float) height,
				(float) 10,
				(float) 10);
		nameLocation 
			= new Point(
				HORIZONTAL_GAP,
				VERTICAL_GAP/2 + textHeight);

		//create a straight line that is used as a reference
		//to draw the colour gradient
		int halfTotalHeight = height/2;
		
		selectedGradientPaint
			= new GradientPaint(
				0, 
				halfTotalHeight,
				selectedStartColour,
				width,
				halfTotalHeight,
				selectedEndColour,
				true);
		
		unselectedGradientPaint
			= new GradientPaint(
				0, 
				halfTotalHeight,
				unselectedStartColour,
				width,
				halfTotalHeight,
				unselectedEndColour,
				true);
		
		Dimension rectangleSize = new Dimension(width, height);
		setMinimumSize(rectangleSize);
		setMaximumSize(rectangleSize);
		setPreferredSize(rectangleSize);

	}
	
}

