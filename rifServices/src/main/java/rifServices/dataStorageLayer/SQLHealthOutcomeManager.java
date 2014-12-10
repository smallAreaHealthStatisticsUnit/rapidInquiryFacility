package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.DiseaseMappingStudy;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.Parameter;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.util.RIFLogger;
import rifServices.taxonomyServices.HealthCodeProvider;
import rifServices.taxonomyServices.RIFXMLTaxonomyProvider;

import java.util.ArrayList;
import java.io.File;
import java.sql.*;
import java.text.Collator;



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

class SQLHealthOutcomeManager 
	extends AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	/** The health code providers. */
	private ArrayList<HealthCodeProvider> healthCodeProviders;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL health outcome manager.
	 */
	public SQLHealthOutcomeManager(final RIFServiceStartupOptions rifServiceStartupOptions) {
		healthCodeProviders = new ArrayList<HealthCodeProvider>();
		
		String targetPathValue
			= rifServiceStartupOptions.getRIFServiceResourcePath();
		if (targetPathValue == null) {
			targetPathValue = RIFServiceMessages.getRIFServicesClassRootLocation();
		}
		System.out.println("SQLHealthOutcomeManager targetClassPathDirectory=="+targetPathValue+"==");
		
		//initialise each health code provider
		try {			
			StringBuilder icd9CodesFileLocation = new StringBuilder();
			icd9CodesFileLocation.append(targetPathValue);
			icd9CodesFileLocation.append(File.separator);
			icd9CodesFileLocation.append("ExampleICD9Codes.xml");

			RIFXMLTaxonomyProvider icd9TaxonomyProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters = new ArrayList<Parameter>();
			Parameter inputFileParameter = Parameter.newInstance();
			inputFileParameter.setName("input_file");		
			inputFileParameter.setValue(icd9CodesFileLocation.toString());
			parameters.add(inputFileParameter);
			icd9TaxonomyProvider.initialise(parameters);
			healthCodeProviders.add(icd9TaxonomyProvider);
		}
		catch(RIFServiceException rifServiceException) {
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLHealthOutcomeManager.class, 
				"constructor", 
				rifServiceException);
		}
				
		try {			
			StringBuilder icd10CodesFileLocation = new StringBuilder();
			icd10CodesFileLocation.append(targetPathValue);
			icd10CodesFileLocation.append(File.separator);
			icd10CodesFileLocation.append("ExampleICD10Codes.xml");

			RIFXMLTaxonomyProvider icd10TaxonomyProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters = new ArrayList<Parameter>();
			Parameter inputFileParameter = Parameter.newInstance();
			inputFileParameter.setName("input_file");		
			inputFileParameter.setValue(icd10CodesFileLocation.toString());
			parameters.add(inputFileParameter);
			icd10TaxonomyProvider.initialise(parameters);
			healthCodeProviders.add(icd10TaxonomyProvider);
		}
		catch(RIFServiceException rifServiceException) {
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLHealthOutcomeManager.class, 
				"constructor", 
				rifServiceException);
		}
		
		try {			
			StringBuilder opcsCodesFileLocation = new StringBuilder();
			opcsCodesFileLocation.append(targetPathValue);
			opcsCodesFileLocation.append(File.separator);
			opcsCodesFileLocation.append("ExampleOPCSCodes.xml");
	
			RIFXMLTaxonomyProvider opcsCodeProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters2 = new ArrayList<Parameter>();
			Parameter inputFileParameter2 = Parameter.newInstance();
			inputFileParameter2.setName("input_file");
			inputFileParameter2.setValue(opcsCodesFileLocation.toString());
			parameters2.add(inputFileParameter2);
	
			opcsCodeProvider.initialise(parameters2);
			healthCodeProviders.add(opcsCodeProvider);	
		}
		catch(RIFServiceException rifServiceException) {
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLHealthOutcomeManager.class, 
				"constructor", 
				rifServiceException);
		}

		
		/*
		ICD9TaxonomyProvider icd9TaxonomyProvider 
			= new ICD9TaxonomyProvider();
		healthCodeProviders.add(icd9TaxonomyProvider);		
		
		ICD10TaxonomyProvider icd10TaxonomyProvider 
			= new ICD10TaxonomyProvider();
		healthCodeProviders.add(icd10TaxonomyProvider);		
		*/		
	}

	/**
	 * Initialise taxonomies.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	public void initialiseTaxomies() 
		throws RIFServiceException {

		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/*
	 * Method used for testing purposes
	 */
	/**
	 * Clear health code providers.
	 */
	public void clearHealthCodeProviders() {
		
		healthCodeProviders.clear();
	}
	
	/**
	 * Adds the health code provider.
	 *
	 * @param healthCodeProvider the health code provider
	 */
	public void addHealthCodeProvider(
		final HealthCodeProvider healthCodeProvider) {

		healthCodeProviders.add(healthCodeProvider);
	}
	
	/**
	 * Clear health code providers.
	 *
	 * @param healthCodeProvider the health code provider
	 */
	public void clearHealthCodeProviders(
		final HealthCodeProvider healthCodeProvider) {

		healthCodeProviders.clear();	
	}
	
	/**
	 * Gets the health code taxonomies.
	 *
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
		final String healthCodeTaxonomyNameSpace)
		throws RIFServiceException {


		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(healthCodeTaxonomyNameSpace);

		if (healthCodeProvider == null) {
			//ERROR: no taxonomy for given name space
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.noHealthCodeTaxonomyForNameSpace",
					healthCodeTaxonomyNameSpace);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.NO_HEALTH_TAXONOMY_FOR_NAMESPACE, 
					errorMessage);
			throw rifServiceException;
		}
		else {
			return healthCodeProvider.getHealthCodeTaxonomy();
		}
	}
	
	/**
	 * Gets the health code taxonomies.
	 *
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies() 
		throws RIFServiceException {
		
		ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
			= new ArrayList<HealthCodeTaxonomy>();
		for (HealthCodeProvider healthCodeProvider : healthCodeProviders) {
			healthCodeTaxonomies.add(healthCodeProvider.getHealthCodeTaxonomy());
		}

		return healthCodeTaxonomies;		
	}
		
	
	/**
	 * Gets the health codes for investigation.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param diseaseMappingStudy the disease mapping study
	 * @param investigation the investigation
	 * @return the health codes for investigation
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getHealthCodesForInvestigation(
		final Connection connection,
		final User user,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation) 
		throws RIFServiceException {
			
		SQLSelectQueryFormatter formatter 
			= new SQLSelectQueryFormatter();
		formatter.addSelectField("condition");
		formatter.addFromTable("t_rif40_inv_conditions");
		formatter.addWhereParameter("inv_id");
		formatter.addWhereParameter("study_id");
		
		ArrayList<HealthCode> results 
			= new ArrayList<HealthCode>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(formatter.generateQuery());
			Integer investigationID
				= Integer.valueOf(investigation.getIdentifier());
			statement.setInt(1, investigationID);
			Integer studyID
				= Integer.valueOf(diseaseMappingStudy.getIdentifier());
			statement.setInt(2, studyID);
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				//TODO KLG: how do we
				String icdCode
					= resultSet.getString(1);
				HealthCode healthCode
					= HealthCode.newInstance();
				healthCode.setCode(icdCode);
				
				//TODO KLG: Missing values for name space and description
				results.add(healthCode);
			}			
		}
		catch(Exception exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.db.unableToGetHealthOutcomesForInvestigation",
					diseaseMappingStudy.getDisplayName(),
					investigation.getDisplayName());
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLHealthOutcomeManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_HEALTH_OUTCOMES_FOR_INVESTIGATION, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}	
		
		return results;
	}
	
	/**
	 * Gets the top level codes.
	 *
	 * @param connection the connection
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getTopLevelCodes(
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {

		//Validate Parameters
		healthCodeTaxonomy.checkErrors();
		
		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(healthCodeTaxonomy);		
		return healthCodeProvider.getTopLevelCodes();
	}
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param connection the connection
	 * @param parentHealthCode the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getImmediateSubterms(
		final HealthCode parentHealthCode) 
		throws RIFServiceException {
		
		//Validate Parameters
		parentHealthCode.checkErrors();
		
		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(parentHealthCode);		
		return healthCodeProvider.getImmediateSubterms(parentHealthCode);		
	}
	
	public HealthCode getHealthCode(
		final String code,
		final String nameSpace)
		throws RIFServiceException {
		
		
		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(nameSpace);	
		if (healthCodeProvider == null) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.noHealthCodeTaxonomyForNameSpace",
					nameSpace);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.NO_HEALTH_TAXONOMY_FOR_NAMESPACE, 
					errorMessage);
			throw rifServiceException;
		}
		
		return healthCodeProvider.getHealthCode(code, nameSpace);
	}
		
	/**
	 * Gets the parent health code.
	 *
	 * @param connection the connection
	 * @param childHealthCode the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCode getParentHealthCode(
		final HealthCode childHealthCode) 
		throws RIFServiceException {
				
		//Validate Parameters
		childHealthCode.checkErrors();
				
		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(childHealthCode);		
		return healthCodeProvider.getParentHealthCode(childHealthCode);		
	}

	/**
	 * Gets the relevant health code provider.
	 *
	 * @param healthCode the health code
	 * @return the relevant health code provider
	 * @throws RIFServiceException the RIF service exception
	 */
	private HealthCodeProvider getRelevantHealthCodeProvider(
		final HealthCode healthCode) 
		throws RIFServiceException {
		
		HealthCodeProvider relevantHealthCodeProvider = null;
		for (HealthCodeProvider healthCodeProvider : healthCodeProviders) {
			if (healthCodeProvider.supportsTaxonomy(healthCode)) {
				relevantHealthCodeProvider = healthCodeProvider;
				break;
			}
		}
		
		if (relevantHealthCodeProvider == null) {
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"healthCodeProvider.error.nonExistentHealthCodeProvider",
					healthCode.getNameSpace());
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, errorMessage);
			throw rifServiceException;					
		}
		
		return relevantHealthCodeProvider;
	}

	private HealthCodeProvider getRelevantHealthCodeProvider(
		final String nameSpace) {
		
		Collator collator = RIFServiceMessages.getCollator();
		for (HealthCodeProvider healthCodeProvider : healthCodeProviders) {
			HealthCodeTaxonomy healthCodeTaxonomy
				= healthCodeProvider.getHealthCodeTaxonomy();
			String currentTaxonomyNameSpace
				= healthCodeTaxonomy.getNameSpace();
			if (collator.equals(nameSpace, currentTaxonomyNameSpace)) {
				return healthCodeProvider;
			}			
		}
		
		return null;
	}
	
	/**
	 * Gets the relevant health code provider.
	 *
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return the relevant health code provider
	 * @throws RIFServiceException the RIF service exception
	 */
	private HealthCodeProvider getRelevantHealthCodeProvider(
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {
			
		HealthCodeProvider relevantHealthCodeProvider = null;
		for (HealthCodeProvider healthCodeProvider : healthCodeProviders) {
			if (healthCodeProvider.supportsTaxonomy(healthCodeTaxonomy)) {
				relevantHealthCodeProvider = healthCodeProvider;
				break;
			}
		}
			
		if (relevantHealthCodeProvider == null) {
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"healthCodeProvider.error.nonExistentHealthCodeProvider",
					healthCodeTaxonomy.getNameSpace());
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, errorMessage);
			throw rifServiceException;					
		}
			
		return relevantHealthCodeProvider;
	}

	/**
	 * Gets the health codes.
	 *
	 * @param connection the connection
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @param searchText the search text
	 * @return the health codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getHealthCodes(
		final HealthCodeTaxonomy healthCodeTaxonomy,
		final String searchText) 
		throws RIFServiceException {
		
		HealthCodeProvider healthCodeProvider
			= getRelevantHealthCodeProvider(healthCodeTaxonomy);
		
		return healthCodeProvider.getHealthCodes(searchText);
		
	}

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	/**
	 * for each health code this routine tries to determine whether the health
	 * code exists in the health code provider described by the name space.  For
	 * example, suppose a HealthCode describes an ICD10 term.  It will search for
	 * the code in whatever health code provider is associated with the namespace
	 * "icd10".
	 * 
	 * @param healthCodes
	 * @throws RIFServiceException
	 */
	public void checkNonExistentHealthCodes(
		final ArrayList<HealthCode> healthCodes) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		for (HealthCode healthCode : healthCodes) {
			HealthCodeTaxonomy healthCodeTaxonomy
				= getHealthCodeTaxonomyFromNameSpace(healthCode.getNameSpace());
			if (healthCodeTaxonomy == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"healthOutcomeManager.error.noHealthCodeTaxonomyForNameSpace",
						healthCode.getNameSpace());
				errorMessages.add(errorMessage);
			}
			else {
				HealthCodeProvider healthCodeProvider
					= getRelevantHealthCodeProvider(healthCodeTaxonomy);
				if (healthCodeProvider == null) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"healthOutcomeManager.error.noProviderForHealthCodeTaxonomy",
							healthCode.getCode(),
							healthCodeTaxonomy.getDisplayName());
					errorMessages.add(errorMessage);
				}
				else if (healthCodeProvider.healthCodeExists(healthCode) == false) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"healthOutcomeManager.error.nonExistentHealthCode",
							healthCode.getCode(),
							healthCodeTaxonomy.getDisplayName());
					errorMessages.add(errorMessage);
				}
			}
		}

		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.NON_EXISTENT_HEALTH_CODE, 
					errorMessages);
			
			throw rifServiceException;
		}
	}
			
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}



