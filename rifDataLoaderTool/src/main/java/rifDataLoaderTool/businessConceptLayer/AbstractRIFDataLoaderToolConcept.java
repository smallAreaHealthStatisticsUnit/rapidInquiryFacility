package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;

import java.util.ArrayList;


/**
 * A convenience class that is meant to hold code common to all of the business class
 * objects.  It follows a similar theme to {rifServices.businessConceptLayer.AbstractRIFConcept} 
 * in that it manages routines related to checking errors and an "identifier" field which is
 * reserved to hold a key that would uniquely identify it in a database table.
 * 
 * <p>
 * The class was developed because it wasn't clear how much overlap the business concept classes
 * in the Data Loader Tool would have with the business concept classes that are used to support
 * study submission and study result retrieval.  In future, this class may disappear and
 * be merged with the <code>AbstractRIFService</code> class.
 * 
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public abstract class AbstractRIFDataLoaderToolConcept
	implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFDataLoaderToolConcept() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	abstract public String getDisplayName();
	abstract public void checkSecurityViolations() throws RIFServiceSecurityException;
	abstract public void checkErrors() throws RIFServiceException;
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	protected void countErrors(
		final RIFDataLoaderToolError rifDataLoaderToolError,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {

		if (errorMessages.size() > 0) {
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					rifDataLoaderToolError, 
					errorMessages);
			throw RIFServiceException;
		}		
	}	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}


