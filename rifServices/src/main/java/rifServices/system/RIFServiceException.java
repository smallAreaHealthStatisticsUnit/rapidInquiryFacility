package rifServices.system;

import java.util.ArrayList;


/**
 *<p>
 * The typed exception that is used by the RIF services project.  The class is 
 * designed to facilitate testing and reporting errors.  Each object may be
 * associated with multiple error messages, but it can only be given one 
 * error code.  
 * </p>
 * <p>
 * Multiple error messages are typically used in the <code>checkErrors()</code>
 * methods that appear in the business concept layer classes.  The 
 * {@link rifServices.system.RIFServiceError} is used in the automated test suites
 * so they can be precise in identifying the kind of exception that may be expected
 * by test cases that are excercising scenarios with errors.
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

public class RIFServiceException 
	extends Exception {

	// ==========================================
	// Section Constants
	// ==========================================

	private static final long serialVersionUID = 609449213280772202L;

	// ==========================================
	// Section Properties
	// ==========================================
	/** The error. */
	private RIFServiceError error;
	
	/** The error messages. */
	private ArrayList<String> errorMessages;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF service exception.
	 *
	 * @param error the error
	 * @param errorMessage the error message
	 */
	public RIFServiceException(
		final RIFServiceError error,
		final String errorMessage) {
		
		super(errorMessage);
		
		this.error = error;
		errorMessages = new ArrayList<String>();
		errorMessages.add(errorMessage);
	}

	/**
	 * Instantiates a new RIF service exception.
	 *
	 * @param error the error
	 * @param errorMessages the error messages
	 */
	public RIFServiceException(
		final RIFServiceError error,
		final ArrayList<String> errorMessages) {
		
		this.error = error;
		this.errorMessages = new ArrayList<String>();
		this.errorMessages.addAll(errorMessages);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public RIFServiceError getError() {

		return error;
	}
	
	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public ArrayList<String> getErrorMessages() {

		return errorMessages;
	}
	
	/**
	 * Gets the error message count.
	 *
	 * @return the error message count
	 */
	public int getErrorMessageCount() {

		return errorMessages.size();
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
}
