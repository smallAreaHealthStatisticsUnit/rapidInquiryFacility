package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 * Exposure covariates cannot vary with time, and where covariates values do 
 * change over the period of the investigation, the user must create an 
 * appropriate exposure metric (such as the mean value, or maximum value)
 * to apply throughout.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
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


public final class ExposureCovariate 
	extends AbstractCovariate {

// ==========================================
// Section Constants
// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
		
// ==========================================
// Section Construction
// ==========================================
	
	/**
	 * Instantiates a new exposure covariate.
	 *
	 * @param name the name
	 * @param minimumValue the minimum value
	 * @param maximumValue the maximum value
	 */
	private ExposureCovariate(
		final String name,
		final String minimumValue,
		final String maximumValue) {
		
		super(name, minimumValue, maximumValue);
	}
	
    /**
     * Instantiates a new exposure covariate.
     */
    private ExposureCovariate() {

    }

	/**
	 * New instance.
	 *
	 * @return the exposure covariate
	 */
	static public ExposureCovariate newInstance() {
		
		ExposureCovariate exposureCovariate = new ExposureCovariate();
		return exposureCovariate;
	}
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param minimumValue the minimum value
	 * @param maximumValue the maximum value
	 * @param covariateType the covariate type
	 * @return the exposure covariate
	 */
	static public ExposureCovariate newInstance(
		final String name,
		final String minimumValue,
		final String maximumValue,
		final CovariateType covariateType) {

		ExposureCovariate exposureCovariate
			= new ExposureCovariate(
				name,
				minimumValue,
				maximumValue);
		exposureCovariate.setCovariateType(covariateType);
		
		return exposureCovariate;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalExposureCovariate the original exposure covariate
	 * @return the exposure covariate
	 */
	static public ExposureCovariate createCopy(
		final ExposureCovariate originalExposureCovariate) {
		
		if (originalExposureCovariate == null) {
			return null;
		}
		
		ExposureCovariate cloneExposureCovariate
            = new ExposureCovariate();
        cloneExposureCovariate.setName(originalExposureCovariate.getName());
        cloneExposureCovariate.setCovariateType(originalExposureCovariate.getCovariateType());
		return cloneExposureCovariate;			
	}	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================

	
	public void identifyDifferences(
		final ExposureCovariate anotherExposureCovariate,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherExposureCovariate, 
			differences);
	}
	
	
// ==========================================
// Section Errors and Validation
// ==========================================


	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();		
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {		
		
		String recordType = getRecordType();
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		super.checkErrors(
			validationPolicy, 
			errorMessages);
      
        CovariateType covariateType = getCovariateType();
        if (covariateType == null) {
	        String covariateTypeLabel
		        = RIFServiceMessages.getMessage("covariate.covariateType.label");
            String errorMessage 
                = GENERIC_MESSAGES.getMessage(
                	"general.validation.emptyRequiredRecordField",
                	recordType,
                    covariateTypeLabel);
            errorMessages.add(errorMessage);
        }
        
        countErrors(RIFServiceError.INVALID_EXPOSURE_COVARIATE, errorMessages);
    		
	}

// ==========================================
// Section Interfaces
// ==========================================
	
// ==========================================
// Section Override
// ==========================================


	@Override
	public String getRecordType() {
		
		String recordNameLabel
			= RIFServiceMessages.getMessage("exposureCovariate.label");
		return recordNameLabel;
	}
		
}
