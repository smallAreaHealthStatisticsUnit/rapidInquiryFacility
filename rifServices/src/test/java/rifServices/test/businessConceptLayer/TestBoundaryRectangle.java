package rifServices.test.businessConceptLayer;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.system.RIFServiceError;
import rifServices.test.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;



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
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public final class TestBoundaryRectangle 
	extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master health code. */
	private BoundaryRectangle masterBoundaryRectangle;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestBoundaryRectangle() {

		masterBoundaryRectangle
			= BoundaryRectangle.newInstance(
				"1.0", 
				"1.0", 
				"2.0",
				"3.0");
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Accept valid health code.
	 */
	@Test
	/**
	 * Accept a valid health code with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * Reject blank code.
	 */
	@Test
	/**
	 * A health code is invalid if it has a blank code.
	 */
	public void rejectBlankRequiredFields_ERROR() {
		
		//ymax is blank
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setYMax("");
			boundaryRectangle.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				1);			
		}		

		//xmax is blank
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setXMax("");
			boundaryRectangle.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				1);			
		}		

		//ymin is blank
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setYMin("");
			boundaryRectangle.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				1);
		}			

		//xmin is blank
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setXMin("");
			boundaryRectangle.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				1);
		}
	}
	
	
	/**
	 * Reject multiple field errors.
	 */
	@Test
	public void rejectMultipleFieldErrors_ERROR() {
		//Check capacity to detect multiple field errors
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setYMax("");
			boundaryRectangle.setXMax("aaa");
			boundaryRectangle.setYMin(null);
			boundaryRectangle.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("blah", rifServiceException);
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				3);
		}		
	}
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setIdentifier(getTestMaliciousValue());
			boundaryRectangle.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setYMax(getTestMaliciousValue());
			boundaryRectangle.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setXMax(getTestMaliciousValue());
			boundaryRectangle.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setYMin(getTestMaliciousValue());
			boundaryRectangle.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
				
		try {
			BoundaryRectangle boundaryRectangle
				= BoundaryRectangle.createCopy(masterBoundaryRectangle);
			boundaryRectangle.setXMin(getTestMaliciousValue());
			boundaryRectangle.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
				
	}

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	

}
