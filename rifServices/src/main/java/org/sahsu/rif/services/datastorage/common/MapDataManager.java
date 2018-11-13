package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractGeographicalArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class MapDataManager extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	public MapDataManager(
			final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
	}

	ArrayList<MapArea> getAllRelevantMapAreas(
			final Connection connection,
			final Geography geography,
			final AbstractGeographicalArea geographicalArea,
			final boolean isStudyArea,
			final boolean isDiseaseMappingStudy)
			throws Exception {

		ArrayList<MapArea> allRelevantMapAreas = new ArrayList<>();

		GeoLevelSelect geoLevelSelect
				= geographicalArea.getGeoLevelSelect();
		GeoLevelToMap geoLevelToMap
				= geographicalArea.getGeoLevelToMap();


		ArrayList<MapArea> selectedMapAreas
				= geographicalArea.getMapAreas();
		if (isStudyArea) {
			rifLogger.info(this.getClass(), "SQLMapDataManager getAllRelevantMapAreas() study areas: " + selectedMapAreas.size());
		}
		else {
			rifLogger.info(this.getClass(), "SQLMapDataManager getAllRelevantMapAreas() comparison areas: " + selectedMapAreas.size());
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {

			/*
			 * Step 1: Obtain the geography table; This maps the map identifier as it is known
			 * at the GeoLevelSelect level to the map identifier as it is known at the finer
			 * resolution of GeoLevelToMap
			 */
			//Obtain geography table eg: sahsuland_geography
			String mapAreaResolutionMappingTableName
					= getMapAreaResolutionMappingAreaTableName(
					connection,
					geography);

			String geoLevelToMapTableName
					= getGeoLevelLookupTableName(
					connection,
					geography,
					geoLevelToMap.getName());

			/*
			 * Example:
			 *
			 * SELECT
			 *    gid, 		// gid
			 *    level4,	// geoLevelToMap.getName()
			 *    level2 	// geoLevelSelect.getName()
			 * FROM
			 *    mapAreaResolutionMappingTableName,  //eg: sahsuland_geography
			 *    geoLevelToMapTableName //eg: sahsuland_level4
			 * WHERE
			 *    level2='01.001' OR  // iteratively read in each map area provided by
			 *    level2='01.002' OR  // by client
			 *    level2='01.003' OR  // geoLevelSelect.getName()
			 *    ...
			 */
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "SELECT DISTINCT");
			queryFormatter.addQueryPhrase(1, geoLevelToMapTableName);
			queryFormatter.addQueryPhrase(".gid,");
			queryFormatter.addQueryPhrase(geoLevelToMapTableName);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());
			queryFormatter.addQueryPhrase(",");
			queryFormatter.addQueryPhrase(mapAreaResolutionMappingTableName);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(geoLevelSelect.getName());
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryLine(0, "FROM ");
			queryFormatter.addQueryPhrase(
					1,
					applySchemaDataPrefixIfNeeded(mapAreaResolutionMappingTableName));
			queryFormatter.addQueryPhrase(",");
			queryFormatter.addQueryPhrase(applySchemaDataPrefixIfNeeded(geoLevelToMapTableName));
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryLine(0, "WHERE");

			queryFormatter.addQueryPhrase(1, geoLevelToMapTableName);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());
			queryFormatter.addQueryPhrase("=");
			queryFormatter.addQueryPhrase(mapAreaResolutionMappingTableName);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());

			HashMap<String, Integer> bandHash = new HashMap<String, Integer>();
			HashMap<String, Integer> intersectCountHash = new HashMap<String, Integer>();
			HashMap<String, Double> distanceFromNearestSourceHash = new HashMap<String, Double>();
			HashMap<String, String> nearestRifShapePolyIdHash = new HashMap<String, String>();
			HashMap<String, Double> exposureValueHash = new HashMap<String, Double>();
			Integer intersectCount;
			Double distanceFromNearestSource;
			String nearestRifShapePolyId;
			Double exposureValue;
			
			int totalSelectedMapAreas = selectedMapAreas.size();
			if (totalSelectedMapAreas > 0) {

				queryFormatter.addQueryPhrase(" AND (");
				queryFormatter.padAndFinishLine();

				String geoLevelSelectLevelName = geoLevelSelect.getName();
				//String geoLevelSelectLevelName = geoLevelToMap.getName();

				for (int i = 0 ; i < selectedMapAreas.size(); i++) {
					if (i != 0) {
						queryFormatter.padAndFinishLine();
						queryFormatter.addQueryPhrase(1, " OR ");
					}

					queryFormatter.addQueryPhrase(mapAreaResolutionMappingTableName);
					//queryFormatter.addQueryPhrase(geoLevelToMapTableName);
					queryFormatter.addQueryPhrase(".");
					queryFormatter.addQueryPhrase(geoLevelSelectLevelName);
					queryFormatter.addQueryPhrase("=\'");
					queryFormatter.addQueryPhrase(selectedMapAreas.get(i).getIdentifier());
					queryFormatter.addQueryPhrase("'");
					
					bandHash.put(selectedMapAreas.get(i).getIdentifier(), selectedMapAreas.get(i).getBand());
					intersectCountHash.put(selectedMapAreas.get(i).getIdentifier(), selectedMapAreas.get(i).getIntersectCount());
					nearestRifShapePolyIdHash.put(selectedMapAreas.get(i).getIdentifier(), selectedMapAreas.get(i).getNearestRifShapePolyId());
					distanceFromNearestSourceHash.put(selectedMapAreas.get(i).getIdentifier(), selectedMapAreas.get(i).getDistanceFromNearestSource());
					exposureValueHash.put(selectedMapAreas.get(i).getIdentifier(), selectedMapAreas.get(i).getExposureValue());
				}

				queryFormatter.addQueryPhrase(")");
			}

			queryFormatter.addQueryPhrase(";");

			logSQLQuery(
					"getAllRelevantMapAreas",
					queryFormatter,
					geography.getName(),
					geoLevelToMap.getName());

			statement
					= createPreparedStatement(
					connection,
					queryFormatter);

			resultSet = statement.executeQuery();
			int i=1;
			while (resultSet.next()) {
				String identifier
						= resultSet.getString(1);
				String geoLevelToMapName
						= resultSet.getString(2);
				String geoLevelSelectName
						= resultSet.getString(3);

				intersectCount=0;
				distanceFromNearestSource=0.0;
				nearestRifShapePolyId=null;
				exposureValue=0.0;
						
				// Add band back		
				int band=-1;
				if (isDiseaseMappingStudy) {
					band=i;
				}
				else if (!isStudyArea) {
					band=0; /* Comparison area */
				}
				else if (isStudyArea) { // Risk analysis
					if (bandHash.containsKey(geoLevelSelectName)) {
						band=bandHash.get(geoLevelSelectName);
					}
					if (intersectCountHash.containsKey(geoLevelSelectName)) {
						intersectCount=intersectCountHash.get(geoLevelSelectName);
					}
					if (distanceFromNearestSourceHash.containsKey(geoLevelSelectName)) {
						distanceFromNearestSource=distanceFromNearestSourceHash.get(geoLevelSelectName);
					}
					if (nearestRifShapePolyIdHash.containsKey(geoLevelSelectName)) {
						nearestRifShapePolyId=nearestRifShapePolyIdHash.get(geoLevelSelectName);
					}	
					if (exposureValueHash.containsKey(geoLevelSelectName)) {
						exposureValue=exposureValueHash.get(geoLevelSelectName);
					}					
					if (band < 1) {
						StringBuilder builder = new StringBuilder();
						for (Map.Entry<String, Integer> entry : bandHash.entrySet()) {
							String key = entry.getKey();
							Integer value = entry.getValue();
							builder.append("Key (areaid): " + key + "; value(band): " + value + lineSeparator);
						}
						rifLogger.info(this.getClass(), "bandHash: " + builder.toString());
						
						throw new Exception("No valid band: " + band + "; found for study area selectedMapAreas: " + 
							identifier + "; map: " + geoLevelToMapName + "; select: " + geoLevelSelectName + ")");
					}
					else if (intersectCount == 0 && distanceFromNearestSource == 0.0 && nearestRifShapePolyId == null) {
						StringBuilder builder = new StringBuilder();
						for (Map.Entry<String, Integer> entry : intersectCountHash.entrySet()) {
							String key = entry.getKey();
							Integer value = entry.getValue();
							builder.append("Key (areaid): " + key + "; value(intersectCount): " + value + lineSeparator);
						}
						rifLogger.info(this.getClass(), "intersectCountHash: " + builder.toString());
						builder = new StringBuilder();
						for (Map.Entry<String, Double> entry : distanceFromNearestSourceHash.entrySet()) {
							String key = entry.getKey();
							Double value = entry.getValue();
							builder.append("Key (areaid): " + key + "; value(distanceFromNearestSourceHash): " + value + lineSeparator);
						}
						rifLogger.info(this.getClass(), "distanceFromNearestSourceHash: " + builder.toString());
						builder = new StringBuilder();
						for (Map.Entry<String, String> entry : nearestRifShapePolyIdHash.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							builder.append("Key (areaid): " + key + "; value(nearestRifShapePolyIdHash): " + value + lineSeparator);
						}
						rifLogger.info(this.getClass(), "nearestRifShapePolyIdHash: " + builder.toString());
						
						throw new Exception("No valid intersectCount, distanceFromNearestSource and nearestRifShapePolyId; found for study area selectedMapAreas: " + 
							identifier + "; map: " + geoLevelToMapName + "; select: " + geoLevelSelectName + ")");
					}
					else if (nearestRifShapePolyId == null) {
						StringBuilder builder = new StringBuilder();
						for (Map.Entry<String, String> entry : nearestRifShapePolyIdHash.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							builder.append("Key (areaid): " + key + "; value(nearestRifShapePolyIdHash): " + value + lineSeparator);
						}
						rifLogger.info(this.getClass(), "nearestRifShapePolyIdHash: " + builder.toString());
						
						throw new Exception("No valid nearestRifShapePolyId; found for study area selectedMapAreas: " + 
							identifier + "; map: " + geoLevelToMapName + "; select: " + geoLevelSelectName + ")");
					}
				}
				else {
					throw new Exception("Dr Spock moment: isStudyArea: " + isStudyArea + 
						"; isDiseaseMappingStudy: " + isDiseaseMappingStudy);
				}
				
				MapArea mapArea
						= MapArea.newInstance(
						identifier,
						identifier,
						geoLevelToMapName,
						band,
						intersectCount,
						distanceFromNearestSource,
						nearestRifShapePolyId,
						exposureValue);
				if (i < 10) {
					rifLogger.info(this.getClass(), "[" + i + "] MapArea.newInstance(" +
						identifier + "," +
						identifier + "," +
						geoLevelToMapName + "," +
						band + "," +
						intersectCount + "," +
						distanceFromNearestSource + "," +
						nearestRifShapePolyId + "," +
						exposureValue);
				}
				allRelevantMapAreas.add(mapArea);
				i++;
			}
		}
		catch(Exception exception) {
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlMapDataManager.error.unableToRetrievaAllRelevantMapAreas");
			RIFServiceException rifServiceException
					= new RIFServiceException(
					RIFServiceError.UNABLE_TO_RETRIEVE_ALL_RELEVANT_MAP_AREAS,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return allRelevantMapAreas;
	}

	private String getMapAreaResolutionMappingAreaTableName(
			final Connection connection,
			final Geography geography)
			throws SQLException,
			       RIFServiceException {

		String result;

		SelectQueryFormatter queryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
		queryFormatter.addSelectField("hierarchytable");
		queryFormatter.addFromTable(applySchemaPrefixIfNeeded("rif40_geographies"));
		queryFormatter.addWhereParameter("geography");

		logSQLQuery(
				"getMapAreaResolutionMappingAreaTableName",
				queryFormatter,
				geography.getName());

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
					= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, geography.getName());
			resultSet = statement.executeQuery();

			resultSet.next();
			result = resultSet.getString(1);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return result;
	}

	/**
	 * Gets the geo level lookup table name.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param resolutionLevel the resolution level
	 * @return the geo level lookup table name
	 * @throws RIFServiceException the RIF service exception
	 */
	private String getGeoLevelLookupTableName(
			final Connection connection,
			final Geography geography,
			final String resolutionLevel)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String result;
		try {

			SelectQueryFormatter queryFormatter =
					SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("lookup_table");
			queryFormatter.addFromTable(applySchemaPrefixIfNeeded("rif40_geolevels"));
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");

			logSQLQuery(
					"getGeoLevelLookupTableName",
					queryFormatter,
					geography.getName(),
					resolutionLevel);

			statement = createPreparedStatement(connection, queryFormatter);
			statement.setString(1, geography.getName());
			statement.setString(2, resolutionLevel);
			resultSet = statement.executeQuery();
			connection.commit();

			// This method assumes that geoLevelSelect is valid
			// Therefore, it must be associated with a lookup table
			resultSet.next();

			result = useAppropriateTableNameCase(resultSet.getString(1));
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return result;
	}
}
