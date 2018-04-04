package rifServices.dataStorageLayer.ms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Sex;
import rifServices.businessConceptLayer.YearRange;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class MSSQLAgeGenderYearManager extends MSSQLAbstractSQLManager
		implements AgeGenderYearManager  {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	
	/** The sql rif context manager. */
	private RIFContextManager sqlRIFContextManager;

	/**
	 * Instantiates a new SQL age gender year manager.
	 *
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	public MSSQLAgeGenderYearManager(final RIFContextManager sqlRIFContextManager,
			final RIFServiceStartupOptions options) {

		super(options);
		this.sqlRIFContextManager = sqlRIFContextManager;
	}

	/**
	 * Gets the age groups.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @param sortingOrder the sorting order
	 * @return the age groups
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public ArrayList<AgeGroup> getAgeGroups(final User user, final Connection connection,
			final Geography geography, final NumeratorDenominatorPair ndPair,
			final AgeGroupSortingOption sortingOrder) throws RIFServiceException {
				
		//Validate parameters
		validateCommonMethodParameters(
			user,
			connection,
			geography,
			ndPair);

		PreparedStatement getAgeIDStatement = null;
		ResultSet getAgeIDResultSet = null;
		PreparedStatement getAgesForAgeGroupStatement = null;
		ResultSet getAgesForAgeGroupResultSet = null;
		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();		
		try {

			//Create query
			Integer ageGroupID = null;
			PGSQLSelectQueryFormatter getAgeIDQueryFormatter 
				= new PGSQLSelectQueryFormatter();
			sqlRIFContextManager.configureQueryFormatterForDB(getAgeIDQueryFormatter);
			getAgeIDQueryFormatter.addSelectField("age_group_id");
			getAgeIDQueryFormatter.addFromTable("rif40.rif40_tables");
			getAgeIDQueryFormatter.addWhereParameter("table_name");
			getAgeIDQueryFormatter.addWhereParameter("isnumerator");
			
			sqlRIFContextManager.logSQLQuery(
				"getAgeIDQuery",
				getAgeIDQueryFormatter,
				ndPair.getNumeratorTableName(),
				String.valueOf(1));
		
			getAgeIDStatement
				= sqlRIFContextManager.createPreparedStatement(
					connection, 
					getAgeIDQueryFormatter);
			getAgeIDStatement.setString(1, ndPair.getNumeratorTableName());
			//set isnumerator flag to 'true'
			getAgeIDStatement.setInt(2, 1);
			getAgeIDResultSet = getAgeIDStatement.executeQuery();
			
			if (!getAgeIDResultSet.next()) {
				//ERROR: No entry available
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlAgeGenderYearManager.error.noAgeGroupIDForNumeratorTable",
						ndPair.getNumeratorTableDescription());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_AGE_GROUP_ID_FOR_NUMERATOR, 
						errorMessage);

				connection.commit();
				
				throw rifServiceException;
			}
			else {
				ageGroupID = getAgeIDResultSet.getInt(1);
			}
			
			if (ageGroupID == null) {
				
				connection.commit();
				return results;
			}
		
			//Step II: Obtain the list of age groups that are appropriate
			//for the age group ID associated with the numerator table
			//The age group id helps group together age groups based on different
			//needs.  "1" may represent the standard breakdown of age ranges.
			//"2" may represent age ranges that are broken down every 4 years
			//After obtaining the list of age groups having the correct age group id
			//sort them by low_age
			PGSQLSelectQueryFormatter getAgesForAgeGroupID 
				= new PGSQLSelectQueryFormatter();
			sqlRIFContextManager.configureQueryFormatterForDB(getAgesForAgeGroupID);

			getAgesForAgeGroupID.addSelectField("age_group_id");
			getAgesForAgeGroupID.addSelectField("low_age");
			getAgesForAgeGroupID.addSelectField("high_age");
			getAgesForAgeGroupID.addSelectField("fieldname");
			getAgesForAgeGroupID.addFromTable("rif40.rif40_age_groups");
			getAgesForAgeGroupID.addWhereParameter("age_group_id");
		
			if ((sortingOrder == null) ||
				(sortingOrder == AgeGroupSortingOption.ASCENDING_LOWER_LIMIT)) {
				getAgesForAgeGroupID.addOrderByCondition(
					"low_age", 
					PGSQLSelectQueryFormatter.SortOrder.ASCENDING);			
			}
			else if (sortingOrder == AgeGroupSortingOption.DESCENDING_LOWER_LIMIT) {
				getAgesForAgeGroupID.addOrderByCondition(
					"low_age",
					PGSQLSelectQueryFormatter.SortOrder.DESCENDING);
			}
			else if (sortingOrder == AgeGroupSortingOption.ASCENDING_UPPER_LIMIT) {
				getAgesForAgeGroupID.addOrderByCondition(
					"high_age",
					PGSQLSelectQueryFormatter.SortOrder.ASCENDING);		
			}
			else {
				//it must be descending lower limit.		
				getAgesForAgeGroupID.addOrderByCondition(
					"high_age",
					PGSQLSelectQueryFormatter.SortOrder.DESCENDING);		
				assert sortingOrder == AgeGroupSortingOption.DESCENDING_UPPER_LIMIT;			
			}
			
			sqlRIFContextManager.logSQLQuery(
				"getAgesForAgeGroupIDQuery",
				getAgesForAgeGroupID,
				String.valueOf(ageGroupID));
				
			//Execute query and generate results
			getAgesForAgeGroupStatement
				= sqlRIFContextManager.createPreparedStatement(
					connection, 
					getAgesForAgeGroupID);				
			getAgesForAgeGroupStatement.setInt(1, ageGroupID);
			getAgesForAgeGroupResultSet = getAgesForAgeGroupStatement.executeQuery();
			connection.commit();
			
			while (getAgesForAgeGroupResultSet.next()) {
				AgeGroup ageGroup = AgeGroup.newInstance();
				ageGroup.setIdentifier(String.valueOf(getAgesForAgeGroupResultSet.getInt(1)));
				ageGroup.setLowerLimit(String.valueOf(getAgesForAgeGroupResultSet.getInt(2)));
				ageGroup.setUpperLimit(String.valueOf(getAgesForAgeGroupResultSet.getInt(3)));
				ageGroup.setName(getAgesForAgeGroupResultSet.getString(4));
				results.add(ageGroup);
			}
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			sqlRIFContextManager.logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage("ageGroup.error.unableToGetAgeGroups");

			rifLogger.error(
				MSSQLAgeGenderYearManager.class, 
				errorMessage, 
				sqlException);
						
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(getAgeIDStatement);
			PGSQLQueryUtility.close(getAgeIDResultSet);			
			PGSQLQueryUtility.close(getAgesForAgeGroupStatement);
			PGSQLQueryUtility.close(getAgesForAgeGroupResultSet);
		}
		return results;		
	}
	
	/**
	 * Gets the genders.
	 *
	 * @return the genders
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Sex> getGenders()
		throws RIFServiceException {
		
		ArrayList<Sex> results = new ArrayList<Sex>();
		results.add(Sex.MALES);
		results.add(Sex.FEMALES);
		results.add(Sex.BOTH);
		
		return results;		
	}
	
	
	/**
	 * Gets the year range.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @return the year range
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public YearRange getYearRange(final User user, final Connection connection,
			final Geography geography, final NumeratorDenominatorPair ndPair)
			throws RIFServiceException {
		
		//Validate parameters
		validateCommonMethodParameters(
			user,
			connection,
			geography,
			ndPair);

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			//Create query
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			sqlRIFContextManager.configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("year_start");
			queryFormatter.addSelectField("year_stop");
			queryFormatter.addFromTable("rif40.rif40_tables");
			queryFormatter.addWhereParameter("table_name");
			
			sqlRIFContextManager.logSQLQuery(
				"getYearRange",
				queryFormatter,
				ndPair.getNumeratorTableName());
				
			statement 
				= sqlRIFContextManager.createPreparedStatement(
					connection, 
					queryFormatter);
						
			//TOUR_SECURITY
			/*
			 * Using PreparedStatements means that even if the numerator table name
			 * somehow contained malicious SQL commands, the value would be properly escaped
			 * so that in a query it would be regarded as just another text value.
			 */
			statement.setString(1, ndPair.getNumeratorTableName());
			resultSet = statement.executeQuery();
			//there should be exactly one result
			if (!resultSet.next()) {
				//no entry found in the rif40 tables
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlAgeGenderYearManager.error.noStartEndYearForNumeratorTable",
						ndPair.getNumeratorTableDescription());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_START_END_YEAR_FOR_NUMERATOR, 
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
			}
			
			int yearStartValue = resultSet.getInt(1);
			int yearEndValue = resultSet.getInt(2);
			YearRange result 
				= YearRange.newInstance(
						String.valueOf(yearStartValue), 
						String.valueOf(yearEndValue));
			
			connection.commit();
			return result;
		}
		catch(SQLException sqlException) {	
			
			//TOUR_SECURITY  
			/*When we encounter an SQL exception,
			 * log it, but then throw an application-specific exception that
			 * will not contain any sensitive information.
			 */

			//Record original exception, throw sanitised, human-readable version			
			sqlRIFContextManager.logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlAgeGenderYearManager.error.unableToGetStartEndYear",
					ndPair.getDisplayName());
			
			rifLogger.error(
				MSSQLAgeGenderYearManager.class, 
				errorMessage, 
				sqlException);
			
			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
	}

	/**
	 * Validate common method parameters.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @throws RIFServiceException the RIF service exception
	 */
	private void validateCommonMethodParameters(
		final User user,
		final Connection connection,
		final Geography geography,
		final NumeratorDenominatorPair ndPair) 
		throws RIFServiceException {

		ValidationPolicy validationPolicy = sqlRIFContextManager.getValidationPolicy();
		
		if (geography != null) {
			geography.checkErrors(validationPolicy);
		
			sqlRIFContextManager.checkGeographyExists(
				connection, 
				geography.getName());	
		}
		if (ndPair != null) {
			ndPair.checkErrors(validationPolicy);
			sqlRIFContextManager.checkNDPairExists(user, connection, geography, ndPair);
		}
	}

	public void checkNonExistentAgeGroups(
		final Connection connection,
		final NumeratorDenominatorPair ndPair,
		final ArrayList<AgeBand> ageBands) 
		throws RIFServiceException {
			
		for (AgeBand ageBand : ageBands) {
			rifLogger.info(this.getClass(), "SQGYM checkNonExistentAgeGroups age band=="+ ageBand.getDisplayName()+"==");
			AgeGroup lowerAgeGroup = ageBand.getLowerLimitAgeGroup();
			checkNonExistentAgeGroup(
				connection, 
				ndPair,
				lowerAgeGroup);
			AgeGroup upperAgeGroup = ageBand.getUpperLimitAgeGroup();
			checkNonExistentAgeGroup(
				connection, 
				ndPair,
				upperAgeGroup);				
		}
	}
		
	private void checkNonExistentAgeGroup(
		final Connection connection,
		final NumeratorDenominatorPair ndPair,
		final AgeGroup ageGroup) 
		throws RIFServiceException {
			
		rifLogger.info(this.getClass(), "checkNonExistentAgeGroup name=="+ageGroup.getName()+"==");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			//Create query
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40");
			queryFormatter.addSelectField("fieldname");
			queryFormatter.addFromTable("rif40_age_groups");
			queryFormatter.addFromTable("rif40_tables");
			queryFormatter.addWhereJoinCondition(
				"rif40_tables", 
				"age_group_id", 
				"rif40_age_groups", 
				"age_group_id");
			queryFormatter.addWhereParameter(
				"rif40_tables", 
				"isnumerator");
			queryFormatter.addWhereParameter(
				"rif40_tables", 
				"table_name");
			
			queryFormatter.addWhereParameter(
				"rif40_age_groups", 
				"fieldname");		
			
			
			
			/*
			SQLRecordExistsQueryFormatter queryFormatter
				= new SQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setFromTable("rif40_age_groups");

			queryFormatter.setLookupKeyFieldName("age_group_id");
			queryFormatter.addWhereParameter("low_age");
			queryFormatter.addWhereParameter("high_age");
			*/
			
			String denominatorTableName
				= ndPair.getDenominatorTableName();
			sqlRIFContextManager.logSQLQuery(
				"checkNonExistentAgeGroup",
				queryFormatter,
				"0",
				denominatorTableName,
				ageGroup.getName());
							
			//Execute query and generate results
			statement 
				= sqlRIFContextManager.createPreparedStatement(
					connection, 
					queryFormatter);
			
			
			//yes, it is a denominator table
			statement.setInt(1, 0);
			statement.setString(2, denominatorTableName);
			statement.setString(3, ageGroup.getName());
			
			/*
			statement.setInt(1, id);
			statement.setInt(2, Integer.valueOf(ageGroup.getLowerLimit()));
			statement.setInt(3, Integer.valueOf(ageGroup.getUpperLimit()));
			*/
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				//ERROR: no such age group exists
				String recordType
					= ageGroup.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						ageGroup.getDisplayName());

				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_AGE_GROUP, 
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
			}
						
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			sqlRIFContextManager.logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					ageGroup.getRecordType(),
					ageGroup.getDisplayName());

			rifLogger.error(
				MSSQLAgeGenderYearManager.class, 
				errorMessage, 
				sqlException);										
					
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}			
	}
	
		
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
