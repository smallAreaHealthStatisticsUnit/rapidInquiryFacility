package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 * This class was developed to reduce the number of parameters that 
 * were often passed to methods in the {@link rifServices.RIFStudyRetrievalAPI}
 * interface.  The common parameters were:
 * <ul>
 * <li>a geography </li>
 * <li>a geo level select</li>
 * <li>a study</li>
 * </ul>
 * 
 * <p>
 * In the implementations of the service, only the identifiers were used: it 
 * was unnecessary to retrieve all the information about a study.  In the 
 * interests of efficient processing, this class was produced.
 * </p>
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class StudyResultRetrievalContext {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String geographyName;
	private String geoLevelSelectName;
	private String studyID;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private StudyResultRetrievalContext() {

	}

	
	public static StudyResultRetrievalContext newInstance() {
		StudyResultRetrievalContext studyRetrievalContext
			= new StudyResultRetrievalContext();
		return studyRetrievalContext;
	}
	
	public static StudyResultRetrievalContext newInstance(
		final String geographyName,
		final String geoLevelSelectName,
		final String studyID) {
		
		StudyResultRetrievalContext studyResultRetrievalContext
			= new StudyResultRetrievalContext();
		studyResultRetrievalContext.setGeographyName(geographyName);
		studyResultRetrievalContext.setGeoLevelSelectName(geoLevelSelectName);
		studyResultRetrievalContext.setStudyID(studyID);
		
		return studyResultRetrievalContext;
	}	
	
	public static StudyResultRetrievalContext createCopy(
		final StudyResultRetrievalContext originalStudyRetrievalContext) {
		
		if (originalStudyRetrievalContext == null) {
			return null;
		}
		
		StudyResultRetrievalContext cloneStudyRetrievalContext
			= new StudyResultRetrievalContext();
		cloneStudyRetrievalContext.setGeographyName(
			originalStudyRetrievalContext.getGeographyName());
		cloneStudyRetrievalContext.setGeoLevelSelectName(originalStudyRetrievalContext.getGeoLevelSelectName());
		cloneStudyRetrievalContext.setStudyID(originalStudyRetrievalContext.getStudyID());
		return cloneStudyRetrievalContext;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getGeographyName() {
		return geographyName;
	}

	public void setGeographyName(final String geographyName) {
		this.geographyName = geographyName;
	}

	public String getGeoLevelSelectName() {
		return geoLevelSelectName;
	}

	public void setGeoLevelSelectName(final String geoLevelSelectName) {
		this.geoLevelSelectName = geoLevelSelectName;
	}

	public String getStudyID() {
		return studyID;
	}

	public void setStudyID(final String studyID) {
		this.studyID = studyID;
	}

	/**
	 * Checks for identical contents.
	 *
	 * @param otherMapArea the other map area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final StudyResultRetrievalContext otherStudyRetrievalContext) {

		Collator collator = GENERIC_MESSAGES.getCollator();

		String otherGeographyName
			= otherStudyRetrievalContext.getGeographyName();
		if (FieldValidationUtility.hasDifferentNullity(
			geographyName, 
			otherGeographyName)) {

			//reject if one is null and the other is non-null
			return false;
		}
		else if (geographyName != null) {
			//they must both be non-null
			if (collator.equals(
				geographyName, 
				otherGeographyName) == false) {
				return false;
			}			
		}
		
		String otherGeoLevelSelectName
			= otherStudyRetrievalContext.getGeoLevelSelectName();
		if (FieldValidationUtility.hasDifferentNullity(
			geoLevelSelectName, 
			otherGeoLevelSelectName)) {

			//reject if one is null and the other is non-null
			return false;
		}
		else if (geoLevelSelectName != null) {
			//they must both be non-null
			if (collator.equals(
				geographyName, 
				otherGeoLevelSelectName) == false) {

				return false;
			}			
		}
		
		String otherStudyID
			= otherStudyRetrievalContext.getStudyID();
		if (FieldValidationUtility.hasDifferentNullity(
			studyID, 
			otherStudyID)) {

			//reject if one is null and the other is non-null
			return false;
		}
		else if (studyID != null) {
			//they must both be non-null
			if (collator.equals(studyID, otherStudyID) == false) {
				return false;
			}			
		}

		return true;
	}
	
	public String getRecordType() {
		String recordType
			= RIFServiceMessages.getMessage("studyResultRetrievalContext.label");
		return recordType;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkErrors(
		final AbstractRIFConcept.ValidationPolicy validationPolicy)
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType = getRecordType();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(geographyName)) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.geographyName");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		
		if (fieldValidationUtility.isEmpty(geoLevelSelectName)) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.geoLevelSelectName");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
				
		if (fieldValidationUtility.isEmpty(studyID)) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.studyID");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
	
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_STUDY_RESULT_RETRIEVAL_CONTEXT,
					errorMessages);
			
			throw rifServiceException;
		}
		
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();

		if (geographyName != null) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.geographyName");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				fieldName,
				geographyName);
		}			

		if (geoLevelSelectName != null) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.geoLevelSelectName");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				fieldName,
				geoLevelSelectName);
		}			
		
		if (studyID != null) {
			String fieldName
				= RIFServiceMessages.getMessage("studyResultRetrievalContext.studyID");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				fieldName,
				studyID);
		}			
		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	public String getDisplayName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(geographyName);
		buffer.append("-");
		buffer.append(geoLevelSelectName);
		buffer.append("-");
		buffer.append(studyID);
		
		return buffer.toString();
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
