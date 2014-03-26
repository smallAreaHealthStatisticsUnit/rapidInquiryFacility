package rifServices.taxonomyServices;


import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.Parameter;
import rifServices.dataStorageLayer.SQLCountQueryFormatter;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.dataStorageLayer.SQLSelectQueryFormatter;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * An implementation of the ICD 10 taxonomy provider.  ICD 9 and ICD 10 taxonomy
 * providers are part of the RIF's standard distribution because most 
 * of the use cases involve these taxonomies.  However, the architecture has
 * been designed so that 
 * {@link rifServices.dataStorageLayer.SQLHealthOutcomeManager} can essentially 
 * delegate its methods to any number of classes that implement the 
 * {@link rifServices.taxonomyServices.HealthCodeProvider} interface.  
 * 
 * <p>
 * Developers of other taxonomy providers can rely on
 * {@link rifServices.ProductionRIFJobSubmissionService} to check health code
 * fields for malicious field values and for SQLHealthOutcomeManager to
 * check that fields in a {@link rifServices.businessConceptLayer.HealthCode}
 * object are not blank.
 * </p>
 * 
 * <p>
 * However, developers should perform additional checks to ensure that the 
 * <code>code</code> field value complies with the coding conventions of the
 * taxonomy they're using.  For example, ICD 10 codes can have a length of 1,
 * 3, or 4.  If they have a length of 2 then they're invalid.
 * </p>
 * 
 * <p>
 * If you encounter a <code>code</code> value that violates the conventions of a
 * taxonomy, then create a suitable error message and use it to help throw
 * a {@link rifServices.system.RIFServiceException}.
 * </p>
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

