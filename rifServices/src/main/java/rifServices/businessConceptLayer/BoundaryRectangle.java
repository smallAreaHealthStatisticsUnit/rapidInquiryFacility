package rifServices.businessConceptLayer;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;



import rifServices.util.FieldValidationUtility;

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

public class BoundaryRectangle extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String xMin;
	private String yMin;
	private String xMax;
	private String yMax;
	
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
		final String xMin,
		final String yMin,
		final String xMax,
		final String yMax) {
		
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
	
	public String getXMin() {
		return xMin;
	}

	public void setXMin(final String xMin) {
		this.xMin = xMin;
	}
	

	public String getYMin() {
		return yMin;
	}

	public void setYMin(final String yMin) {
		this.yMin = yMin;
	}

	
	public String getXMax() {
		return xMax;
	}

	public void setXMax(final String xMax) {
		this.xMax = xMax;
	}

	public String getYMax() {
		return yMax;
	}

	public void setYMax(final String yMax) {
		this.yMax = yMax;
	}
	
	public void identifyDifferences(
		final BoundaryRectangle anotherBoundaryRectangle,
		final ArrayList<String> differences) {
		
	}
	
	public String convertToKey() {
		StringBuilder key = new StringBuilder();
		key.append(String.valueOf(xMin));
		key.append("-");
		key.append(String.valueOf(yMin));
		key.append("-");
		key.append(String.valueOf(xMax));
		key.append("-");
		key.append(String.valueOf(yMax));
		
		return key.toString();		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {	
		
		super.checkSecurityViolations();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
		if (yMax != null) {
			String yMaxFieldName
				= RIFServiceMessages.getMessage("boundaryRectangle.yMax.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				yMaxFieldName,
				yMax);
		}

		if (xMax != null) {
			String xMaxFieldName
				= RIFServiceMessages.getMessage("boundaryRectangle.xMax.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				xMaxFieldName,
				xMax);
		}


		if (yMin != null) {
			String yMinFieldName
				= RIFServiceMessages.getMessage("boundaryRectangle.yMin.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				yMinFieldName,
				yMin);
		}


		if (xMin != null) {
			String xMinFieldName
				= RIFServiceMessages.getMessage("boundaryRectangle.xMin.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				xMinFieldName,
				xMin);
		}
		
		
	}


	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		//ensure that xMax >= xMin
		ArrayList<String> errorMessages = new ArrayList<String>(); 

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		
		Float xMaxValue = null;
		Float yMaxValue = null;
		Float xMinValue = null;
		Float yMinValue = null;

		String recordType = getRecordType();

		String xMaxFieldName
			= RIFServiceMessages.getMessage("boundaryRectangle.xMax.label");
		if (fieldValidationUtility.isEmpty(xMax)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					xMaxFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {			
				xMaxValue = Float.valueOf(xMax);
			}
			catch(NumberFormatException numericFormatException) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"boundaryRectangle.error.invalidBoundaryValue",
						xMax);
				errorMessages.add(errorMessage);
			}
		}


		String yMaxFieldName
			= RIFServiceMessages.getMessage("boundaryRectangle.yMax.label");
		if (fieldValidationUtility.isEmpty(yMax)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					yMaxFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {			
				yMaxValue = Float.valueOf(yMax);
			}
			catch(NumberFormatException numericFormatException) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"boundaryRectangle.error.invalidBoundaryValue",
						yMax);
				errorMessages.add(errorMessage);
			}
		}

		

		String xMinFieldName
			= RIFServiceMessages.getMessage("boundaryRectangle.xMin.label");
		if (fieldValidationUtility.isEmpty(xMin)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					xMinFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {			
				xMinValue = Float.valueOf(xMin);
			}
			catch(NumberFormatException numericFormatException) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"boundaryRectangle.error.invalidBoundaryValue",
						xMin);
				errorMessages.add(errorMessage);
			}
		}
		

		String yMinFieldName
			= RIFServiceMessages.getMessage("boundaryRectangle.yMin.label");
		if (fieldValidationUtility.isEmpty(yMin)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					yMinFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				yMinValue = Float.valueOf(yMin);
			}
			catch(NumberFormatException numericFormatException) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"boundaryRectangle.error.invalidBoundaryValue",
						yMin);
				errorMessages.add(errorMessage);
			}
		}
		
		countErrors(
			RIFServiceError.INVALID_BOUNDARY_RECTANGLE, 
			errorMessages);
		
		try {			
			yMaxValue = Float.valueOf(yMax);
		}
		catch(NumberFormatException numericFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.invalidBoundaryValue",
					yMax);
			errorMessages.add(errorMessage);
		}
		
		
		try {			
			xMinValue = Float.valueOf(xMin);
		}
		catch(NumberFormatException numericFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.invalidBoundaryValue",
					xMin);
			errorMessages.add(errorMessage);
		}

		
		try {			
			yMinValue = Float.valueOf(yMin);
		}
		catch(NumberFormatException numericFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.invalidBoundaryValue",
					yMin);
			errorMessages.add(errorMessage);
		}

		//another opportunity to throw an exception
		//there is no point comparing numeric values if some of them are not valid numbers
		countErrors(
			RIFServiceError.INVALID_BOUNDARY_RECTANGLE, 
			errorMessages);

		
		
		if (xMaxValue < xMinValue) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.xMaxLessThanMin",
					String.valueOf(xMax),
					String.valueOf(xMin));
			errorMessages.add(errorMessage);
		}
		
		if (yMaxValue < yMinValue) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"boundaryRectangle.error.yMaxLessThanMin",
					String.valueOf(yMax),
					String.valueOf(yMin));
			errorMessages.add(errorMessage);			
		}

		countErrors(
			RIFServiceError.INVALID_BOUNDARY_RECTANGLE, 
			errorMessages);
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
	
	@Override
	public String getRecordType() {
		
		String recordNameLabel
			= RIFServiceMessages.getMessage("boundaryRectangle.label");
		return recordNameLabel;
	}	

	
}
