package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.QueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractRIFConcept.ValidationPolicy;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.AgeGroupSortingOption;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class AgeGenderYearManager extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	/** The sql rif context manager. */
	private RIFContextManager sqlRIFContextManager;
	private final RIFServiceStartupOptions rifServiceStartupOptions;
	private final String tablesTableName;

	/**
	 * @param sqlRIFContextManager the sql rif context manager
	 * @param options the {@link RIFServiceStartupOptions}
	 */
	public AgeGenderYearManager(final RIFContextManager sqlRIFContextManager,
			final RIFServiceStartupOptions options) {

		super(options);
		this.sqlRIFContextManager = sqlRIFContextManager;
		this.rifServiceStartupOptions = options;
		tablesTableName = applySchemaPrefixIfNeeded("rif40_tables");
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
	public List<AgeGroup> getAgeGroups(final User user, final Connection connection,
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
		List<AgeGroup> results = new ArrayList<>();
		try {

			//Create query
			Integer ageGroupID;
			SelectQueryFormatter getAgeIDQueryFormatter = SelectQueryFormatter.getInstance(
					rifServiceStartupOptions.getRifDatabaseType());

			sqlRIFContextManager.configureQueryFormatterForDB(getAgeIDQueryFormatter);
			getAgeIDQueryFormatter.addSelectField("age_group_id");

			getAgeIDQueryFormatter.addFromTable(tablesTableName);
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
					= SERVICE_MESSAGES.getMessage(
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

			//Step II: Obtain the list of age groups that are appropriate
			//for the age group ID associated with the numerator table
			//The age group id helps group together age groups based on different
			//needs.  "1" may represent the standard breakdown of age ranges.
			//"2" may represent age ranges that are broken down every 4 years
			//After obtaining the list of age groups having the correct age group id
			//sort them by low_age
			SelectQueryFormatter getAgesForAgeGroupID = SelectQueryFormatter.getInstance(
					rifServiceStartupOptions.getRifDatabaseType());
			sqlRIFContextManager.configureQueryFormatterForDB(getAgesForAgeGroupID);

			getAgesForAgeGroupID.addSelectField("age_group_id");
			getAgesForAgeGroupID.addSelectField("low_age");
			getAgesForAgeGroupID.addSelectField("high_age");
			getAgesForAgeGroupID.addSelectField("fieldname");
			String ageGroupsTableName = applySchemaPrefixIfNeeded("rif40_age_groups");
			getAgesForAgeGroupID.addFromTable(ageGroupsTableName);
			getAgesForAgeGroupID.addWhereParameter("age_group_id");
		
			if ((sortingOrder == null) ||
				(sortingOrder == AgeGroupSortingOption.ASCENDING_LOWER_LIMIT)) {
				getAgesForAgeGroupID.addOrderByCondition(
						"low_age", QueryFormatter.SortOrder.ASCENDING);
			}
			else if (sortingOrder == AgeGroupSortingOption.DESCENDING_LOWER_LIMIT) {
				getAgesForAgeGroupID.addOrderByCondition(
						"low_age",
						QueryFormatter.SortOrder.DESCENDING);
			}
			else if (sortingOrder == AgeGroupSortingOption.ASCENDING_UPPER_LIMIT) {
				getAgesForAgeGroupID.addOrderByCondition(
						"high_age",
						QueryFormatter.SortOrder.ASCENDING);
			}
			else {
				//it must be descending lower limit.		
				getAgesForAgeGroupID.addOrderByCondition(
						"high_age",
						QueryFormatter.SortOrder.DESCENDING);
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
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= SERVICE_MESSAGES.getMessage("ageGroup.error.unableToGetAgeGroups");

			rifLogger.error(
				getClass(),
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(getAgeIDStatement);
			SQLQueryUtility.close(getAgeIDResultSet);
			SQLQueryUtility.close(getAgesForAgeGroupStatement);
			SQLQueryUtility.close(getAgesForAgeGroupResultSet);
		}
		return results;		
	}
	
	/**
	 * Gets the genders.
	 *
	 * @return the genders
	 */
	public ArrayList<Sex> getGenders() {
		
		ArrayList<Sex> results = new ArrayList<>();
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
			SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
					rifServiceStartupOptions.getRifDatabaseType());
			sqlRIFContextManager.configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("year_start");
			queryFormatter.addSelectField("year_stop");
			queryFormatter.addFromTable(tablesTableName);
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
					= SERVICE_MESSAGES.getMessage(
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
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"sqlAgeGenderYearManager.error.unableToGetStartEndYear",
					ndPair.getDisplayName());
			
			rifLogger.error(
				getClass(),
				errorMessage, 
				sqlException);
			
			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
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
	private void validateCommonMethodParameters(final User user, final Connection connection,
			final Geography geography, final NumeratorDenominatorPair ndPair)
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
			SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
					rifServiceStartupOptions.getRifDatabaseType());
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
			
			if (!resultSet.next()) {
				//ERROR: no such age group exists
				String recordType
					= ageGroup.getRecordType();
				String errorMessage
					= SERVICE_MESSAGES.getMessage(
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
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					ageGroup.getRecordType(),
					ageGroup.getDisplayName());

			rifLogger.error(
				getClass(),
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}			
	}
}
