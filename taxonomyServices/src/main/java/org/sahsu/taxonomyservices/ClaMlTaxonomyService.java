package org.sahsu.taxonomyservices;

import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.FederatedTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceAPI;
import org.sahsu.rif.generic.taxonomyservices.AbstractTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTermManager;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfiguration;
import org.sahsu.taxonomyservices.system.TaxonomyServiceError;

import org.sahsu.rif.generic.util.TaxonomyLogger;

import java.io.File;

/**
 * A taxonomy service that provides terms from ICD collections.  The main activity
 * of the class is to call a custom parser that understands the XML data format that
 * the WHO uses to represent ICD terms.  The parser registers the terms it reads into
 * an instance of {@link TaxonomyTermManager}, which
 * holds manages them in-memory.  The taxonomy term manager provides the main mechanism
 * by which the superclass {@link AbstractTaxonomyService}
 * supports most of the service calls.  
 * 
 * <p>
 * Most of the potential concurrency problems that may arise in simultaneous access to the
 * service are controlled by {@link FederatedTaxonomyService}.
 * </p>
 *  
 * 
 * 
 * </p>
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

public class ClaMlTaxonomyService
	extends AbstractTaxonomyService 
	implements TaxonomyServiceAPI {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	// ==========================================
	// Section Properties
	// ==========================================
	

	
	// ==========================================
	// Section Construction
	// ==========================================

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface TaxonomyServiceAPI

	public void initialiseService(
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration) 
		throws RIFServiceException {
			
		String name = "UNKNOWN";
		String description = "UNKNOWN";
		
		try {
			ICD10TaxonomyTermParser icd1011TaxonomyParser 
				= new ICD10TaxonomyTermParser();	// May need a separate ICD11 parser if the ClaML format is different		
			
			setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

			ArrayList<Parameter> parameters
				= taxonomyServiceConfiguration.getParameters();
		
			Parameter icd1011FileParameter = Parameter.getParameter("icd10_ClaML_file", parameters);
			if (icd1011FileParameter == null) {
				icd1011FileParameter = Parameter.getParameter("icd11_ClaML_file", parameters);
			}
			name = taxonomyServiceConfiguration.getName();
			description = taxonomyServiceConfiguration.getDescription();
			
			if (icd1011FileParameter == null) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10/11 taxonomy service: " + name + " is missing a parameter for \"icd10_ClaML_file\" or \"icd11_ClaML_file\"";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				throw rifServiceException;
			}		
		
			StringBuilder icd1011FileName = new StringBuilder();
			icd1011FileName.append(defaultResourceDirectoryPath);
			icd1011FileName.append(File.separator);
			icd1011FileName.append(icd1011FileParameter.getValue());
			
			File icd1011File = new File(icd1011FileName.toString());
			if (icd1011File.exists()) {		
				icd1011TaxonomyParser.readFile(icd1011File);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name + " read: \"" + icd1011FileName + "\".");
				setTaxonomyTermManager(icd1011TaxonomyParser.getTaxonomyTermManager());
				setServiceWorking(true);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name + " initialised: " + description + ".");
			}
			else {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10/11 taxonomy service: " + name + " file: \"" + icd1011FileParameter + "\" not found.";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				this.getClass(), 
				"ICD10/11 taxonomy service: " + name + " initialiseService() error", 
				rifServiceException);
			setServiceWorking(false);
		}
	}


	// ==========================================
	// Section Override
	// ==========================================

}