public class ICD10TaxonomyProvider 
	implements HealthCodeProvider {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The Constant ICD_10_NAME_SPACE. */
	private static final String ICD_10_NAME_SPACE = "icd10";

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new IC d10 taxonomy provider.
	 */
	public ICD10TaxonomyProvider() {

	}

	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#initialise(java.util.ArrayList)
	 */
	public void initialise(
		final ArrayList<Parameter> parameters) 
		throws RIFServiceException {
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	
	//Interface: Taxonomy Provider
	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#getHealthCodeTaxonomy()
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomy() {
		
		String name
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.icd10.name");
		String description
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.icd10.description");
		String nameSpace
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.icd10.nameSpace");
		String version
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.icd10.version");
		
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.newInstance(
				name, 
				description, 
				nameSpace,
				version);
		return healthCodeTaxonomy;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#supportsTaxonomy(rifServices.businessConceptLayer.HealthCodeTaxonomy)
	 */
	public boolean supportsTaxonomy(
		final HealthCodeTaxonomy healthCodeTaxonomy) {

		Collator collator = RIFServiceMessages.getCollator();		
		if (collator.equals(healthCodeTaxonomy.getNameSpace(), ICD_10_NAME_SPACE)) {
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#supportsTaxonomy(rifServices.businessConceptLayer.HealthCode)
	 */
	public boolean supportsTaxonomy(
		final HealthCode healthCode) {
		
		Collator collator = RIFServiceMessages.getCollator();		
		if (collator.equals(healthCode.getNameSpace(), ICD_10_NAME_SPACE)) {
			return true;
		}
		
		return false;		
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#getTopLevelCodes(java.sql.Connection)
	 */
	public ArrayList<HealthCode> getTopLevelCodes(
		final Connection connection) 
		throws RIFServiceException {
		// TODO Auto-generated method stub
		
		SQLSelectQueryFormatter formatter 
			= new SQLSelectQueryFormatter();
		formatter.setUseDistinct(true);
		formatter.addSelectField("icd10_1char");
		formatter.addSelectField("text_1char");
		formatter.addFromTable("rif40_icd10");
		formatter.addOrderByCondition("icd10_1char");
		
		//Parameterise and execute query
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;

		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			dbResultSet = statement.executeQuery();
			while (dbResultSet.next()) {
				HealthCode healthCode = HealthCode.newInstance();
				healthCode.setCode(dbResultSet.getString(1));
				healthCode.setDescription(dbResultSet.getString(2));
				healthCode.setNameSpace(ICD_10_NAME_SPACE);
				healthCode.setTopLevelTerm(true);
				results.add(healthCode);
			}
			
			for (HealthCode result : results) {
				establishNumberOfSubTerms(
					connection, 
					result);				
			}
			
			return results;
		}
		catch(SQLException sqlException) {

			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.unableToGetTopLevelICDCodes",
					ICD_10_NAME_SPACE);
			
			Logger logger 
				= LoggerFactory.getLogger(ICD10TaxonomyProvider.class);
			logger.error(errorMessage, sqlException);			
																		
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_TOP_LEVEL_ICD_TERMS, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}				
	}

	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#getImmediateSubterms(java.sql.Connection, rifServices.businessConceptLayer.HealthCode)
	 */
	public ArrayList<HealthCode> getImmediateSubterms(
		final Connection connection,
		final HealthCode parentHealthCode) 
		throws RIFServiceException {

		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		System.out.println("ICD10 getImmediateSubterms 1");

		/*
		if (parentHealthCode.getNumberOfSubTerms() == 0) {
			//it's a term that has no children
			System.out.println("ICD10 getImmediateSubterms=="+parentHealthCode.getDisplayName()+"==is a leaf node");
			return results;
		}
		*/
		
		SQLSelectQueryFormatter formatter = new SQLSelectQueryFormatter();
		formatter.setUseDistinct(true);

		String parentCode = parentHealthCode.getCode();	
		if (parentHealthCode.isTopLevelTerm() == true) {			
			//we will assume that we mean icd10.
			System.out.println("ICD10 getImmediateSubterms IS top level term");
			formatter.addSelectField("icd10_3char");
			formatter.addSelectField("text_3char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_1char");
			formatter.addOrderByCondition("icd10_3char");				
		}
		else if (parentCode.length() == 3) {
			//we can use the length of the code to tell us
			//what level of the taxonomy we're using
			System.out.println("ICD10 getImmediateSubterms is NOT top level term");
			formatter.addSelectField("icd10_4char");
			formatter.addSelectField("text_4char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_3char");
			formatter.addOrderByCondition("icd10_4char");				
		}
		else {
			//stub
			System.out.println("ICD10 getImmediateSubterms stuff");			
			return results;			
		}				
	
		//Perform operation
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			statement.setString(1, parentHealthCode.getCode());
			dbResultSet = statement.executeQuery();
			while (dbResultSet.next()) {
				HealthCode healthCode = HealthCode.newInstance();				
				healthCode.setCode(dbResultSet.getString(1));
				healthCode.setDescription(dbResultSet.getString(2));
				healthCode.setNameSpace(parentHealthCode.getNameSpace());
				results.add(healthCode);
			}

			for (HealthCode result : results) {
				establishNumberOfSubTerms(
					connection, 
					result);				
			}

			return results;
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthOutcomeManager.error.unableToGetChildICDCodes",
					parentHealthCode.getDisplayName());

			Logger logger 
				= LoggerFactory.getLogger(ICD10TaxonomyProvider.class);
			logger.error(errorMessage, sqlException);			
																				
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_CHILDREN_FOR_ICD_CODE,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
	}

	/**
	 * Establish number of sub terms.
	 *
	 * @param connection the connection
	 * @param healthCode the health code
	 * @throws SQLException the SQL exception
	 * @throws RIFServiceException the RIF service exception
	 */
	private void establishNumberOfSubTerms(
		final Connection connection, 		
		final HealthCode healthCode) 
		throws SQLException, 
		RIFServiceException {
		
		String code = healthCode.getCode();

		SQLCountQueryFormatter formatter = new SQLCountQueryFormatter();		
		if (healthCode.isTopLevelTerm() == true) {	
			System.out.println("ICD10Taxonomy establishNumberTerms for=="+healthCode.getDisplayName()+"== is top level term!");
			//we will assume that we mean icd10.
			formatter.setCountField("icd10_3char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_1char");
			formatter.setUseDistinct(true);
		}	
		else if (code.length() == 3) {
			System.out.println("ICD10Taxonomy establishNumberTerms for=="+healthCode.getDisplayName()+"== is non-top level 3 char term!");
			//we can use the length of the code to tell us
			//what level of the taxonomy we're using
			formatter.setCountField("icd10_4char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_3char");
			formatter.setUseDistinct(true);
		}
		else {
			System.out.println("ICD10Taxonomy establishNumberTerms for=="+healthCode.getDisplayName()+"== is non-top level 5 char term!");
			//the code must have 5 characters length.
			//in this case, we are guaranteed that the code will
			//not have any sub terms
			healthCode.setNumberOfSubTerms(0);
			return;
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(formatter.generateQuery());
			statement.setString(1, healthCode.getCode());
			resultSet = statement.executeQuery();
			resultSet.next();
			int numberOfSubTerms = resultSet.getInt(1);
			healthCode.setNumberOfSubTerms(numberOfSubTerms);
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
		
	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#getParentHealthCode(java.sql.Connection, rifServices.businessConceptLayer.HealthCode)
	 */
	public HealthCode getParentHealthCode(
		final Connection connection,
		final HealthCode childHealthCode) 
		throws RIFServiceException {
		
		String childCode = childHealthCode.getCode();
		int childCodeLength = childCode.length();

		SQLSelectQueryFormatter formatter 
			= new SQLSelectQueryFormatter();
		
		//ICD codes are either length 1, 3 or 4
		if (childCodeLength == 4) {
			//obtain the third character precision health code
			formatter.setUseDistinct(true);
			formatter.addSelectField("icd10_3char");
			formatter.addSelectField("text_3char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_4char");			
		}
		else if (childCodeLength == 3) {
			//obtain the first character precision health code
			formatter.setUseDistinct(true);
			formatter.addSelectField("icd10_1char");
			formatter.addSelectField("text_1char");
			formatter.addFromTable("rif40_icd10");
			formatter.addWhereParameter("icd10_3char");
		}
		else {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"icd10HealthCodeProvider.error.nonCompliantCodeFormat",
					childHealthCode.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_ICD_CODE, 
					errorMessage);
			throw rifServiceException;
		}

		//Parameterise and execute query
		HealthCode result = HealthCode.newInstance();
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			statement.setString(1, childHealthCode.getCode());
			dbResultSet = statement.executeQuery();
			
			if (dbResultSet.next() == false) {
				//no parent available
				return null;
			}
						
			result.setCode(dbResultSet.getString(1));
			String description = dbResultSet.getString(2);			
			result.setDescription(description);
			result.setNameSpace(childHealthCode.getNameSpace());
			
			if (childCodeLength == 3) {
				//we know that the result must be a top level node
				result.setTopLevelTerm(true);		
			}	
			
			return result;
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("healthOutcomeManager.error.unableToGetParentICDCode");
			
			Logger logger 
				= LoggerFactory.getLogger(ICD10TaxonomyProvider.class);
			logger.error(errorMessage, sqlException);			
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_PARENT_FOR_ICD_CODE,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
	}
	
	/* (non-Javadoc)
	 * @see rifServices.taxonomyServices.HealthCodeProvider#getHealthCodes(java.sql.Connection, java.lang.String)
	 */
	public ArrayList<HealthCode> getHealthCodes(
		final Connection connection,
		final String searchText)
		throws RIFServiceException {
		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
	
		SQLSelectQueryFormatter getMatchingOneCharacterCodesFormatter = new SQLSelectQueryFormatter();
		getMatchingOneCharacterCodesFormatter.addSelectField("icd10_1char");
		getMatchingOneCharacterCodesFormatter.addSelectField("text_1char");
		getMatchingOneCharacterCodesFormatter.addFromTable("rif40_icd10");
		getMatchingOneCharacterCodesFormatter.addWhereLikeFieldName("icd10_1char");
		getMatchingOneCharacterCodesFormatter.addWhereLikeFieldName("text_1char");
		getMatchingOneCharacterCodesFormatter.orAllWhereConditions(true);
		
		SQLSelectQueryFormatter getMatchingThreeCharacterCodesFormatter = new SQLSelectQueryFormatter();		
		getMatchingThreeCharacterCodesFormatter.setUseDistinct(true);
		getMatchingThreeCharacterCodesFormatter.addSelectField("icd10_3char");
		getMatchingThreeCharacterCodesFormatter.addSelectField("text_3char");
		getMatchingThreeCharacterCodesFormatter.addFromTable("rif40_icd10");
		getMatchingThreeCharacterCodesFormatter.addWhereLikeFieldName("icd10_3char");
		getMatchingThreeCharacterCodesFormatter.addWhereLikeFieldName("text_3char");
		getMatchingThreeCharacterCodesFormatter.orAllWhereConditions(true);

		SQLSelectQueryFormatter getMatchingFourCharacterCodesFormatter = new SQLSelectQueryFormatter();
		getMatchingFourCharacterCodesFormatter.addSelectField("icd10_4char");
		getMatchingFourCharacterCodesFormatter.addSelectField("text_4char");
		getMatchingFourCharacterCodesFormatter.addFromTable("rif40_icd10");
		getMatchingFourCharacterCodesFormatter.addWhereLikeFieldName("icd10_4char");
		getMatchingFourCharacterCodesFormatter.addWhereLikeFieldName("text_4char");
		getMatchingFourCharacterCodesFormatter.orAllWhereConditions(true);
						
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		String searchCriteria = "%" + searchText + "%";
		try {
			statement
				= connection.prepareStatement(getMatchingOneCharacterCodesFormatter.generateQuery());
			statement.setString(1, searchCriteria);
			statement.setString(2, searchCriteria);
			resultSet = statement.executeQuery();
	
			while (resultSet.next()) {
				HealthCode healthCode 
					= HealthCode.newInstance(
						resultSet.getString(1), 
						ICD_10_NAME_SPACE, 
						resultSet.getString(2),
						true);
				results.add(healthCode);				
			}
	
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
			
			statement
				= connection.prepareStatement(getMatchingThreeCharacterCodesFormatter.generateQuery());
			statement.setString(1, searchCriteria);
			statement.setString(2, searchCriteria);
			resultSet = statement.executeQuery();
		
			while (resultSet.next()) {
				HealthCode healthCode 
					= HealthCode.newInstance(
						resultSet.getString(1), 
						ICD_10_NAME_SPACE, 
						resultSet.getString(2),
						false);
				results.add(healthCode);				
			}
				
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
			
			statement
				= connection.prepareStatement(getMatchingFourCharacterCodesFormatter.generateQuery());
			statement.setString(1, searchCriteria);
			statement.setString(2, searchCriteria);
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				HealthCode healthCode 
					= HealthCode.newInstance(
							resultSet.getString(1), 
							ICD_10_NAME_SPACE, 
							resultSet.getString(2),
							false);
				results.add(healthCode);				
			}
			
			for (HealthCode result : results) {
				establishNumberOfSubTerms(
					connection, 
					result);				
			}
			
			return results;			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("healthOutcomeManager.error.unableToGetHealthCodes");

			Logger logger 
				= LoggerFactory.getLogger(ICD10TaxonomyProvider.class);
			logger.error(errorMessage, sqlException);			
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_HEALTH_CODES,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
