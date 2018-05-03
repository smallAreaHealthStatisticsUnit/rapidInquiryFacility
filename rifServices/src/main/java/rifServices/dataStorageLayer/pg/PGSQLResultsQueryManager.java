package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.FunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Geography;
import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.dataStorageLayer.common.ResultsQueryManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class PGSQLResultsQueryManager extends BaseSQLManager implements ResultsQueryManager {

	public PGSQLResultsQueryManager(
			final RIFServiceStartupOptions startupOptions) {
		
		super(startupOptions);
		
		FunctionCallerQueryFormatter getTilesQueryFormatter = new FunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);		
	}

	@Override
	public RIFResultTable getTileMakerCentroids(
			final Connection connection,
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
			throws RIFServiceException {
								
			PGSQLSelectQueryFormatter getMapTileTableQueryFormatter
				= new PGSQLSelectQueryFormatter();		
				
			getMapTileTableQueryFormatter.setDatabaseSchemaName("rif_data");
			getMapTileTableQueryFormatter.addSelectField(geoLevelSelect.getName());
			getMapTileTableQueryFormatter.addSelectField("areaname");
			getMapTileTableQueryFormatter.addSelectField("geographic_centroid");		
			getMapTileTableQueryFormatter.addFromTable("lookup_" + geoLevelSelect.getName());
					
			PreparedStatement resultCounterStatement = null;
			PreparedStatement statement = null;
			ResultSet resultCounterSet = null;
			ResultSet resultSet = null;
					
			try {
				//Count the number of results first
				resultCounterStatement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());
				resultCounterSet = resultCounterStatement.executeQuery();
				
				int totalNumberRowsInResults = 0;
				while (resultCounterSet.next()) {			
					totalNumberRowsInResults++;
				}

				//get the results
				statement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());	
				
				RIFResultTable results = new RIFResultTable();	
				
				String[] columnNames = new String[4];
				columnNames[0] = "id";
				columnNames[1] = "name";
				columnNames[2] = "x";
				columnNames[3] = "y";
				
				RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[4];
				columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;
				
				String[][] data = new String[totalNumberRowsInResults][4];
				int ithRow = 0;
												
				resultSet = statement.executeQuery();
				while (resultSet.next()) {					
					data[ithRow][0] = resultSet.getString(1);
					data[ithRow][1] = resultSet.getString(2);									
					String coords = resultSet.getString(3).split(":")[2];	
					String x = coords.split(",")[0];
					String y = coords.split(",")[1];
					x = x.replaceAll("[^0-9?!\\.-]","");
					y = y.replaceAll("[^0-9?!\\.-]","");		
					data[ithRow][2] = x;
					data[ithRow][3] = y;
					ithRow++;
				}	
				
				results.setColumnProperties(columnNames, columnDataTypes);
				results.setData(data);	
				connection.commit();	
									
				return results;
			}
			catch(SQLException sqlException) {
				//Record original exception, throw sanitised, human-readable version			
				logSQLException(sqlException);
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.unableToGetCentroids",
						geoLevelSelect.getDisplayName(),
						geography.getDisplayName());
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
	
	@Override
	public String getTileMakerTiles(
			final Connection connection,
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect,
			final Integer zoomlevel,
			final Integer x,
			final Integer y)
		throws RIFServiceException {

		//STEP 1: get the tile table name

		PGSQLSelectQueryFormatter getMapTileTableQueryFormatter
			= new PGSQLSelectQueryFormatter();		
			
		getMapTileTableQueryFormatter.setDatabaseSchemaName("rif40");
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "tiletable");
		getMapTileTableQueryFormatter.addFromTable("rif40_geographies");
		getMapTileTableQueryFormatter.addWhereParameter("geography");
						
		logSQLQuery(
			"getTileMakerTiles",
			getMapTileTableQueryFormatter,
			geography.getName().toUpperCase());			
				
		//For tile table name
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		//For map tiles
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;
		
		try {
	
			statement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());		
			statement.setString(1, geography.getName().toUpperCase());
					
			resultSet = statement.executeQuery();
			resultSet.next();
			
			//This is the tile table name for this geography
			String myTileTable = resultSet.getString(1);
					
			PGSQLSelectQueryFormatter getMapTilesQueryFormatter
				= new PGSQLSelectQueryFormatter();

			//STEP 2: get the tiles
			getMapTilesQueryFormatter.addSelectField(myTileTable,"optimised_topojson");
			getMapTilesQueryFormatter.addFromTable(myTileTable);
			getMapTilesQueryFormatter.addFromTable("rif40_geolevels");	
			getMapTilesQueryFormatter.addWhereJoinCondition(myTileTable, "geolevel_id", "rif40_geolevels", "geolevel_id");
			getMapTilesQueryFormatter.addWhereParameter("rif40_geolevels", "geolevel_name");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "zoomlevel");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "x");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "y");
			
			logSQLQuery(
				"getTileMakerTiles",
				getMapTilesQueryFormatter,
				geoLevelSelect.getName().toUpperCase(),
				zoomlevel.toString(),
				x.toString(),
				y.toString());
				
			statement2 = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());	
			statement2.setString(1, geoLevelSelect.getName().toUpperCase());
			statement2.setInt(2, zoomlevel);
			statement2.setInt(3, x);
			statement2.setInt(4, y);
			
			resultSet2 = statement2.executeQuery();
			resultSet2.next();
			String result = resultSet2.getString(1);

			rifLogger.info(getClass(), "get tile for geogrpahy: " + geography.getName().toUpperCase() +
				"; tileTable: " + myTileTable + 
				"; zoomlevel: " + zoomlevel.toString() + 
				"; geolevel: " + geoLevelSelect.getName().toUpperCase() + " x/y: " + x + "/" + y +
				"; length: " + result.length());

			connection.commit();				
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
			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
			
			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}			
	}
				
		
	@Override
	public String getStudyName(
			final Connection connection,
			final String studyID)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("study_name");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
		
			logSQLQuery(
				"getStudyName",
				queryFormatter,
				studyID);
				
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
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
			
			String result = resultSet.getString(1);
			
			connection.commit();			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
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
}
