package rifServices.businessConceptLayer;

import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceError;


import java.util.ArrayList;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public class BoundaryRectangle {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private BoundaryRectangle() {
	}

	public static BoundaryRectangle newInstance() {
		BoundaryRectangle boundaryRectangle = new BoundaryRectangle();
		return boundaryRectangle;
	}
	
	public static BoundaryRectangle newInstance(
		final double xMin,
		final double yMin,
		final double xMax,
		final double yMax) {
		
		BoundaryRectangle boundaryRectangle 
			= new BoundaryRectangle();
		boundaryRectangle.setXMin(xMin);
		boundaryRectangle.setYMin(yMin);
		boundaryRectangle.setXMax(xMax);
		boundaryRectangle.setYMax(yMax);
		
		return boundaryRectangle;
	}
		
	public static BoundaryRectangle createCopy(
		final BoundaryRectangle originalBoundaryRectangle) {
		
		if (originalBoundaryRectangle == null) {
			return null;
		}
		
		BoundaryRectangle cloneBoundaryRectangle
			= new BoundaryRectangle();
		
		cloneBoundaryRectangle.setXMin(originalBoundaryRectangle.getXMin());
		cloneBoundaryRectangle.setYMin(originalBoundaryRectangle.getYMin());
		cloneBoundaryRectangle.setXMax(originalBoundaryRectangle.getXMax());
		cloneBoundaryRectangle.setYMax(originalBoundaryRectangle.getYMax());
		
		return cloneBoundaryRectangle;
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public double getXMin() {
		return xMin;
	}

	public void setXMin(final double xMin) {
		this.xMin = xMin;
	}
	

	public double getYMin() {
		return yMin;
	}

	public void setYMin(final double yMin) {
		this.yMin = yMin;
	}

	
	public double getXMax() {
		return xMax;
	}

	public void setXMax(final double xMax) {
		this.xMax = xMax;
	}

	public double getYMax() {
		return yMax;
	}

	public void setYMax(final double yMax) {
		this.yMax = yMax;
	}
	
	public void identifyDifferences(
		final BoundaryRectangle anotherBoundaryRectangle,
		final ArrayList<String> differences) {
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkErrors() 
		throws RIFServiceException {

		//ensure that xMax >= xMin
		ArrayList<String> errorMessages = new ArrayList<String>(); 
		if (xMax < xMin) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.xMaxLessThanMin",
					String.valueOf(xMax),
					String.valueOf(xMin));
			errorMessages.add(errorMessage);
		}
		
		if (yMax < yMin) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.yMaxLessThanMin",
					String.valueOf(yMax),
					String.valueOf(yMin));
			errorMessages.add(errorMessage);			
		}
		
		//ensure that yMax >= yMin
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
					errorMessages);
			throw rifServiceException;
		}
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	public String getDisplayName() {
		String displayName
			= RIFServiceMessages.getMessage(
				"boundaryRectangle.displayName",
				String.valueOf(xMin),
				String.valueOf(yMin),
				String.valueOf(xMax),
				String.valueOf(yMax));
		return displayName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
