package rifJobSubmissionTool.desktop.interactive;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.swing.JPanel;


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

final class WorkFlowArrow 
	extends JPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final long serialVersionUID = 7577421605016719605L;
	/** The Constant MAXIMUM_HEIGHT. */
	private static final int MAXIMUM_HEIGHT = 15;	
	/** The Constant MAXIMUM_WIDTH. */
	private static final int MAXIMUM_WIDTH = 20;		
	/** The is selected. */
	
	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	private boolean isSelected;

	
	//GUI Components
	/** The arrow. */
	private Polygon arrow;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new work flow arrow.
	 */
	public WorkFlowArrow() {
		
			//calculate the points on an arrow
			//     x     x     x    x
			//                 3
			//1_______________2|\
			//|    |     |       \
			//|    |     |        \
			//|    |     |         >4
			//|    |     |        /
			//----------------6| /
			//7                |/
			//                 5

		initialiseArrow2();
	
		setMinimumSize(new Dimension(MAXIMUM_WIDTH, MAXIMUM_HEIGHT));
		setMaximumSize(new Dimension(MAXIMUM_WIDTH, MAXIMUM_HEIGHT));
		setPreferredSize(new Dimension(MAXIMUM_WIDTH, MAXIMUM_HEIGHT));
	}
	
	/**
	 * Initialise arrow2.
	 */
	private void initialiseArrow2() {
		
		//calculate the points on an arrow
		//     x     x     x    x
		//                 3
		//1_______________2|\
		//|    |     |       \
		//|    |     |        \
		//|    |     |         >4
		//|    |     |        /
		//----------------6| /
		//7                |/
		//                 5
	
		int[] xPoints = new int[7];
		int[] yPoints = new int[7];
	
		//Point 1
		xPoints[0] = 0;
		yPoints[0] = MAXIMUM_HEIGHT/4;
	
		//Point 2
		xPoints[1] = (3*MAXIMUM_WIDTH)/4;
		yPoints[1] = MAXIMUM_HEIGHT/4; 
	
		//Point 3
		xPoints[2] = (3*MAXIMUM_WIDTH)/4;
		yPoints[2] = 0;
	
		//Point 4
		xPoints[3] = MAXIMUM_WIDTH;
		yPoints[3] = MAXIMUM_HEIGHT/2;
	
		//Point 5
		xPoints[4] = (3*MAXIMUM_WIDTH)/4;
		yPoints[4] = MAXIMUM_HEIGHT;		
	
		//Point 6
		xPoints[5] = (3*MAXIMUM_WIDTH)/4;
		yPoints[5] = (3*MAXIMUM_HEIGHT)/4;	
	
		//Point 7
		xPoints[6] = 0;
		yPoints[6] = (3*MAXIMUM_HEIGHT)/4;	
	
		arrow = new Polygon(xPoints, yPoints, 7);
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
				
		if (isSelected) {
			graphics2D.setColor(Color.black);
		}
		else {
			graphics2D.setColor(new Color(220, 220, 220));
		}
		graphics2D.fill(arrow);	
		graphics2D.setColor(Color.black);
		graphics2D.draw(arrow);	
		
	}
}

