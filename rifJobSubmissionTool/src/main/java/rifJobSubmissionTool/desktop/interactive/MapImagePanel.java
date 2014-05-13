package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


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

public class MapImagePanel 
	extends JPanel 
	implements ComponentListener {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final long serialVersionUID = 4194725123510331062L;

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	
	//GUI Components
	/** The buffered image. */
	private BufferedImage bufferedImage;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map image panel.
	 */
	public MapImagePanel() {
		
		setBorder(LineBorder.createGrayLineBorder());
		addComponentListener(this);
		Dimension size = new Dimension(200, 200);
		setMaximumSize(size);
		setMinimumSize(size);
		setPreferredSize(size);	
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(
		Graphics graphics) {
		
		super.paint(graphics);

		Dimension currentDimension = getSize();
		
		int startX = 5;
		int startY = (int) currentDimension.getHeight()/2;
		
		if (bufferedImage == null) {
			String noAreaDefined
				= RIFJobSubmissionToolMessages.getMessage("mapDisplayArea.noAreaDefined");
			graphics.drawString(
				noAreaDefined, 
				startX, 
				startY);
			
			return;
		}
		
		BufferedImage scaledImage = getScaledImage();
		graphics.drawImage(
			scaledImage, 
			0, 
			0, 
			(int) currentDimension.getWidth(), 
			(int) currentDimension.getHeight(),
			null);
	}
	
	/**
	 * Gets the scaled image.
	 *
	 * @return the scaled image
	 */
	private BufferedImage getScaledImage() {
		
		BufferedImage scaledImage = null;
		
		int imageWidth  = bufferedImage.getWidth();
	    int imageHeight = bufferedImage.getHeight();

	    int panelWidth = 200;
	    int panelHeight = 200;
	    double scaleX = (double)panelWidth/imageWidth;
	    double scaleY = (double)panelHeight/imageHeight;
	    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
	    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

	    scaledImage
	    	= bilinearScaleOp.filter(
	    		bufferedImage,
	    		new BufferedImage(
	    			panelWidth, 
	    			panelHeight, 
	    			bufferedImage.getType()));
		
		return scaledImage;
	}
	
	/**
	 * Sets the data.
	 *
	 * @param bufferedImage the new data
	 */
	public void setData(
		BufferedImage bufferedImage) {	
		
		this.bufferedImage = bufferedImage;

		repaint();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Component Listener
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(
		ComponentEvent event) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(
		ComponentEvent event) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(
		ComponentEvent event) {
				
		repaint();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(
		ComponentEvent event) {}
		
	// ==========================================
	// Section Override
	// ==========================================

}

