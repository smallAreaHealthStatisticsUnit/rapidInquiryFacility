package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.CountTableRowsQueryFormatter;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class SmoothedResultManager extends BaseSQLManager {

	protected static final RIFLogger rifLogger = RIFLogger.getLogger();

	private ArrayList<String> allAttributeColumnNames;
	private Hashtable<String, String> columnDescriptionFromName;
	private HashSet<String> numericColumns;
	private HashSet<String> doublePrecisionColumns;

	public SmoothedResultManager(final RIFServiceStartupOptions options) {

		super(options);

		numericColumns = new HashSet<>();
		doublePrecisionColumns = new HashSet<>();

		//initialise the list of attributes columns that the clients can
		//use to construct queries
		allAttributeColumnNames = new ArrayList<>();
		columnDescriptionFromName = new Hashtable<>();

		//Below, we are trying to register all the possible fields we would see in the smoothed result file.
		//Note that the second parameter is a property name for tool tip help text that could help comment
		//the column names as they would appear in a web page display.
		registerColumnComment("area_id", "schemaComments.mapTable.areaID");
		registerColumnComment("band_id", "schemaComments.mapTable.areaID");
		registerColumnComment("genders", "schemaComments.mapTable.genders");
		registerColumnComment("observed", "schemaComments.mapTable.observed");
		registerColumnComment("expected", "schemaComments.mapTable.expected");
		registerColumnComment("adjusted", "schemaComments.mapTable.adjusted");

		//We don't need to include the following attributes because they will ALWAYS be in the results

		registerNumericColumn("gid", "schemaComments.mapTable.gid");
		//registerTextColumn("gid_rowindex", "schemaComments.mapTable.gidRowIndex");
		registerNumericColumn("inv_id", "schemaComments.mapTable.invID");
		registerNumericColumn("band_id", "schemaComments.mapTable.bandID");

		registerDoublePrecisionColumn("lower95", "schemaComments.mapTable.lower95");
		registerDoublePrecisionColumn("upper95", "schemaComments.mapTable.upper95");
		registerDoublePrecisionColumn("relative_risk", "schemaComments.mapTable.relativeRisk");
		registerDoublePrecisionColumn("smoothed_relative_risk", "schemaComments.mapTable.smoothedRelativeRisk");
		registerDoublePrecisionColumn("posterior_probability", "schemaComments.mapTable.posteriorProbability");
		registerDoublePrecisionColumn("posterior_probability_upper95", "schemaComments.mapTable.posteriorProbabilityUpper95");
		registerDoublePrecisionColumn("posterior_probability_lower95", "schemaComments.mapTable.posteriorProbabilityLower95");
		registerDoublePrecisionColumn("residual_relative_risk", "schemaComments.mapTable.residualRelativeRisk");
		registerDoublePrecisionColumn("residual_rr_lower95", "schemaComments.mapTable.residualRRLower95");
		registerDoublePrecisionColumn("residual_rr_upper95", "schemaComments.mapTable.residualRRUpper95");
		registerDoublePrecisionColumn("smoothed_smr", "schemaComments.mapTable.smoothedSMR");
		registerDoublePrecisionColumn("smoothed_smr_lower95", "schemaComments.mapTable.smoothedSMRLower95");
		registerDoublePrecisionColumn("smoothed_smr_upper95", "schemaComments.mapTable.smoothedSMRUpper95");
	}


	private void registerColumnComment(
			final String columnName,
			final String columnDescriptionPropertyName) {

		String message
				= RIFServiceMessages.getMessage(columnDescriptionPropertyName);
		columnDescriptionFromName.put(columnName, message);
	}

	private void registerDoublePrecisionColumn(
			final String columnName,
			final String columnDescriptionPropertyName) {

		registerNumericColumn(columnName, columnDescriptionPropertyName);
		doublePrecisionColumns.add(columnName);
	}

	private void registerNumericColumn(
			final String columnName,
			final String columnDescriptionPropertyName) {

		allAttributeColumnNames.add(columnName);
		numericColumns.add(columnName);

		String message
				= RIFServiceMessages.getMessage(columnDescriptionPropertyName);
		columnDescriptionFromName.put(columnName, message);
	}

	public ArrayList<Sex> getSexes(
			final Connection connection,
			final String studyID)
			throws RIFServiceException {

		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());

		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addSelectField("genders");
		queryFormatter.addFromTable("rif40_investigations");
		queryFormatter.addWhereParameter("study_id");

		logSQLQuery(
				"getSexes",
				queryFormatter,
				studyID);

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<Sex> results = new ArrayList<Sex>();
		try {

			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			resultSet.next();

			int sexID = resultSet.getInt(1);

			if (sexID == 3) {
				//BOTH
				results.add(Sex.getSexFromCode(1));
				results.add(Sex.getSexFromCode(2));
				results.add(Sex.getSexFromCode(3));
			} else {
				//M or F
				results.add(Sex.getSexFromCode(sexID));
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"smoothedResultsManager.error.unableToGetSexesForStudy",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return results;
	}

	ArrayList<Integer> getYears(
			final Connection connection,
			final String studyID)
			throws RIFServiceException {

		String extractTableName
				= deriveExtractTableName(studyID);
		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
		queryFormatter.setUseDistinct(true);
		queryFormatter.addSelectField("year");
		queryFormatter.addFromTable(extractTableName);
		queryFormatter.addOrderByCondition("year");

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<Integer> results = new ArrayList<Integer>();
		try {
			statement
					= connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				results.add(resultSet.getInt(1));
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"smoothedResultsManager.error.unableToGetYearsForStudy",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return results;
	}


	public String[] getColumnNameDescriptions(final String[] columnNames) {
		String[] results = new String[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			results[i] = columnDescriptionFromName.get(columnNames[i]);
		}

		return results;
	}


	// new geography method
	public String[] getGeographyAndLevelForStudy(
			final Connection connection,
			final String studyID)
			throws RIFServiceException {

		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addSelectField("geography");
		queryFormatter.addSelectField("study_geolevel_name");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String[] results = new String[2];
		try {
			connection.setAutoCommit(false);
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			resultSet.next();
			results[0] = resultSet.getString(1);
			results[1] = resultSet.getString(2);
		}
		catch(SQLException exception) {
			rifLogger.error(this.getClass(),
			                "SmoothedResultManager.getGeographyAndLevelForStudy error", exception);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		//		String[] results = new String[2];
		//		results[0] = "SAHSU";
		//	results[1] = "Level3";
		return results;
	}

	public RIFResultTable getSmoothedResults(
			final Connection connection,
			final String studyID,
			final Sex sex)
			throws RIFServiceException {

		return getSmoothedResultsForAttributes(
				connection,
				allAttributeColumnNames,
				studyID,
				sex);
	}

	public String[] getDetailsForProcessedStudy(
			final Connection connection,
			final String studyID)
			throws RIFServiceException {

		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		queryFormatter.addQueryLine(1, "SELECT");
		queryFormatter.addQueryLine(2, "a.username, a.study_name, a.description, a.study_date, a.geography, "
		                               + "a.study_type, a.comparison_geolevel_name, a.denom_tab, a.year_start, a.year_stop,  "
		                               + "a.max_age_group, a.min_age_group, a.study_geolevel_name, a.project, a.project_description, "
		                               + "a.stats_method, b.covariate_name, c.inv_name, c.genders, c.numer_tab");
		queryFormatter.addQueryLine(1, "FROM");
		queryFormatter.addQueryLine(2, "rif40.rif40_studies a left join rif40.rif40_inv_covariates b");
		queryFormatter.addQueryLine(2, "on a.study_id = b.study_id,");
		queryFormatter.addQueryLine(2, "rif40.rif40_investigations c");
		queryFormatter.addQueryLine(1, "WHERE");
		queryFormatter.addQueryLine(2, "a.study_id = c.study_id");
		queryFormatter.addQueryLine(1, "AND");
		queryFormatter.addQueryLine(2, "a.study_id = ?");

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String[] results = new String[20];
		try {
			connection.setAutoCommit(false);
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			resultSet.next();
			results[0] = resultSet.getString(1);
			results[1] = resultSet.getString(2);
			results[2] = resultSet.getString(3);
			results[3] = resultSet.getString(4);
			results[4] = resultSet.getString(5);
			results[5] = getStudyType(resultSet.getString(6));
			results[6] = resultSet.getString(7);
			results[7] = resultSet.getString(8);
			results[8] = resultSet.getString(9);
			results[9] = resultSet.getString(10);
			results[10] = resultSet.getString(11);
			results[11] = resultSet.getString(12);
			results[12] = resultSet.getString(13);
			results[13] = resultSet.getString(14);
			results[14] = resultSet.getString(15);
			results[15] = resultSet.getString(16);
			results[16] = resultSet.getString(17);
			results[17] = resultSet.getString(18);
			results[18] = getGender(resultSet.getString(19));
			results[19] = resultSet.getString(20);
		}
		catch(SQLException exception) {
			rifLogger.error(this.getClass(),
			                "SmoothedResultManager.getDetailsForProcessedStudy error", exception);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		return results;
	}

	private String getStudyType(final String studyType) {
		if (studyType.equals("1")) {
			return "Disease Mapping";
		} else {
			return "Risk Analysis";
		}
	}
	private String getGender(final String gender) {
		if (gender.equals("3")) {
			return "Both";
		} else if (gender.equals("2")){
			return "Female";
		} else {
			return "Male";
		}
	}

	public String[] getHealthCodesForProcessedStudy(
			final Connection connection,
			final String studyID)
			throws RIFServiceException {

		//Count the number of terms
		SQLGeneralQueryFormatter countTableRowsQueryFormatter = new SQLGeneralQueryFormatter();
		countTableRowsQueryFormatter.addQueryLine(0, "SELECT");
		countTableRowsQueryFormatter.addQueryLine(1, "COUNT(min_condition) AS total");
		countTableRowsQueryFormatter.addQueryLine(0, "FROM");
		countTableRowsQueryFormatter.addQueryLine(1, "rif40.rif40_inv_conditions");
		countTableRowsQueryFormatter.addQueryLine(1, "WHERE study_id = ?");

		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addSelectField("min_condition");
		queryFormatter.addSelectField("field_name");
		queryFormatter.addFromTable("rif40_inv_conditions");
		queryFormatter.addWhereParameter("study_id");

		PreparedStatement statement = null;
		PreparedStatement countTableRowsStatement = null;
		ResultSet resultSet = null;
		ResultSet totalRowCountResultSet = null;
		int numberOfRows = 0;
		String[] results = null;

		try {
			connection.setAutoCommit(false);

			countTableRowsStatement
					= connection.prepareStatement(countTableRowsQueryFormatter.generateQuery());
			countTableRowsStatement.setInt(1, Integer.valueOf(studyID));
			totalRowCountResultSet
					= countTableRowsStatement.executeQuery();
			totalRowCountResultSet.next();
			numberOfRows = totalRowCountResultSet.getInt(1);

			results = new String[numberOfRows];

			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));

			resultSet = statement.executeQuery();
			int currentRow = 0;
			while(resultSet.next()) {
				results[currentRow] = resultSet.getString("min_condition") + "-" + resultSet.getString("field_name");
				currentRow++;
			}
		}
		catch(SQLException exception) {
			rifLogger.error(this.getClass(),
			                "SmoothedResultManager.getHealthCodesForProcessedStudy error", exception);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(countTableRowsStatement);
			SQLQueryUtility.close(totalRowCountResultSet);
		}
		return results;
	}

	/* 
	 * Changes needed:
	 *
	 * Risk analysis version needs to return 1/row band not area as at present but the front end needs a re-write first
	 * Add area name 
	 * Convert SQL fetch to dynamic method 4 (i.e. works out the field names and datatypes automatically)
	 * Use a different list of fields for risk analysis and disease mapping and make easy to configure
	 */
	public RIFResultTable getStudyTableForProcessedStudy(
			final Connection connection,
			final String studyID,
			final String type,
			final String stt,
			final String stp)
			throws RIFServiceException {

		String tableName = "";
		if (type.equals("extract")) {
			tableName = deriveExtractTableName(studyID);
		} else {
			tableName = deriveMapTableName(studyID);
		}

		//count total rows in table
		SQLGeneralQueryFormatter countTableRowsQueryFormatter = new SQLGeneralQueryFormatter();
		boolean isDiseaseMappingStudy = isDiseaseMapping(connection, studyID);
		
		if (isDiseaseMappingStudy) {		
			countTableRowsQueryFormatter.addQueryLine(0, "SELECT");
			countTableRowsQueryFormatter.addQueryLine(1, "COUNT(area_id) AS total");
			countTableRowsQueryFormatter.addQueryLine(0, "FROM");
			countTableRowsQueryFormatter.addQueryLine(1, tableName);
		}
		else {
			countTableRowsQueryFormatter.addQueryLine(0, "SELECT");
			countTableRowsQueryFormatter.addQueryLine(1, "COUNT(band_id) AS total");
			countTableRowsQueryFormatter.addQueryLine(0, "FROM");
			countTableRowsQueryFormatter.addQueryLine(1, tableName);
		}			

		//get requested subset
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		if (isDiseaseMappingStudy) {		
			queryFormatter.addQueryLine(0, "SELECT * FROM");
			queryFormatter.addQueryLine(1, "(select row_number() over(ORDER BY area_id ASC) as row_number, * ");
			queryFormatter.addQueryLine(1, "from " + tableName + ") a ");
			queryFormatter.addQueryLine(1, "where row_number >= ? and row_number <= ?");
		}
		else {
			queryFormatter.addQueryLine(0, "SELECT * FROM");
			queryFormatter.addQueryLine(1, "(select row_number() over(ORDER BY band_id ASC) as row_number, * ");
			queryFormatter.addQueryLine(1, "from " + tableName + ") a ");
			queryFormatter.addQueryLine(1, "where row_number >= ? and row_number <= ?");
		}
		
		logSQLQuery(
				"getNumberOfResultsForMapDataSet",
				queryFormatter,
				studyID);
				
		PreparedStatement mainResultsStatement = null;
		PreparedStatement resultCounterStatement = null;
		ResultSet mainResultSet = null;
		ResultSet resultCounterSet = null;

		RIFResultTable results = new RIFResultTable();

		try {
			resultCounterStatement = connection.prepareStatement(countTableRowsQueryFormatter.generateQuery());
			resultCounterSet = resultCounterStatement.executeQuery();
			resultCounterSet.next();
			int totalNumberRowsInTable = resultCounterSet.getInt(1);

			Integer startRow = Integer.parseInt(stt);
			Integer stopRow = Integer.parseInt(stp);

			//If request max is greater than total do not use upper
			if (totalNumberRowsInTable < stopRow) {
				stopRow = totalNumberRowsInTable;
			}

			//get expected row count
			Integer totalNumberRowsInResults = stopRow - startRow + 1;

			//Now get the results
			mainResultsStatement = connection.prepareStatement(queryFormatter.generateQuery());
			mainResultsStatement.setInt(1, startRow);
			mainResultsStatement.setInt(2, stopRow);

			mainResultSet = mainResultsStatement.executeQuery();
			mainResultSet.next();
			ResultSetMetaData rsmd = mainResultSet.getMetaData();
			Integer totalNumberColumnsInResults = rsmd.getColumnCount();
			
			String[] columnNames = new String[totalNumberColumnsInResults];
			RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[totalNumberColumnsInResults];
			String[][] data = new String[totalNumberRowsInResults][totalNumberColumnsInResults];
			int ithRow = 0;
				
			// The column count starts from 1
			for (int i = 1; i <= totalNumberColumnsInResults; i++ ) {
				columnNames[i-1] = rsmd.getColumnName(i);
				String columnType = rsmd.getColumnTypeName(i);
				
				if (columnType.equals("integer") || 
					columnType.equals("bigint") || 
					columnType.equals("int4") ||
					columnType.equals("int") ||
					columnType.equals("smallint")) {	
					columnDataTypes[i-1] = RIFResultTable.ColumnDataType.NUMERIC;
				}				
				else if (columnType.equals("float") || 
						 columnType.equals("float8") || 
						 columnType.equals("double precision") ||
						 columnType.equals("numeric")) {
					columnDataTypes[i-1] = RIFResultTable.ColumnDataType.NUMERIC;
				}
				else {
					columnDataTypes[i-1] = RIFResultTable.ColumnDataType.TEXT;
				}		 
			}
					
			do {
				for (int j = 0; j < totalNumberColumnsInResults; j++) {
					data[ithRow][j] = mainResultSet.getString(j + 1);
				}
				ithRow++;
			} while (mainResultSet.next());

			results.setColumnProperties(columnNames, columnDataTypes);
			results.setData(data);

			return results;
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlSmoothedResultsManager.error.unableToRetrieveStudyTableForProcessedStudy",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
		finally {
			SQLQueryUtility.close(mainResultsStatement);
			SQLQueryUtility.close(mainResultSet);
			SQLQueryUtility.close(resultCounterStatement);
			SQLQueryUtility.close(resultCounterSet);
		}
	}

	public RIFResultTable getSmoothedResultsForAttributes(
			final Connection connection,
			final ArrayList<String> smoothedAttributesToInclude,
			final String studyID,
			final Sex sex)
			throws RIFServiceException {

		String extractTableName = deriveExtractTableName(studyID);
		String mapTableName	= deriveMapTableName(studyID);

		/*
		 * Create the statement to obtain the total number of rows we should
		 * expect to get back when we fetch the entire table.  using a
		 * SELECT
		 *    COUNT(study_id)
		 * FROM
		 *    rif_studies.s[study_id]_map
		 *
		 * This is being used as an alternative to finding out the number of rows by using
		 *
		 * resultSet.last()
		 * resultSet.getRow()
		 *
		 * resultSet.beforeFirst()
		 * while (resultSet.next()) {
		 *    ... do stuff ...
		 * }
		 */

		SQLGeneralQueryFormatter countTableRowsQueryFormatter = new SQLGeneralQueryFormatter();
		//otherwise, if it's both, don't filter by any sex value
		boolean isDiseaseMappingStudy = isDiseaseMapping(connection, studyID);
		if (isDiseaseMappingStudy) {
			countTableRowsQueryFormatter.addQueryLine(0, "SELECT");
			countTableRowsQueryFormatter.addQueryLine(1, "COUNT(DISTINCT area_id) /* Disease Mapping */ AS total");
			countTableRowsQueryFormatter.addQueryLine(0, "FROM");
			countTableRowsQueryFormatter.addQueryLine(1, mapTableName);
			countTableRowsQueryFormatter.padAndFinishLine();
		}
		else {
			countTableRowsQueryFormatter.addQueryLine(1, "(SELECT COUNT(DISTINCT area_id) AS total");
			countTableRowsQueryFormatter.addQueryLine(1, "   FROM rif40.rif40_study_areas");
			countTableRowsQueryFormatter.addQueryLine(1, "  WHERE study_id = ?)");
			countTableRowsQueryFormatter.padAndFinishLine();
		}

		/*
		 * Create the SQL query to return all of the fields of interest
		 */

		//Aggregate population totals for each area based on sex.  If sex is 'BOTH', aggregate male and female
		//totals together.
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		if (isDiseaseMappingStudy) { 
			queryFormatter.addQueryLine(0, "WITH population_per_area AS ");
			queryFormatter.addQueryLine(1, "(SELECT area_id, SUM(total_pop) AS population");
		}
		else {
			queryFormatter.addQueryLine(0, "WITH population_per_band AS ");
			queryFormatter.addQueryLine(1, "(SELECT band_id, SUM(total_pop) AS population");
		}
		queryFormatter.addQueryLine(1, "FROM");
		queryFormatter.addQueryLine(2, extractTableName);
		queryFormatter.addQueryLine(2, " WHERE study_or_comparison = 'S'");
		if (sex == Sex.MALES) {
			queryFormatter.addQueryLine(2, "  AND sex = 1 ");
		}
		else if (sex == Sex.FEMALES) {
			queryFormatter.addQueryLine(2, "  AND sex = 2 ");
		}
		//otherwise, if it's both, don't filter by any sex value
		if (isDiseaseMappingStudy) { 
			queryFormatter.addQueryLine(1, "GROUP BY area_id)");
		}
		else {
			queryFormatter.addQueryLine(1, "GROUP BY band_id)");
		}
		
		if (!isDiseaseMappingStudy) { // Risk analysis - add study_areas CTE
			queryFormatter.addQueryLine(0, ", study_areas AS ");
			queryFormatter.addQueryLine(1, "(SELECT area_id, band_id, row_number() OVER(ORDER BY area_id ASC) AS gid");
			queryFormatter.addQueryLine(1, "   FROM rif40.rif40_study_areas");
			queryFormatter.addQueryLine(1, "  WHERE study_id = ?)");
		}
		
		//now generate the main SELECT statement
		String mapTableNameAlias = "a";
		queryFormatter.addQueryLine(0, "SELECT");

		if (isDiseaseMappingStudy) { 
			addSelectSmoothedFieldEntry(
					queryFormatter,
					1,
					"population_per_area",
					"area_id",
					false);
		}
		else { 
			addSelectSmoothedFieldEntry(
					queryFormatter,
					1,
					"study_areas",
					"area_id",
					false);
		}

		addSelectSmoothedFieldEntry(
				queryFormatter,
				1,
				mapTableNameAlias,
				"band_id",
				true);

		addSelectSmoothedFieldEntry(
				queryFormatter,
				1,
				mapTableNameAlias,
				"genders",
				true);

		addSelectSmoothedFieldEntry(
				queryFormatter,
				1,
				mapTableNameAlias,
				"observed",
				true);

		addSelectSmoothedFieldEntry(
				queryFormatter,
				1,
				mapTableNameAlias,
				"expected",
				true);

		if (isDiseaseMappingStudy) { 
			addSelectSmoothedFieldEntry(
					queryFormatter,
					1,
					"population_per_area",
					"population",
					true);
		}
		else {
			addSelectSmoothedFieldEntry(
					queryFormatter,
					1,
					"population_per_band",
					"population",
					true);
		}


		addSelectSmoothedFieldEntry(
				queryFormatter,
				1,
				mapTableNameAlias,
				"adjusted",
				true);

		for (String attribute : smoothedAttributesToInclude) {
			
			if (!isDiseaseMappingStudy && attribute.equals("gid")) { 
				addSelectSmoothedFieldEntry(
						queryFormatter,
						1,
						"study_areas",
						attribute,
						true);
			}
			else {
				addSelectSmoothedFieldEntry(
						queryFormatter,
						1,
						mapTableNameAlias,
						attribute,
						true);
			}
		}
		queryFormatter.finishLine();
		
		if (isDiseaseMappingStudy) { 
			queryFormatter.addQueryLine(0, "FROM " + mapTableName + " AS " + mapTableNameAlias + 
				", population_per_area");
		}
		else {
			queryFormatter.addQueryLine(0, "FROM " + mapTableName + " AS " + mapTableNameAlias + 
				", population_per_band, study_areas");
		}
		
		queryFormatter.addQueryLine(0, "WHERE");
		queryFormatter.addQueryPhrase(1, mapTableNameAlias);
		if (isDiseaseMappingStudy) { 
			queryFormatter.addQueryPhrase(".area_id = population_per_area.area_id AND ");
		}
		else {
			queryFormatter.addQueryPhrase(".band_id = population_per_band.band_id AND "); 
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(1, mapTableNameAlias);
			queryFormatter.addQueryPhrase(".band_id = study_areas.band_id AND ");
		}
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, mapTableNameAlias);
		queryFormatter.addQueryPhrase(".genders=?");
		queryFormatter.padAndFinishLine();

		//add in join condition related to genders
		logSQLQuery(
				"getNumberOfResultsForMapDataSet",
				countTableRowsQueryFormatter,
				studyID);

/* Disease mapping version:

WITH population_per_area AS 
   (SELECT
      area_id,
      SUM(total_pop) AS population
   FROM
      rif_studies.s136_extract
       WHERE study_or_comparison = 'S'
   GROUP BY
      area_id)
SELECT
   population_per_area.area_id,
   a.band_id,
   a.genders,
   a.observed,
   a.expected,
   population_per_area.population,
   a.adjusted,
   a.gid,
   a.inv_id,
   a.band_id,
   ROUND(a.lower95, 3) AS lower95,
   ROUND(a.upper95, 3) AS upper95,
   ROUND(a.relative_risk, 3) AS relative_risk,
   ROUND(a.smoothed_relative_risk, 3) AS smoothed_relative_risk,
   ROUND(a.posterior_probability, 3) AS posterior_probability,
   ROUND(a.posterior_probability_upper95, 3) AS posterior_probability_upper95,
   ROUND(a.posterior_probability_lower95, 3) AS posterior_probability_lower95,
   ROUND(a.residual_relative_risk, 3) AS residual_relative_risk,
   ROUND(a.residual_rr_lower95, 3) AS residual_rr_lower95,
   ROUND(a.residual_rr_upper95, 3) AS residual_rr_upper95,
   ROUND(a.smoothed_smr, 3) AS smoothed_smr,
   ROUND(a.smoothed_smr_lower95, 3) AS smoothed_smr_lower95,
   ROUND(a.smoothed_smr_upper95, 3) AS smoothed_smr_upper95
FROM
   rif_studies.s136_map as a,
   population_per_area
WHERE
   a.area_id = population_per_area.area_id AND  
   a.genders=? 
   
Risk Analysis version:

WITH population_per_band AS 
   (SELECT band_id, SUM(total_pop) AS population
   FROM
      rif_studies.s136_extract
       WHERE study_or_comparison = 'S'
   GROUP BY band_id)
, study_areas AS 
   (SELECT area_id, band_id, row_number() OVER(ORDER BY area_id ASC) AS gid
      FROM rif40.rif40_study_areas
     WHERE study_id = ?)
SELECT
   study_areas.area_id,
   a.band_id,
   a.genders,
   a.observed,
   a.expected,
   population_per_band.population,
   a.adjusted,
   study_areas.gid,
   a.inv_id,
   a.band_id,
   ROUND(a.lower95, 3) AS lower95,
   ROUND(a.upper95, 3) AS upper95,
   ROUND(a.relative_risk, 3) AS relative_risk,
   ROUND(a.smoothed_relative_risk, 3) AS smoothed_relative_risk,
   ROUND(a.posterior_probability, 3) AS posterior_probability,
   ROUND(a.posterior_probability_upper95, 3) AS posterior_probability_upper95,
   ROUND(a.posterior_probability_lower95, 3) AS posterior_probability_lower95,
   ROUND(a.residual_relative_risk, 3) AS residual_relative_risk,
   ROUND(a.residual_rr_lower95, 3) AS residual_rr_lower95,
   ROUND(a.residual_rr_upper95, 3) AS residual_rr_upper95,
   ROUND(a.smoothed_smr, 3) AS smoothed_smr,
   ROUND(a.smoothed_smr_lower95, 3) AS smoothed_smr_lower95,
   ROUND(a.smoothed_smr_upper95, 3) AS smoothed_smr_upper95
FROM rif_studies.s136_map AS a, population_per_band, study_areas
WHERE
   a.band_id = population_per_band.band_id AND  
   a.band_id = study_areas.band_id AND  
   a.genders=?;
 */   
		logSQLQuery(
				"retrieveResultsForMapDataSet",
				queryFormatter,
				studyID);

		PreparedStatement countTableRowsStatement = null;
		PreparedStatement retrieveDataStatement = null;
		ResultSet totalRowCountResultSet = null;
		ResultSet smoothedResultSet = null;
		int numberOfRows = 0;
		try {
			countTableRowsStatement
					= connection.prepareStatement(countTableRowsQueryFormatter.generateQuery());
			if (!isDiseaseMappingStudy) { 
				countTableRowsStatement.setInt(1, Integer.valueOf(studyID));
			}
			totalRowCountResultSet
					= countTableRowsStatement.executeQuery();
			totalRowCountResultSet.next();
			numberOfRows = totalRowCountResultSet.getInt(1);

			retrieveDataStatement
					= connection.prepareStatement(queryFormatter.generateQuery());

			if (isDiseaseMappingStudy) { 
				if (sex == Sex.MALES) {
					retrieveDataStatement.setInt(1, 1);
				}
				else if (sex == Sex.FEMALES) {
					retrieveDataStatement.setInt(1, 2);
				}
				else {
					retrieveDataStatement.setInt(1, 3);
				}
			}
			else { 
				retrieveDataStatement.setInt(1, Integer.valueOf(studyID));
				if (sex == Sex.MALES) {
					retrieveDataStatement.setInt(2, 1);
				}
				else if (sex == Sex.FEMALES) {
					retrieveDataStatement.setInt(2, 2);
				}
				else {
					retrieveDataStatement.setInt(2, 3);
				}
			}

			smoothedResultSet = retrieveDataStatement.executeQuery();


			/*
			 * Format should look like:
			 * area_id,
			 * band_id,
			 * genders,
			 * observed,
			 * expected,
			 * population
			 * adjusted,
			 *
			 * + n
			 */

			int numberOfColumns = smoothedAttributesToInclude.size();
			String[][] data = new String[numberOfRows][7 + numberOfColumns];

			//Build up results table
			int currentRow = 0;
			while(smoothedResultSet.next()) {
				//grab data that will be used to set values in columns which will definitely appear in the extract
				data[currentRow][0] = smoothedResultSet.getString("area_id");
				data[currentRow][1] = smoothedResultSet.getString("band_id");
				data[currentRow][2] = smoothedResultSet.getString("genders");
				data[currentRow][3] = smoothedResultSet.getString("observed");
				data[currentRow][4] = smoothedResultSet.getString("expected");
				data[currentRow][5] = smoothedResultSet.getString("population");
				data[currentRow][6] = smoothedResultSet.getString("adjusted");

				//Add in the optional fields that end users will have selected in the front end UI
				int currentColumn = 7;
				for (String smoothedAttributeToInclude : smoothedAttributesToInclude) {
					data[currentRow][currentColumn]
							= smoothedResultSet.getString(smoothedAttributeToInclude);
					currentColumn++;
				}

				currentRow++;
			}

			//Stuff everything the client will have to know about the results and send it back
			RIFResultTable rifResultTable = new RIFResultTable();
			int numberOfSmoothedAttributesToInclude = smoothedAttributesToInclude.size();
			String[] columnNames = new String[7 + numberOfSmoothedAttributesToInclude];
			columnNames[0] = "area_id";
			columnNames[1] = "band_id";
			columnNames[2] = "genders";
			columnNames[3] = "observed";
			columnNames[4] = "expected";
			columnNames[5] = "population";
			columnNames[6] = "adjusted";
			for (int i = 0; i < numberOfSmoothedAttributesToInclude; i++) {
				columnNames[7 + i] = smoothedAttributesToInclude.get(i);
			}

			String[] columnNameDescriptions = getColumnNameDescriptions(columnNames);

			RIFResultTable.ColumnDataType[] columnDataTypes = deriveColumnDataTypes(columnNames);
			rifResultTable.setColumnProperties(columnNames, columnNameDescriptions, columnDataTypes);
			rifResultTable.setData(data);
			return rifResultTable;

		}
		catch(SQLException sqlException) {
			rifLogger.error(this.getClass(),
			                "SmoothedResultManager.getSmoothedResultsForAttributes error", sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"SmoothedResultsManager.error.unableToGetSmoothedResults",
					studyID);
			RIFServiceException rifServiceException
					= new RIFServiceException(RIFServiceError.DATABASE_QUERY_FAILED, errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(totalRowCountResultSet);
			SQLQueryUtility.close(smoothedResultSet);
			SQLQueryUtility.close(countTableRowsStatement);
			SQLQueryUtility.close(retrieveDataStatement);
		}

	}

	private boolean isDiseaseMapping(final Connection connection, final String studyID)
			throws RIFServiceException {
				
		boolean rval=true;
		ResultSet isDiseaseMappingResultSet = null;
		PreparedStatement isDiseaseMappingStatement = null;
		try {
			SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
			
			queryFormatter.setDatabaseSchemaName("rif40");
			queryFormatter.addSelectField("study_type");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");

			logSQLQuery(
					"isDiseaseMapping",
					queryFormatter,
					studyID);
				
			isDiseaseMappingStatement
					= connection.prepareStatement(queryFormatter.generateQuery());
			isDiseaseMappingStatement.setInt(1, Integer.valueOf(studyID));
			isDiseaseMappingResultSet = isDiseaseMappingStatement.executeQuery();
			isDiseaseMappingResultSet.next();
			Integer studyType = isDiseaseMappingResultSet.getInt(1);
			// 1 - disease mapping, 11 - Risk Analysis (many areas, one band), 12 - Risk Analysis (point sources), 
			// 13 - Risk Analysis (exposure covariates), 14 - Risk Analysis (coverage shapefile), 
			// 15 - Risk Analysis (exposure shapefile)
			if (studyType == 1) {
				rval=true;
			}
			else if (studyType >= 11 || studyType <= 15) {
				rval=false;
			}
			else {
				throw new Exception("Invalid stypeType: " + studyType);
			}				
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),
			                "SmoothedResultManager.isDiseaseMapping error", exception);
			logException(exception);				
			String errorMessage
					= RIFServiceMessages.getMessage(
					"SmoothedResultsManager.error.unableToGetSmoothedResults",
					studyID);
			RIFServiceException rifServiceException
					= new RIFServiceException(RIFServiceError.DATABASE_QUERY_FAILED, errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(isDiseaseMappingResultSet);	
			SQLQueryUtility.close(isDiseaseMappingStatement);	
			return rval;
		}
	}
	
	private void addSelectSmoothedFieldEntry(
			final SQLGeneralQueryFormatter queryFormatter,
			final int indentationLevel,
			final String tableName,
			final String columnName,
			final boolean startsWithComma) {

		//KLG: round appropriately if the field is double precision
		//for now we'll conserve whatever digits of precision that the R program
		//produces.  It might be useful for exporting a data set to a zip file that
		//will be used by other stats programs.  But for retrieving data for the web
		//application, there is no need for a lot of the precision because (1)
		//the data technically shouldn't be reported with all those trailing values anyway
		//and (2) in the data viewer, the users explore the data through visual inspection
		//and not through client-side statistical functions that can do additioanl analysis
		//work.

		//KLG: @TODO The most useful way of rounding is to round to the correct number
		//of significant digits, not decimal places.  But doing this requires developing
		//special code that may not be portable across SQL Server and PostgreSQL.  Moreover,
		//for visual inspection purposes, I don't think users are going to care.  If they want
		//to preserve a consistent number of significant digits in some kind of post-analysis,
		//they should analyse the data using the zip file of CSV results and some stats program.

		if (startsWithComma) {
			queryFormatter.addQueryPhrase(",");
			queryFormatter.finishLine();
		}

		if (doublePrecisionColumns.contains(columnName)) {

			final StringBuilder sqlRoundingFragment = new StringBuilder();
			sqlRoundingFragment.append("ROUND(").append(tableName).append(".").append(columnName);

			// Need a special cast for Postgres, because the round function is not overloaded
			// with a double-precision first parameter. See
			// https://stackoverflow.com/questions/13113096/how-to-round-an-average-to-2-decimal-places-in-postgresql#13113623
			if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) {
				sqlRoundingFragment.append("::numeric");
			}
			sqlRoundingFragment.append( ", 3) AS ").append(columnName);
			queryFormatter.addQueryPhrase(indentationLevel, sqlRoundingFragment.toString());
		}
		else {
			queryFormatter.addQueryPhrase(indentationLevel, tableName + "." + columnName);
		}
	}


	private RIFResultTable.ColumnDataType[] deriveColumnDataTypes(
			final String[] columnNames) {

		RIFResultTable.ColumnDataType[] results
				= new RIFResultTable.ColumnDataType[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			if (numericColumns.contains(columnNames[i])) {
				results[i] = RIFResultTable.ColumnDataType.NUMERIC;
			}
			else {
				results[i] = RIFResultTable.ColumnDataType.TEXT;
			}
		}

		return results;
	}


	public RIFResultTable getPopulationPyramidData(
			final Connection connection,
			final String studyID,
			final Integer year)
			throws RIFServiceException {

		String extractTableName = deriveExtractTableName(studyID);

		CountTableRowsQueryFormatter getPyramidResultsCounter = new CountTableRowsQueryFormatter();
		getPyramidResultsCounter.setCountFieldName("age_group", true);
		getPyramidResultsCounter.setTableName(extractTableName);


		SQLGeneralQueryFormatter getPopulationPyramidData = new SQLGeneralQueryFormatter();
		getPopulationPyramidData.addQueryLine(0, "WITH males AS");
		getPopulationPyramidData.addQueryLine(1, "(SELECT");
		getPopulationPyramidData.addQueryLine(2, "age_group,");
		getPopulationPyramidData.addQueryLine(2, "SUM(total_pop) AS total_population");
		getPopulationPyramidData.addQueryLine(1, "FROM");
		getPopulationPyramidData.addQueryLine(2, extractTableName);
		getPopulationPyramidData.addQueryLine(1, "WHERE");
		getPopulationPyramidData.addQueryLine(2, "sex = 1 AND ");
		getPopulationPyramidData.addQueryLine(2, "year = ?");
		getPopulationPyramidData.addQueryLine(1, "GROUP BY");
		getPopulationPyramidData.addQueryLine(2, "age_group),");

		getPopulationPyramidData.addQueryLine(0, "females AS");
		getPopulationPyramidData.addQueryLine(1, "(SELECT");
		getPopulationPyramidData.addQueryLine(2, "age_group,");
		getPopulationPyramidData.addQueryLine(2, "SUM(total_pop) AS total_population");
		getPopulationPyramidData.addQueryLine(1, "FROM");
		getPopulationPyramidData.addQueryLine(2, extractTableName);
		getPopulationPyramidData.addQueryLine(1, "WHERE");
		getPopulationPyramidData.addQueryLine(2, "sex = 2 AND ");
		getPopulationPyramidData.addQueryLine(2, "year = ?");
		getPopulationPyramidData.addQueryLine(1, "GROUP BY");
		getPopulationPyramidData.addQueryLine(2, "age_group)");

		getPopulationPyramidData.addQueryLine(0, "SELECT");
		getPopulationPyramidData.addQueryLine(1, "rif40.rif40_age_groups.fieldname AS population_label,");
		getPopulationPyramidData.addQueryLine(1, "males.total_population AS males,");
		getPopulationPyramidData.addQueryLine(1, "females.total_population AS females");
		getPopulationPyramidData.addQueryLine(0, "FROM");
		getPopulationPyramidData.addQueryLine(1, "males,");
		getPopulationPyramidData.addQueryLine(1, "females,");
		getPopulationPyramidData.addQueryLine(1, "rif40.rif40_age_groups");
		getPopulationPyramidData.addQueryLine(0, "WHERE");

		getPopulationPyramidData.addQueryLine(1, "males.age_group = females.age_group AND");
		getPopulationPyramidData.addQueryLine(1, "rif40.rif40_age_groups.age_group_id = ? AND");
		getPopulationPyramidData.addQueryLine(1, "males.age_group = rif40.rif40_age_groups.offset");
		getPopulationPyramidData.addQueryLine(0, "ORDER BY");
		getPopulationPyramidData.addQueryLine(1, "rif40.rif40_age_groups.offset");


		PreparedStatement resultCounterStatement = null;
		PreparedStatement mainResultsStatement = null;

		ResultSet resultCounterSet = null;
		ResultSet mainResultSet = null;

		RIFResultTable results = new RIFResultTable();

		try {

			//Count the number of results first
			resultCounterStatement = connection.prepareStatement(getPyramidResultsCounter.generateQuery());
			resultCounterSet = resultCounterStatement.executeQuery();
			resultCounterSet.next();
			int totalNumberRowsInResults = resultCounterSet.getInt(1);

			//Now get the results
			mainResultsStatement = connection.prepareStatement(getPopulationPyramidData.generateQuery());
			mainResultsStatement.setInt(1, year);
			mainResultsStatement.setInt(2, year);
			//"1" is associated with the group of age group identifiers that are typically used for dividing ages into
			//different categories
			mainResultsStatement.setInt(3, 1);

			String[] columnNames = new String[3];
			columnNames[0] = "population_label";
			columnNames[1] = "males";
			columnNames[2] = "females";

			RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[3];
			columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[1] = RIFResultTable.ColumnDataType.NUMERIC;
			columnDataTypes[2] = RIFResultTable.ColumnDataType.NUMERIC;

			String[][] data = new String[totalNumberRowsInResults][3];
			int ithRow = 0;

			mainResultSet = mainResultsStatement.executeQuery();
			while (mainResultSet.next()) {
				data[ithRow][0] = mainResultSet.getString(1);
				data[ithRow][1] = mainResultSet.getString(2);
				data[ithRow][2] = mainResultSet.getString(3);
				ithRow++;
			}

			results.setColumnProperties(columnNames, columnDataTypes);
			results.setData(data);

			return results;
		} catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlSmoothedResultManager.error.unableToRetrievePopulationPyramidData",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			SQLQueryUtility.close(resultCounterStatement);
			SQLQueryUtility.close(resultCounterSet);

			SQLQueryUtility.close(mainResultsStatement);
			SQLQueryUtility.close(mainResultSet);
		}
	}

	private String deriveMapTableName(final String studyID) {

		return "rif_studies."
		       + "s"
		       + studyID
		       + "_map";
	}

	private String deriveExtractTableName(final String studyID) {

		return "rif_studies."
		       + "s"
		       + studyID
		       + "_extract";
	}
}
