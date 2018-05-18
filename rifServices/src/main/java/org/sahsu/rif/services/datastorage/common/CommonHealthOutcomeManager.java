package org.sahsu.rif.services.datastorage.common;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.common.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.pg.PGSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.ClassFileLocator;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractRIFConcept.ValidationPolicy;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.ontologyservices.HealthCodeProviderInterface;
import org.sahsu.rif.services.ontologyservices.ICD10ClaMLTaxonomyProvider;
import org.sahsu.rif.services.ontologyservices.RIFXMLTaxonomyProvider;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class CommonHealthOutcomeManager implements HealthOutcomeManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	/** The health code providers. */
	private ArrayList<HealthCodeProviderInterface> healthCodeProviders;
	
	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;

	/**
	 * Instantiates a new SQL health outcome manager.
	 */
	public CommonHealthOutcomeManager(final RIFServiceStartupOptions rifServiceStartupOptions) {

		healthCodeProviders = new ArrayList<>();

		String targetPathValue;
		try {
			targetPathValue = rifServiceStartupOptions.getRIFServiceResourcePath();
		} catch (RIFServiceException e) {
			throw new IllegalStateException(getClass().getSimpleName() + ": problem getting "
			                                + "startup options", e);
		}
		if (targetPathValue == null) {
			targetPathValue = ClassFileLocator.getClassRootLocation("rifServices");
		}

		//initialise each health code provider
		try {
			RIFXMLTaxonomyProvider icd9TaxonomyProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters = new ArrayList<>();
			Parameter inputFileParameter = Parameter.newInstance();
			inputFileParameter.setName("input_file");
			final String icd9CodesFileLocation = targetPathValue
			                                     + File.separator
			                                     + "ExampleICD9Codes.xml";
			inputFileParameter.setValue(icd9CodesFileLocation);
			parameters.add(inputFileParameter);
			icd9TaxonomyProvider.initialise(parameters);
			healthCodeProviders.add(icd9TaxonomyProvider);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				getClass(),
				"constructor", 
				rifServiceException);
		}
				
		try {
			RIFXMLTaxonomyProvider icd10TaxonomyProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters = new ArrayList<>();
			Parameter inputFileParameter = Parameter.newInstance();
			inputFileParameter.setName("input_file");
			final String icd10CodesFileLocation = targetPathValue
			                                      + File.separator
			                                      + "ExampleICD10Codes.xml";
			inputFileParameter.setValue(icd10CodesFileLocation);
			parameters.add(inputFileParameter);
			icd10TaxonomyProvider.initialise(parameters);
			healthCodeProviders.add(icd10TaxonomyProvider);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				getClass(),
				"constructor", 
				rifServiceException);
		}
		
		try {
			RIFXMLTaxonomyProvider opcsCodeProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters2 = new ArrayList<>();
			Parameter inputFileParameter2 = Parameter.newInstance();
			inputFileParameter2.setName("input_file");
			final String opcsCodesFileLocation = targetPathValue
			                                     + File.separator
			                                     + "ExampleOPCSCodes.xml";
			inputFileParameter2.setValue(opcsCodesFileLocation);
			parameters2.add(inputFileParameter2);
	
			opcsCodeProvider.initialise(parameters2);
			healthCodeProviders.add(opcsCodeProvider);	
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				getClass(),
				"constructor", 
				rifServiceException);
		}

		//Add by Nan Lin 15/07/2015.
		try {
			ICD10ClaMLTaxonomyProvider icd10ClaMLCodeProvider = new ICD10ClaMLTaxonomyProvider();
			ArrayList<Parameter> paras = new ArrayList<>();
			Parameter inputFilePara = Parameter.newInstance();
			inputFilePara.setName("icd10_ClaML_file");
			final String icd10ClaMLCodesFileLocation = targetPathValue
			                                           + File.separator
			                                           + "ExampleClaMLICD10Codes.xml";
			inputFilePara.setValue(icd10ClaMLCodesFileLocation);
			paras.add(inputFilePara);
	
			icd10ClaMLCodeProvider.initialise(paras);
			healthCodeProviders.add(icd10ClaMLCodeProvider);	
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				getClass(),
				"constructor", 
				rifServiceException);
		}
	}
	/*
	 * Method used for testing purposes
	 */

	/**
	 * Gets the health code taxonomies.
	 *
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
		final String healthCodeTaxonomyNameSpace)
		throws RIFServiceException {


		HealthCodeProviderInterface healthCodeProvider
			= getRelevantHealthCodeProvider(healthCodeTaxonomyNameSpace);

		if (healthCodeProvider == null) {
			//ERROR: no taxonomy for given name space
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.noHealthCodeTaxonomyForNameSpace",
					healthCodeTaxonomyNameSpace);
			throw new RIFServiceException(
					RIFServiceError.NO_HEALTH_TAXONOMY_FOR_NAMESPACE,
					errorMessage);
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
	@Override
	public ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies()
		throws RIFServiceException {
		
		ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
			= new ArrayList<HealthCodeTaxonomy>();
		for (HealthCodeProviderInterface healthCodeProvider : healthCodeProviders) {
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
	@Override
	public ArrayList<HealthCode> getHealthCodesForInvestigation(
		final Connection connection,
		final User user,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation)
		throws RIFServiceException {
			
		PGSQLSelectQueryFormatter queryFormatter 
			= new PGSQLSelectQueryFormatter();
		queryFormatter.addSelectField("condition");
		queryFormatter.addFromTable("t_rif40_inv_conditions");
		queryFormatter.addWhereParameter("inv_id");
		queryFormatter.addWhereParameter("study_id");
		
		ArrayList<HealthCode> results 
			= new ArrayList<HealthCode>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					queryFormatter);
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
			
			rifLogger.error(
				getClass(),
				errorMessage, 
				exception);

			throw new RIFServiceException(
				RIFServiceError.GET_HEALTH_OUTCOMES_FOR_INVESTIGATION,
				errorMessage);
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
	@Override
	public ArrayList<HealthCode> getTopLevelCodes(
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {

		//Validate Parameters
		healthCodeTaxonomy.checkErrors(ValidationPolicy.STRICT);
		
		HealthCodeProviderInterface healthCodeProvider
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
	@Override
	public ArrayList<HealthCode> getImmediateSubterms(
		final HealthCode parentHealthCode) 
		throws RIFServiceException {
		
		//Validate Parameters
		parentHealthCode.checkErrors(validationPolicy);
		
		HealthCodeProviderInterface healthCodeProvider
			= getRelevantHealthCodeProvider(parentHealthCode);		
		return healthCodeProvider.getImmediateSubterms(parentHealthCode);		
	}

	@Override
	public HealthCode getHealthCode(
		final String code,
		final String nameSpace)
		throws RIFServiceException {
		
		
		HealthCodeProviderInterface healthCodeProvider
			= getRelevantHealthCodeProvider(nameSpace);	
		if (healthCodeProvider == null) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.noHealthCodeTaxonomyForNameSpace",
					nameSpace);
			throw new RIFServiceException(
				RIFServiceError.NO_HEALTH_TAXONOMY_FOR_NAMESPACE,
				errorMessage);
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
	@Override
	public HealthCode getParentHealthCode(
		final HealthCode childHealthCode) 
		throws RIFServiceException {
				
		//Validate Parameters
		childHealthCode.checkErrors(validationPolicy);
				
		HealthCodeProviderInterface healthCodeProvider
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
	private HealthCodeProviderInterface getRelevantHealthCodeProvider(
		final HealthCode healthCode) 
		throws RIFServiceException {
		
		HealthCodeProviderInterface relevantHealthCodeProvider = null;
		for (HealthCodeProviderInterface healthCodeProvider : healthCodeProviders) {
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
			throw new RIFServiceException(
					RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, errorMessage);
		}
		
		return relevantHealthCodeProvider;
	}

	private HealthCodeProviderInterface getRelevantHealthCodeProvider(
		final String nameSpace) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		for (HealthCodeProviderInterface healthCodeProvider : healthCodeProviders) {
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
	private HealthCodeProviderInterface getRelevantHealthCodeProvider(
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {
			
		HealthCodeProviderInterface relevantHealthCodeProvider = null;
		for (HealthCodeProviderInterface healthCodeProvider : healthCodeProviders) {
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
			throw new RIFServiceException(
					RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, errorMessage);
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
	@Override
	public ArrayList<HealthCode> getHealthCodes(
		final HealthCodeTaxonomy healthCodeTaxonomy,
		final String searchText,
		final boolean isCaseSensitive) 
		throws RIFServiceException {
		
		
		HealthCodeProviderInterface healthCodeProvider
			= getRelevantHealthCodeProvider(healthCodeTaxonomy);
		
		return healthCodeProvider.getHealthCodes(
			searchText,
			isCaseSensitive);
		
	}
}
