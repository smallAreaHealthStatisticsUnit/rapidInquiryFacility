package taxonomyServices;

import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI;
import rifGenericLibrary.taxonomyServices.AbstractTaxonomyService;
import rifGenericLibrary.taxonomyServices.TaxonomyServiceConfiguration;
import taxonomyServices.system.TaxonomyServiceError;

import java.io.File;

/**
 * A taxonomy service that provides terms from ICD collections.  The main activity
 * of the class is to call a custom parser that understands the XML data format that
 * the WHO uses to represent ICD terms.  The parser registers the terms it reads into
 * an instance of {@link rifGenericLibrary.taxonomyServices.TaxonomyTermManager}, which
 * holds manages them in-memory.  The taxonomy term manager provides the main mechanism
 * by which the superclass {@link rifGenericLibrary.taxonomyServices.AbstractTaxonomyService}
 * supports most of the service calls.  
 * 
 * <p>
 * Most of the potential concurrency problems that may arise in simultaneous access to the
 * service are controlled by {@link rifGenericLibrary.taxonomyServices.FederatedTaxonomyService}.
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

public class ICDTaxonomyService 
	extends AbstractTaxonomyService 
	implements TaxonomyServiceAPI {

	// ==========================================
	// Section Constants
	// ==========================================

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
		
		try {
			ICD10TaxonomyTermParser icd10TaxonomyParser 
				= new ICD10TaxonomyTermParser();			
			
			setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

			ArrayList<Parameter> parameters
				= taxonomyServiceConfiguration.getParameters();
		
			Parameter icd10FileParameter
				= Parameter.getParameter("icd10_ClaML_file", parameters);
			
			if (icd10FileParameter == null) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10 taxonomy service is missing a parameter for \"icd10_ClaML_file\"";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				setServiceWorking(false);
				throw rifServiceException;
			}		
		
			StringBuilder icd10FileName = new StringBuilder();
			icd10FileName.append(defaultResourceDirectoryPath);
			icd10FileName.append(File.separator);
			icd10FileName.append(icd10FileParameter.getValue());
			
			File icd10File = new File(icd10FileName.toString());
			
			icd10TaxonomyParser.readFile(icd10File);
			setTaxonomyTermManager(icd10TaxonomyParser.getTaxonomyTermManager());
			setServiceWorking(true);
		}
		catch(RIFServiceException rifServiceException) {
			setServiceWorking(false);
		}
	}


	// ==========================================
	// Section Override
	// ==========================================

}

