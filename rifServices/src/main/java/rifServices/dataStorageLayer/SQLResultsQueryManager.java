package rifServices.dataStorageLayer;

import rifGenericLibrary.dataStorageLayer.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifServices.businessConceptLayer.*;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFDatabaseProperties;
import rifServices.util.FieldValidationUtility;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Public methods assume that parameter values are non-null and do not present
 * security concerns.  Error checks focus on the following kinds of errors:
 * <ul>
 * <li>
 * check whether String values conform to regular expressions expected in the
 * database
 * </li>
 * <li>
 * check that parameter objects instantiated from business classes do not have
 * field-level errors or errors caused by combinations of errors
 * </li>
 * <li>
 * check that combinations of parameter values do not exhibit errors
 * </li>
 * </ul>
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

final class SQLResultsQueryManager extends AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private SQLRIFContextManager sqlRIFContextManager;
	private SQLMapDataManager sqlMapDataManager;
	private SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public SQLResultsQueryManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final SQLRIFContextManager sqlRIFContextManager,
		final SQLMapDataManager sqlMapDataManager,
		final SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager) {
		
		super(rifDatabaseProperties);
		this.sqlRIFContextManager = sqlRIFContextManager;
		this.sqlMapDataManager = sqlMapDataManager;
		this.sqlDiseaseMappingStudyManager = sqlDiseaseMappingStudyManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public BoundaryRectangle getGeoLevelBoundsForArea(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea)
		throws RIFServiceException {
	
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		mapArea.checkErrors();
		checkMapAreaExists(
			connection, 
			studyResultRetrievalContext,
			mapArea);
		checkMapAreaExistsInStudy(
			connection, 
			studyResultRetrievalContext,
			mapArea);
		
		//Create query
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getgeolevelboundsforarea");
		queryFormatter.setNumberOfFunctionParameters(3);
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyResultRetrievalContext.getGeographyName());
			statement.setString(2, studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(3, mapArea.getIdentifier());
			resultSet = statement.executeQuery();

			/*
			 * Expecting a result with four columns:
			 * yMax  |   xMax   |  yMax  |  yMin
			 * 
			 * Assumes at least one result returned because
			 * SQL function call will throw an exception if
			 * no results are returned
			 */
			//Assumes at least one result, because function will
			//
			resultSet.next();
			
			double yMax = resultSet.getDouble(1);
			double xMax = resultSet.getDouble(2);
			double yMin = resultSet.getDouble(3);			
			double xMin = resultSet.getDouble(4);
			
			BoundaryRectangle result
				= BoundaryRectangle.newInstance(
					String.valueOf(xMin),
					String.valueOf(yMin),
					String.valueOf(xMax),
					String.valueOf(yMax));
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetBoundsForArea",
					mapArea.getDisplayName(),
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}	

	public BoundaryRectangle getGeoLevelFullExtentForStudy(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext)
		throws RIFServiceException {
	
		//Validate parameters
		user.checkErrors();
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		
		//Create query
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getgeolevelfullextentforstudy");
		queryFormatter.setNumberOfFunctionParameters(3);

		logSQLQuery(
			"checkNumeratorTableExists",
			queryFormatter,
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName(),
			studyResultRetrievalContext.getStudyID());
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());			
			statement.setString(1, studyResultRetrievalContext.getGeographyName());
			statement.setString(2, studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setInt(3, Integer.valueOf(studyResultRetrievalContext.getStudyID()));
			
			resultSet = statement.executeQuery();
			
			//Assume there is at least one result because
			//SQL function will generate an exception if 
			//there are no results
			resultSet.next();

			double yMax = resultSet.getDouble(1);
			double xMax = resultSet.getDouble(2);
			double yMin = resultSet.getDouble(3);			
			double xMin = resultSet.getDouble(4);
			
			BoundaryRectangle result
				= BoundaryRectangle.newInstance(
					String.valueOf(xMin),
					String.valueOf(yMin),
					String.valueOf(xMax),
					String.valueOf(yMax));
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName 
				= getStudyName(
					connection, 
					studyResultRetrievalContext.getStudyID());
			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetBoundsForStudy",
					studyName,
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
	
	public BoundaryRectangle getGeoLevelFullExtent(
		final Connection connection,
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {
		
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			geography,
			geoLevelSelect);		
					
		//Create query
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getgeolevelfullextent");
		queryFormatter.setNumberOfFunctionParameters(2);		
		
		logSQLQuery(
			"getGeoLevelFullExtent",
			queryFormatter,
			geography.getName(),
			geoLevelSelect.getName());
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, geography.getName());
			statement.setString(2, geoLevelSelect.getName());				
			resultSet = statement.executeQuery();
			
			//Assume there is at least one row result
			resultSet.next();
			
			double yMax = resultSet.getDouble(1);
			double xMax = resultSet.getDouble(2);
			double yMin = resultSet.getDouble(3);			
			double xMin = resultSet.getDouble(4);
			
			BoundaryRectangle result
				= BoundaryRectangle.newInstance(
					String.valueOf(xMin),
					String.valueOf(yMin),
					String.valueOf(xMax),
					String.valueOf(yMax));
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetBoundsForGeoLevel",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}

	public String getTiles(
		final Connection connection,
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final String tileIdentifier,
		final Integer zoomFactor,		
		final BoundaryRectangle boundaryRectangle) 
		throws RIFServiceException {

		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			geography,
			geoLevelSelect);		
		boundaryRectangle.checkErrors();


		//check cache
		//if tile with tileIdentifier+zoomFactor has already been used, return that
		//instead of making another call to the database
		
		
		
		//Create query
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_get_geojson_tiles");
		queryFormatter.setNumberOfFunctionParameters(7);		

		logSQLQuery(
			"getTiles",
			queryFormatter,
			String.valueOf(geography.getName()),
			String.valueOf(geoLevelSelect.getName()),
			String.valueOf(boundaryRectangle.getYMax()),
			String.valueOf(boundaryRectangle.getXMax()),
			String.valueOf(boundaryRectangle.getYMin()),
			String.valueOf(boundaryRectangle.getXMin()),
			String.valueOf(false));
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, geography.getName());
			statement.setString(2, geoLevelSelect.getName());
			statement.setFloat(3, Float.valueOf(boundaryRectangle.getYMax()));
			statement.setFloat(4, Float.valueOf(boundaryRectangle.getXMax()));
			statement.setFloat(5, Float.valueOf(boundaryRectangle.getYMin()));
			statement.setFloat(6, Float.valueOf(boundaryRectangle.getXMin()));
			statement.setBoolean(7, false);
			
			resultSet = statement.executeQuery();

			//Assume at least one row will be returned
			resultSet.next();
			
			//returns JSON
			String result = resultSet.getString(1);
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTiles",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
	
	//Issue: make sure that the results table has a column 'row' because it's where
	//we associate a BETWEEN X AN Y for start and end index of block
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param calculatedResultTableFieldNames
	 * @param startRowIndex
	 * @param endRowIndex
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getCalculatedResultsByBlock(
		final Connection connection,
		final User user,
		final StudySummary studySummary,
		final String[] calculatedResultTableFieldNames,
		final Integer startRowIndex,
		final Integer endRowIndex)
		throws RIFServiceException {
			
		//Validate parameters
		user.checkErrors();
		studySummary.checkErrors();

		String studyID = studySummary.getStudyID();
		String calculatedResultTableName
			= getCalculatedResultTableName(
				connection, 
				studyID);
		if (calculatedResultTableName == null) {
			//Permissions may allow results table to be generated
			//but the job may not have yet been processed
		
		}
		//determine whether the data governance permissions even allow
		//a result table to be generated
		checkPermissionsAllowResultsTable(
			connection,
			studyID);

		validateTableFieldNames(
			connection,
			calculatedResultTableName,
			calculatedResultTableFieldNames);
		
		String startRowPhrase = String.valueOf(startRowIndex);
		String endRowPhrase = String.valueOf(endRowIndex);
		int totalRowsInResultTable 
			= getRowCountForResultTable(
				connection,
				calculatedResultTableName);
				
		if (startRowIndex > totalRowsInResultTable) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unrealisticBlockStartRow",
					String.valueOf(startRowIndex),
					studyID,
					String.valueOf(totalRowsInResultTable));
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNREALISTIC_RESULT_BLOCK_START_ROW, 
					errorMessage);
			throw rifServiceException;
		}
		
		if (startRowIndex > endRowIndex) {
			//ERROR: start row cannot be greater than end row
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.startRowMoreThanEndRow",
					studyID,
					startRowPhrase,
					endRowPhrase);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.RESULT_BLOCK_START_MORE_THAN_END, 
					errorMessage);
			throw rifServiceException;
		}		

		//Create query
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addFromTable(calculatedResultTableName);
		for (String resultTableFieldName : calculatedResultTableFieldNames) {
			queryFormatter.addSelectField(resultTableFieldName);
		}
		queryFormatter.addFromTable(calculatedResultTableName);
		queryFormatter.addWhereBetweenParameter(
			"row", 
			startRowPhrase, 
			endRowPhrase);

		logSQLQuery(
			"getCalculatedResultsByBlock",
			queryFormatter);
		
		int totalRowsInBlock = totalRowsInResultTable - startRowIndex;
		if (endRowIndex > totalRowsInResultTable) {
			//it means the last block of results will not be completely filled
			totalRowsInBlock = endRowIndex - startRowIndex;			
		}
		
		//Execute query and generate results
		RIFResultTable result = new RIFResultTable();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(
			queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			result = generateResultTable(resultSet);
			return result;		
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName 
				= getStudyName(
					connection, 
					studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetCalculatedResultsByBlock",
					studyName,
					startRowPhrase, 
					endRowPhrase);
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
		
	}

	/**
	 * Obtains the name of the table of calculated results that is associated with 
	 * a given study.  The name of the table should appear in the 'map_table' field
	 * of rif40_studies.  Note that this is different from the extract table, which
	 * appears in the 'extract_table' field of the same table.
	 * Assumes that the study exists
	 * @param connection
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	private String getCalculatedResultTableName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		//Create query
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addSelectField("map_table");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");
		
		logSQLQuery(
			"getCalculatedResultTableName",
			queryFormatter,
			studyID);
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			//there should be an entry for the study in rif40_studies.
			//However, it may have a blank value for the map table field value
			resultSet.next();
			String result = resultSet.getString(1);
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName 
				= getStudyName(
					connection, 
					studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetCalculatedResultsTableName",
					studyName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}

	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param geoLevelAttributeSource
	 * @param geoLevelAttribute
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<MapAreaAttributeValue> getMapAreaAttributeValues(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
		
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);

		//check geo level source has no errors
		geoLevelAttributeSource.checkErrors();
		
		//check geo level source exists in the database
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(), 
			geoLevelAttributeSource);
		
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);
		
		
		ArrayList<MapAreaAttributeValue> results
			= new ArrayList<MapAreaAttributeValue>();

		
		//@TODO: fill in the database function name
		
		//Create query		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("");
		queryFormatter.setNumberOfFunctionParameters(2);		
		
		//@TODO: fill this in properly, this query has not been completed yet
		logSQLQuery(
			"getMapAreaAttributeValues",
			queryFormatter,
			geoLevelAttributeSource.getName(),
			geoLevelAttribute);
		
		/*		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(query.generateQuery());
			statement.setString(1, geoLevelAttribute);
			resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				MapAreaAttributeValue mapAreaAttributeValue
					= MapAreaAttributeValue.newInstance();
				mapAreaAttributeValue.setIdentifier(resultSet.getString(1));
				mapAreaAttributeValue.setAttributeValue(resultSet.getString(2));
				mapAreaAttributeValue.setAttributeName(geoLevelAttribute);
				results.add(mapAreaAttributeValue);
			}
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName 
				= getStudyName(
					connection, 
					studyResultRetrievalContext.getStudyID());
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetValuesForMapAreaAttribute",
					geoLevelAttribute,
					studyName,
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
*/
		return results;
	}
	
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<GeoLevelAttributeSource> getGeoLevelAttributeSources(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext) 
		throws RIFServiceException {
		
		//Validate parameters
		user.checkErrors();
		studyResultRetrievalContext.checkErrors();		
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyResultRetrievalContext.getStudyID());
				
		//Create query		
		SQLSelectQueryFormatter queryFormatter 
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addSelectField("extract_table");
		queryFormatter.addSelectField("map_table");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");
				
		logSQLQuery(
			"getGeoLevelAttributeSources",
			queryFormatter,
			studyResultRetrievalContext.getStudyID());
		
		//Execute query and generate results
		ArrayList<GeoLevelAttributeSource> results
			= new ArrayList<GeoLevelAttributeSource>();
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(
				1, 
				Integer.valueOf(studyResultRetrievalContext.getStudyID()));
			resultSet = statement.executeQuery();
			
			//@TODO: uncomment some of the code below.  Need to fix database record entry for kgarwood
			//resultSet.next();
			String extractTableSourceTitle
				= RIFServiceMessages.getMessage("geoLevelAttributeSource.extractTableSource.label");

			//String extractTableName
			//	= resultSet.getString(1);
			String extractTableName = "s1_extract";
			GeoLevelAttributeSource extractTableSource
				= GeoLevelAttributeSource.newInstance(
					extractTableSourceTitle, 
					extractTableName);
			results.add(extractTableSource);

			String mapTableSourceTitle
				= RIFServiceMessages.getMessage("geoLevelAttributeSource.mapTableSource.label");
			//String mapTableName
			//	= resultSet.getString(2);
			String mapTableName = "s1_map";
			GeoLevelAttributeSource mapTableSource
				= GeoLevelAttributeSource.newInstance(
					mapTableSourceTitle, 
					mapTableName);
			results.add(mapTableSource);
			
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetGeoLevelAttributeSources",
					studyResultRetrievalContext.getStudyID(),
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}				
	}
		
	// -- what function to call go get attribute themes?
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<GeoLevelAttributeTheme> getGeoLevelAttributeThemes(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException {

		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		
		//Create query		
		ArrayList<GeoLevelAttributeTheme> results
			= new ArrayList<GeoLevelAttributeTheme>();
		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("");
		queryFormatter.setNumberOfFunctionParameters(2);
		
		logSQLQuery(
			"getGeoLevelAttributeThemes",
			queryFormatter,
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName());
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyResultRetrievalContext.getGeographyName());
			statement.setString(2, studyResultRetrievalContext.getGeoLevelSelectName());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				GeoLevelAttributeTheme geoLevelAttributeTheme
					= GeoLevelAttributeTheme.newInstance(resultSet.getString(1));
				results.add(geoLevelAttributeTheme);
			}
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetGeoLevelAttributeThemes",
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}

	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param geoLevelAttributeSource
	 * @param geoLevelAttributeTheme
	 * @return
	 * @throws RIFServiceException
	 */
	public String[] getAllAttributesForGeoLevelAttributeTheme(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeTheme geoLevelAttributeTheme,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String attributeArrayName)
		throws RIFServiceException {
			
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);		
		geoLevelAttributeSource.checkErrors();		
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		geoLevelAttributeTheme.checkErrors();
		checkGeoLevelAttributeThemeExists(
			connection, 
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttributeTheme);
		
		//Create query		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getallattributesforgeolevelattributetheme");
		queryFormatter.setNumberOfFunctionParameters(4);

		logSQLQuery(
			"getAllAttributesForGeoLevelAttributeTheme",
			queryFormatter,
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName());
		
		//Execute query and generate results
		String[] results = new String[0];			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(
				1, 
				studyResultRetrievalContext.getGeographyName());
			
			statement.setString(
				2, 
				studyResultRetrievalContext.getGeoLevelSelectName());
			
			statement.setString(
				3, 
				geoLevelAttributeTheme.getName());
			statement.setString(4, attributeArrayName);
			resultSet = statement.executeQuery();
			ArrayList<String> attributes = new ArrayList<String>();
			while (resultSet.next()) {
				attributes.add(resultSet.getString(1));
			}
			
			results = attributes.toArray(new String[0]);

			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetAttributesForGeoLevelAttributeTheme",
					geoLevelAttributeTheme.getDisplayName(),
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}	
	
	//CHECKED -- find out function name
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param geoLevelAttributeSource
	 * @param geoLevelAttributeTheme
	 * @return
	 * @throws RIFServiceException
	 */
	public String[] getNumericAttributesForGeoLevelAttributeTheme(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final GeoLevelAttributeTheme geoLevelAttributeTheme) 
		throws RIFServiceException {
			
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelAttributeSource.checkErrors();		
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		
		geoLevelAttributeTheme.checkErrors();
		checkGeoLevelAttributeThemeExists(
			connection, 
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttributeTheme);
					
		//Create query		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addSelectField("attribute");
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getAllAttributesForGeoLevelAttributeTheme");
		queryFormatter.setNumberOfFunctionParameters(3);
		queryFormatter.addWhereParameter("attribute_source");
		queryFormatter.addOrderByCondition("attribute");

		logSQLQuery(
			"getNumericAttributesForGeoLevelAttributeTheme",
			queryFormatter,
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName(),
			geoLevelAttributeTheme.getName(),
			geoLevelAttributeSource.getName());
		
		//Execute query and generate results
		String[] results = new String[0];
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyResultRetrievalContext.getGeographyName());
			statement.setString(2, studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(3, geoLevelAttributeTheme.getName());
			statement.setString(4, geoLevelAttributeSource.getName());
			
			resultSet = statement.executeQuery();
			ArrayList<String> attributes = new ArrayList<String>();
			while (resultSet.next()) {
				attributes.add(resultSet.getString(1));
			}
			
			results = attributes.toArray(new String[0]);

			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetNumericAttributesForGeoLevelAttributeTheme",
					geoLevelAttributeTheme.getDisplayName(),
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
		
	}
	
	//CHECKED -- assume that result table has the field "row" in it to get 
	//start and end row index	
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param calculatedResultTableFieldNames
	 * @param extractStartRowIndex
	 * @param extractEndRowIndex
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getExtractResultsByBlock(
		final Connection connection,
		final User user,
		final StudySummary studySummary,
		final String[] extractTableFieldNames,
		final Integer extractStartRowIndex,
		final Integer extractEndRowIndex)
		throws RIFServiceException {
				
		//Validate parameters
		user.checkErrors();
		studySummary.checkErrors();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studySummary.getStudyID());
				
		
		String studyID = studySummary.getStudyID();
		String extractTableName
			= getExtractTableName(
				connection, 
				studyID);
		if (extractTableName == null) {
			//Permissions may allow results table to be generated
			//but the job may not have yet been processed
		
		}
		//determine whether the data governance permissions even allow
		//a result table to be generated
		checkPermissionsAllowResultsTable(
			connection,
			studyID);

		validateTableFieldNames(
			connection,
			extractTableName,
			extractTableFieldNames);
		
		//Obtain the extract table
		String startRowPhrase = String.valueOf(extractStartRowIndex);
		String endRowPhrase = String.valueOf(extractEndRowIndex);
		int totalRowsInResultTable 
			= getRowCountForResultTable(
				connection,
				extractTableName);
				
		if (extractStartRowIndex > totalRowsInResultTable) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unrealisticBlockStartRow",
					String.valueOf(extractStartRowIndex),
					String.valueOf(totalRowsInResultTable));
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNREALISTIC_RESULT_BLOCK_START_ROW, 
					errorMessage);
			throw rifServiceException;
		}
		
		if (extractStartRowIndex > extractEndRowIndex) {
			//ERROR: start row cannot be greater than end row
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.startRowMoreThanEndRow",
					startRowPhrase,
					endRowPhrase);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.RESULT_BLOCK_START_MORE_THAN_END, 
					errorMessage);
			throw rifServiceException;
		}		

		//KLG: TODO call database function for this

		//Create query		
				
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		for (String extractTableFieldName : extractTableFieldNames) {
			queryFormatter.addSelectField(extractTableFieldName);
		}
		queryFormatter.addFromTable(extractTableName);
		queryFormatter.addWhereBetweenParameter(
			"row", 
			startRowPhrase, 
			endRowPhrase);

		logSQLQuery(
			"getExtractResultsByBlock",
			queryFormatter,
			String.valueOf(extractStartRowIndex),
			String.valueOf(extractEndRowIndex));
			
		int totalRowsInBlock;
		if (extractEndRowIndex > totalRowsInResultTable) {
			//it means the last block of results will not be completely filled
			totalRowsInBlock = extractEndRowIndex - extractStartRowIndex;			
		}
		else {
			totalRowsInBlock = totalRowsInResultTable - extractStartRowIndex;
		}
		
		//Execute query and generate results
		RIFResultTable result = new RIFResultTable();
		result.setFieldNames(extractTableFieldNames);
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			result = generateResultTable(resultSet);
					
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName
				= getStudyName(
					connection, 
					studySummary.getStudyID());
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetExtractResultsByBlock",
					studyName,
					startRowPhrase, 
					endRowPhrase);
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
		
	}

	/**
	 * This is a routine that provides a generic way of transforming rows
	 * returned by the JDBC call into rows that can be easily rendered by clients.
	 * This method needs to know how large the result set is going to be.  
	 * 
	 * <p>
	 * A common way of doing this is to move to the last row of the resultSet and
	 * obtain the row number.  However, this approach is slow.  
	 * It isn't clear how flexible this routine has to be.  If numberOfRows is 
	 * not null, then we will use this value to determine how many rows the result
	 * table should have. 
	 * @param resultSet
	 * @param tableName
	 * @param primaryKeyField
	 * @return
	 * @throws SQLException
	 */
	private RIFResultTable generateResultTable(
		final ResultSet resultSet)
		throws SQLException {
		
		RIFResultTable rifResultTable = new RIFResultTable();
		
		//Obtain the total number of rows
		int totalResultRows = 0;
		if (resultSet == null) {
			return rifResultTable;
		}		
		try {
			resultSet.last();
			totalResultRows = resultSet.getRow();
		}
		finally {
			resultSet.beforeFirst();
		}		
		
		//Obtain the column names
		ResultSetMetaData resultSetMetaData
			= resultSet.getMetaData();

		int numberOfColumns = resultSetMetaData.getColumnCount();
		String[] resultFieldNames = new String[numberOfColumns];
		for (int i = 0; i < resultFieldNames.length; i++) {
			resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
		}
		rifResultTable.setFieldNames(resultFieldNames);
					
		String[][] resultsBlockData
			= new String[totalResultRows][resultFieldNames.length];
		
		int ithRow = 0;
		while (resultSet.next()) {
			for (int i = 1; i < resultFieldNames.length; i++) {
				resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
			}			
			ithRow = ithRow + 1;
		}
		rifResultTable.setData(resultsBlockData);
		
		return rifResultTable;
	}
	
	
	private String getExtractTableName(
		final Connection connection,
		final String studyID) 
		throws RIFServiceException {
					
		//Create query		
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		queryFormatter.addSelectField("extract_table");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");
				
		logSQLQuery(
			"getExtractTableName",
			queryFormatter,
			studyID);
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				/*
				 * In many of the queries in this class, we assume that 
				 * a query will generate a result.  This is not the case
				 * for getting the name of an extract table for a given study.
				 * A study may not have been run yet to produce a results table
				 * It is also the case that permissions problems prevent result
				 * tables from being generated.
				 */
				String studyName
					= getStudyName(
						connection,
						studyID);
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.unableToGetExtractTableName",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_GET_EXTRACT_TABLE_NAME, 
						errorMessage);
				throw rifServiceException;
			}
			
			String extractTableName = resultSet.getString(1);
			return extractTableName;
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName
				= getStudyName(connection, studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetExtractTableName",
					studyName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
	}
	
	
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public String[] getGeometryColumnNames(
		final Connection connection,
		final User user,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
		
		//Validate parameters
		user.checkErrors();
		diseaseMappingStudy.checkErrors();
		
		//Create query
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		
		//KLG: TODO unfinished method
		logSQLQuery(
			"getGeometryColumnNames",
			queryFormatter,
			diseaseMappingStudy.getIdentifier());
		
		//Execute query and generate results
		String[] results = new String[0];
		//get the name of the calculated results table - ie the map table
		//KLG: TODO - do we need more parameters in the method here?
		String errorMessage
			= RIFServiceMessages.getMessage(
				"sqlResultsQueryManager.error.unableToGetGeometryColumnNames",
				diseaseMappingStudy.getDisplayName());
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.toString());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
			
			ArrayList<String> columnNames = new ArrayList<String>();
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				columnNames.add(resultSet.getString(1));
			}
			
			if (columnNames.size() == 0) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_GET_GEOMETRY_COLUMN_NAMES, 
						errorMessage);
				throw rifServiceException;				
			}
			
			results = columnNames.toArray(new String[0]);
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}
		
	
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param geoLevelAttributeTheme
	 * @param geoLevelAttribute
	 * @param mapAreas
	 * @param year
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getResultsStratifiedByGenderAndAgeGroup(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelToMap geoLevelToMap,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelSourceAttribute,
		final ArrayList<MapArea> mapAreas,
		final Integer year) 
		throws RIFServiceException {
		
		user.checkErrors();
		studyResultRetrievalContext.checkErrors();
		geoLevelToMap.checkErrors();
		geoLevelAttributeSource.checkErrors();
		for (MapArea mapArea : mapAreas) {
			mapArea.checkErrors();
		}

		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(), 
			geoLevelAttributeSource);
		
		sqlMapDataManager.checkAreasExist(
			connection, 
			studyResultRetrievalContext.getGeographyName(), 
			geoLevelToMap.getName(), 
			mapAreas);
		
		/*
		//@TODO - KLG - create database function for this.

		//Create query
	
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
		//fill in parameters we need for the query
		logSQLQuery(
			"",
			queryFormatter);
		
		//Execute query and generate results
		RIFResultTable result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.toString());
			resultSet = statement.executeQuery();
			result = generateResultTable(resultSet);			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.getResultsStratifiedByGenderAndAgeGroup",
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getName(),
					geoLevelSourceAttribute);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
		
		*/
		
		//stub 
		RIFResultTable results = new RIFResultTable();
		return results;		
	}


	/**
	 * STUBBED
	 * @param connection
	 * @param resultTableName
	 * @return
	 * @throws RIFServiceException
	 */
	private int getRowCountForResultTable(
		final Connection connection,
		final String resultTableName)
		throws RIFServiceException {
		
		//TODO: Peter
		
		return 0;
	}

	public RIFResultTable getPyramidData(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
	
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelAttributeSource.checkErrors();
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		
		
		/*
		 * @TODO - we need to develop this procedure in the DB
		 */
		
		RIFResultTable result = new RIFResultTable();

		/*
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("get_pyramid_data");
		queryFormatter.setNumberOfFunctionParameters(3);
		
		logSQLQuery(
			"getPyramidData",
			queryFormatter,
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource.getName(),
			geoLevelAttribute);
			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			result = generateResultTable(resultSet);
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.getPyramidData",
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttribute);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		 */
		return result;		
	}	
	
	public RIFResultTable getPyramidDataByYear(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute,
		final Integer year) 
		throws RIFServiceException {
				
		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelAttributeSource.checkErrors();
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		RIFResultTable result = new RIFResultTable();
		
		/*
		 * @TODO - we need to develop this procedure in the DB
		 */

		/*
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		
		logSQLQuery(
			"getPyramidDataByYear",
			queryFormatter);

		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		 */		
		
		return result;
	}
	
	public RIFResultTable getPyramidDataByMapAreas(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelToMap geoLevelToMap,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute,
		final ArrayList<MapArea> mapAreas) 
		throws RIFServiceException {

		//Validate parameters
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelToMap.checkErrors();
		geoLevelAttributeSource.checkErrors();

		for (MapArea mapArea : mapAreas) {
			mapArea.checkErrors();
		}
		
		//check non-existent geo level to map
		sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(
			connection, 
			studyResultRetrievalContext.getGeographyName(), 
			studyResultRetrievalContext.getGeoLevelSelectName(),
			geoLevelToMap.getName(),
			true);
		
		//check non-existent map areas
		sqlMapDataManager.checkAreasExist(
			connection, 
			studyResultRetrievalContext.getGeographyName(), 
			geoLevelToMap.getName(), 
			mapAreas);
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
	
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		//check map areas are valid
		for (MapArea mapArea : mapAreas) {
			mapArea.checkErrors();
		}
		
		RIFResultTable results = new RIFResultTable();
		

		/*
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		
		logSQLQuery(
			"getPyramidDataByYear",
			queryFormatter);

		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		 */		
		
		return results;
	}
	
	public String[] getResultFieldsStratifiedByAgeGroup(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException {
	
		String[] results = new String[0];
	

		/*
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		
		logSQLQuery(
			"getPyramidDataByYear",
			queryFormatter);

		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		 */		
		
		return results;
	}
	
	public RIFResultTable getSMRValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
	
		studySummary.checkErrors();
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		
		RIFResultTable results = new RIFResultTable();
		/*
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement 
				= connection.prepareStatement(query.toString());
			resultSet = statement.executeQuery();

	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setFieldNames(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		*/
		return results;
	}
	
	
	public RIFResultTable getRRValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
				
		studySummary.checkErrors();
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		
		RIFResultTable results = new RIFResultTable();
		/*
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement 
				= connection.prepareStatement(query.toString());
			resultSet = statement.executeQuery();

	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setFieldNames(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		*/
		return results;		
	}

	public RIFResultTable getRRUnadjustedValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
				
		studySummary.checkErrors();
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		
		RIFResultTable results = new RIFResultTable();
		/*
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement 
				= connection.prepareStatement(query.toString());
			resultSet = statement.executeQuery();

	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setFieldNames(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		*/
		return results;		
	}
	
	public RIFResultTable getResultStudyGeneralInfo(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
	
		studySummary.checkErrors();
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
				
		RIFResultTable results = new RIFResultTable();
		
		/*
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		*/
		return results;
	}

	public ArrayList<AgeGroup> getResultAgeGroups(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelSourceAttribute)
		throws RIFServiceException {
		
		user.checkErrors();
		studyResultRetrievalContext.checkErrors();
		geoLevelAttributeSource.checkErrors();

		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();
		/*
		@TODO: KLG - we need to implement this database method
		
		StringBuilder query = new StringBuilder();
		
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(query.toString());
			resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				AgeGroup ageGroup = AgeGroup.newInstance();
				results.add(ageGroup);
			}
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetResultAgeGroups",
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getName(),
					geoLevelSourceAttribute);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		*/
		
		return results;
	}
		
	public String getStudyName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addSelectField("study_name");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");
		
		logSQLQuery(
			"getStudyName",
			queryFormatter,
			studyID);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(
				1, 
				Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			if (resultSet.next() == false) {
				String recordType
					= RIFServiceMessages.getMessage("diseaseMappingStudy.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY, 
						errorMessage);
					throw rifServiceException;
			}
			return resultSet.getString(1);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetStudyName",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
	}

	private String[][] getGeometryColumnNames(
		final Connection connection,
		final Geography geography) 
		throws RIFServiceException {
		
		geography.checkErrors();
		
		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getgeometrycolumnnames");
		queryFormatter.setNumberOfFunctionParameters(1);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String[][] results = null;
		try {
			statement 
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			statement.setString(1, geography.getName());
			resultSet = statement.executeQuery();
			resultSet.last();			
			int numberOfResults = resultSet.getRow();

			resultSet.beforeFirst();
			results = new String[numberOfResults][2];
			for (int i = 0; i < results.length; i++) {
				resultSet.next();
				results[i][0] = resultSet.getString(1);
				results[i][1] = resultSet.getString(2);
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.getGeometryColumnNames",
					geography.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		
		return results;
	}
		
	private int createMapAreaAttributeSource(
		final Connection connection,
		final String temporaryTableName,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelAttributeTheme geoLevelAttributeTheme,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String attributeNameArray) throws RIFServiceException {
		
		geography.checkErrors();
		geoLevelSelect.checkErrors();
		geoLevelAttributeTheme.checkErrors();
		geoLevelAttributeSource.checkErrors();
		
		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_createmapareaattributesource");		
		queryFormatter.setNumberOfFunctionParameters(6);
			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Integer result = null;
		try {
			statement = connection.prepareStatement(
				queryFormatter.generateQuery());
			statement.setString(1, temporaryTableName);
			statement.setString(2, geography.getName());
			statement.setString(3, geoLevelSelect.getName());
			statement.setString(4, geoLevelAttributeTheme.getName());
			statement.setString(5, geoLevelAttributeSource.getName());
					
			result = statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.createMapAreaAttributeSource",
					temporaryTableName,
					geoLevelAttributeSource.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}

	private void closeMapAreaAttributeCursor(
		final Connection connection,
		final String cursorName)
		throws RIFServiceException {
		
		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_closegetmapareaattributecursor");		
		queryFormatter.setNumberOfFunctionParameters(1);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(
				queryFormatter.generateQuery());
			statement.setString(1, cursorName);
			statement.executeQuery();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.closeMapAreaAttributeSource",
					cursorName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	
	
	private void deleteMapAreaAttributeSource(
		final Connection connection,
		final String temporaryTableName) 
		throws RIFServiceException {
	
		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_deletemapareaattributesource");		
		queryFormatter.setNumberOfFunctionParameters(1);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(
				queryFormatter.generateQuery());
			statement.setString(1, temporaryTableName);
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.deleteMapAreaAttributeSource",
					temporaryTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void validateCommonParameters(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext)
		throws RIFServiceException {

		user.checkErrors();
		studyResultRetrievalContext.checkErrors();		
		sqlRIFContextManager.checkGeographyExists(
			connection, 
			studyResultRetrievalContext.getGeographyName());
		
		sqlRIFContextManager.checkGeoLevelSelectExists(
			connection, 
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName());		

		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyResultRetrievalContext.getStudyID());
	}

	
	private void validateCommonParameters(
		final Connection connection,
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {
		
		user.checkErrors();
		geography.checkErrors();	
		geoLevelSelect.checkErrors();
		sqlRIFContextManager.checkGeographyExists(
			connection, 
			geography.getName());
		sqlRIFContextManager.checkGeoLevelSelectExists(
			connection, 
			geography.getName(),
			geoLevelSelect.getName());
	}
	
	
	
	
	/**
	 * Checks whether permissions associated with the table would
	 * prevent results tables from being generated.  We determine the
	 * permissions solely by looking at the value of the 'extract_permitted'
	 * field in the table rif40_studies.  We do *not* consider the value of
	 * 'transfer_permitted', which for now remains an information governance
	 * property that informs documentation rather than influences whether the
	 * middleware returns results or not.
	 * 
	 * <p>
	 * Assumes that the diseaseMappingStudy exists
	 * </p>
	 * @param connection
	 * @param diseaseMappingStudy
	 */
	private void checkPermissionsAllowResultsTable(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
	
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.addSelectField("extract_permitted");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_id");
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String studyName = "";
		try {
			studyName
				= getStudyName(
					connection, 
					studyID);
			statement
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			//We can assume that the table will have an entry for the study
			resultSet.next();
			Boolean extractPermitted = resultSet.getBoolean(1);
			if (extractPermitted == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.unableToDetermineExtractPermissionForStudy",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_DETERMINE_EXTRACT_PERMISSION_FOR_STUDY, 
						errorMessage);
				throw rifServiceException;				
			}
			else if (extractPermitted == false) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.studyDoesNotAllowExtraction",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.EXTRACT_NOT_PERMITTED_FOR_STUDY, 
						errorMessage);
				throw rifServiceException;
			}			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToDetermineExtractPermissionForStudy",
					studyName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}

	
	private void validateTableFieldNames(
		final Connection connection, 
		final String tableName,
		final String[] tableFieldNames) 
		throws RIFServiceException {

		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setSchema("rif40");
		queryFormatter.setFunctionName("table_field_exists");
		queryFormatter.setNumberOfFunctionParameters(2);
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();		
		ArrayList<String> errorMessages = new ArrayList<String>();
		for (String tableFieldName : tableFieldNames) {
			if (fieldValidationUtility.isEmpty(tableFieldName)) {
				//field name is empty
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.emptyTableFieldName",
						tableName);
				errorMessages.add(errorMessage);
			}
		}

		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_TABLE_FIELD_NAMES,
					errorMessages);
			throw rifServiceException;
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(
				queryFormatter.generateQuery());
						
			for (String tableFieldName : tableFieldNames) {
				//check if field exists in table
				statement.setString(1, tableName);
				statement.setString(2, tableFieldName);
					
				/**
				 * @TODO - we need a table function that does this
				resultSet = statement.executeQuery();
				resultSet.next();
				Boolean exists = resultSet.getBoolean(1);
				if (exists == false) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"sqlResultsQueryManager.error.nonExistentTableField",
							tableName,
							tableFieldName);
					errorMessages.add(errorMessage);
				}
				*/
			}
			
			if (errorMessages.size() > 0) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_TABLE_FIELD_NAME,
						errorMessages);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {			
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckTableFieldExists",
					tableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		
	}
	
	private void checkResultsTableExists(
		final Connection connection,
		final String studyID,
		final String resultsTableName)
		throws RIFServiceException {
		
		//Create query/
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String studyName = "";
		try {			
			studyName
				= getStudyName(
					connection, 
					studyID);
			statement
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			statement.setString(1, studyID);
			resultSet 
				= statement.executeQuery();
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentResultTable",
						resultsTableName,
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_RESULT_TABLE, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckResultTableExists",
					studyName);
			RIFServiceException rifServiceException
				 = new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}
	
	private void checkResultsTableFieldExists(
		final Connection connection,
		final String studyID,
		final String resultsTableName,
		final String resultsTableFieldName)
		throws RIFServiceException {

		
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			resultSet 
				= statement.executeQuery();
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentResultTableField",
						resultsTableFieldName,
						resultsTableName,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_RESULT_TABLE_FIELD_NAME, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String studyName
				= getStudyName(
					connection, 
					studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckResultTableFieldExists",
					resultsTableFieldName,
					resultsTableName,
					studyName);
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}
	
	
	//Check what query we need
	private void checkMapAreaExists(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea) 
		throws RIFServiceException {
		
				
		//Use geo level select to determine the correct
		//resolution lookup table.
	
		//KLG: @TODO - need to implement this method
		//should this check be one that checks if a map area exists in
		//the geography or that it is part of the study?
		
		/**
		String geographyTable = "sahsuland_geography";
		
		
		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setFromTable(geographyTable);
		query.
		query.addWhereParameter(geoLevelSelect.getName());
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(query.generateQuery());
			statement.setString(1, mapArea.getIdentifier());
			resultSet = statement.executeQuery();
		
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentMapArea",
						mapArea.getDisplayName(),
						geography.getDisplayName(),
						geoLevelSelect.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_MAP_AREA, 
						errorMessage);
				throw rifServiceException;
			}			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckMapAreaExists",
					mapArea.getDisplayName(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}	
		
		*/
	}

	/**
	 * @TODO - This check determines whether the map area appears
	 * within the map table of the study
	 * @param connection
	 * @param studyResultRetrievalContext
	 * @param mapArea
	 * @throws RIFServiceException
	 */
	private void checkMapAreaExistsInStudy(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea) 
		throws RIFServiceException {

		
		//@TODO
		
	}
	
	//CHECKED -- find out the name of the function
	private void checkGeoLevelAttributeExists(
		final Connection connection,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
		
		/*
		SQLFunctionCallerQueryFormatter query
			= new SQLFunctionCallerQueryFormatter();
		query.setSchema("rif40_xml_pkg");
		query.setFunctionName("geo_level_attribute_exists");
		query.setNumberOfFunctionParameters(3);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {			
			statement 	
				= connection.prepareStatement(query.generateQuery());
			statement.setString(
				1, 
				studyResultRetrievalContext.getGeographyName());
			statement.setString(
				2, 
				studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(
				3, 
				geoLevelAttribute);
			resultSet = statement.executeQuery();
			resultSet.next();
			Boolean geoLevelSelectExists = resultSet.getBoolean(1);
			if (geoLevelSelectExists == false) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentGeoLevelAttribute",
						geoLevelAttribute,
						studyResultRetrievalContext.getGeographyName(),
						studyResultRetrievalContext.getGeoLevelSelectName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckGeoLevelAttributeExists",
					geoLevelAttribute,
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		*/
	}

	private void checkGeoLevelAttributeSourceExists(
		final Connection connection,
		final String studyID,
		final GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException {
		
	}
	
	private void checkGeoLevelAttributeThemeExists(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final GeoLevelAttributeTheme geoLevelAttributeTheme)
		throws RIFServiceException {

		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setUseDistinct(true);
		queryFormatter.addSelectField("theme");
		queryFormatter.setSchema("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_getAllAttributesForGeoLevelAttributeTheme");
		queryFormatter.setNumberOfFunctionParameters(3);
		queryFormatter.addWhereParameter("attribute_source");
		logSQLQuery(
			"checkGeoLevelAttributeThemeExists1", 
			queryFormatter, 
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName(),
			geoLevelAttributeTheme.getName(),
			geoLevelAttributeSource.getName());
			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 	
				= connection.prepareStatement(
					queryFormatter.generateQuery());
			statement.setString(
				1, 
				studyResultRetrievalContext.getGeographyName());
			statement.setString(
				2, 
				studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(
				3, 
				geoLevelAttributeTheme.getName());
			statement.setString(
				4, 
				geoLevelAttributeSource.getName());
			resultSet = statement.executeQuery();
			if (resultSet.next() == false) {
				//Error doesn't exist
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentGeoLevelAttributeTheme",
						geoLevelAttributeTheme.getDisplayName(),
						studyResultRetrievalContext.getGeographyName(),
						studyResultRetrievalContext.getGeoLevelSelectName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE_THEME,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckGeoLevelAttributeThemeExists",
					geoLevelAttributeTheme.getDisplayName(),
					geoLevelAttributeSource.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
