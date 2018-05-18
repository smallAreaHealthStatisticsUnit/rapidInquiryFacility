package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.Date;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

/**
 * A convenience class that is meant to hold code common to all of the business class
 * objects.  It follows a similar theme to {rifServices.concepts.AbstractRIFConcept}
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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

abstract class AbstractDataLoaderToolConcept
	implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	private boolean isChanged;
	private Date lastModifiedTime;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoaderToolConcept() {
		isChanged = false;
		lastModifiedTime = new Date(System.currentTimeMillis());
		updateLastModifiedTime();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean isChanged() {
		return isChanged;
	}
	
	public void setChanged(final boolean isChanged) {
		this.isChanged = isChanged;
	}
	
	public void setLastModifiedTime(final Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}

	protected void updateLastModifiedTime() {
		Date currentTimeStamp = new Date(System.currentTimeMillis());
		lastModifiedTime = currentTimeStamp;		
	}
	
	
	
	
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

	protected boolean lastModifiedDatesIdentical(final AbstractDataLoaderToolConcept otherConcept) {
		
		Date otherLastModifiedTime = otherConcept.getLastModifiedTime();
		if (FieldValidationUtility.hasDifferentNullity(
			lastModifiedTime, 
			otherLastModifiedTime)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (lastModifiedTime.compareTo(otherLastModifiedTime) == 0) {
			return true;
		}
		return false;
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


