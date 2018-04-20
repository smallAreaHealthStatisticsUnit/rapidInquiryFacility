package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.MapArea;
import rifServices.dataStorageLayer.common.MapDataManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

import java.sql.*;
import java.util.ArrayList;


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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final class PGSQLMapDataManager 
	extends PGSQLAbstractSQLManager implements MapDataManager {
	
	//TODO: (DM) class is full of unused methods

	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");	

	// ==========================================
	// Section Properties
	// ==========================================
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL map data manager.
	 *
	 * @param rifServiceStartupOptions the rif service startup options
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	public PGSQLMapDataManager(
		final RIFServiceStartupOptions rifServiceStartupOptions,
		final RIFContextManager sqlRIFContextManager) {

		super(rifServiceStartupOptions);
	}

	// ==========================================
	// Section Accessors and Mutators///////
	// ==========================================?//

	
	@Override
	public ArrayList<MapArea> getAllRelevantMapAreas(
			final Connection connection,
			final Geography geography,
			final AbstractGeographicalArea geographicalArea)
		throws RIFServiceException {
		
		rifLogger.info(this.getClass(), "SQLMapDataManager getAllRelevantAreas!!!!!!!!!!!");
		ArrayList<MapArea> allRelevantMapAreas = new ArrayList<MapArea>();

		GeoLevelSelect geoLevelSelect
			= geographicalArea.getGeoLevelSelect();		
		GeoLevelToMap geoLevelToMap
			= geographicalArea.getGeoLevelToMap();
		
		
		ArrayList<MapArea> selectedMapAreas
			= geographicalArea.getMapAreas();
		
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
			 *    gid,
			 *    level4
			 * FROM
			 *    mapAreaResolutionMappingTableName,  //eg: sahsuland_geography
			 *    geoLevelToMapTableName //eg: sahsuland_level4
			 * WHERE
			 *    level2='01.001' OR  //iteratively read in each map area provided by
			 *    level2='01.002' OR  //by client
			 *    level2='01.003' OR
			 *    ...
			 * 
			 * 
			 * 
			 * 
			 * 			
			 */
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "SELECT DISTINCT");
			queryFormatter.addQueryPhrase(1, geoLevelToMapTableName);			
			queryFormatter.addQueryPhrase(".gid,");
			queryFormatter.addQueryPhrase(geoLevelToMapTableName);			
			queryFormatter.addQueryPhrase(".");			
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());			
			queryFormatter.padAndFinishLine();			
			queryFormatter.addQueryLine(0, "FROM");
			queryFormatter.addQueryPhrase(1, mapAreaResolutionMappingTableName);
			queryFormatter.addQueryPhrase(",");
			queryFormatter.addQueryPhrase(geoLevelToMapTableName);
			queryFormatter.padAndFinishLine();			
			queryFormatter.addQueryLine(0, "WHERE");
			
			queryFormatter.addQueryPhrase(1, geoLevelToMapTableName);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());			
			queryFormatter.addQueryPhrase("=");
			queryFormatter.addQueryPhrase(mapAreaResolutionMappingTableName);
			queryFormatter.addQueryPhrase(".");			
			queryFormatter.addQueryPhrase(geoLevelToMap.getName());			
			
			
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
			while (resultSet.next()) {
				String identifier
					= resultSet.getString(1);
				String name
					= resultSet.getString(2);
				
				MapArea mapArea
					= MapArea.newInstance(
						identifier, 
						identifier, 
						name);
				allRelevantMapAreas.add(mapArea);
				
			}
		}
		catch(SQLException sqlException) {
			logException(sqlException);
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
				
		String result = "";
				
		PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
		queryFormatter.addSelectField("hierarchytable");
		queryFormatter.addFromTable("rif40_geographies");
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
		String result = null;
		try {
		
			PGSQLSelectQueryFormatter queryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("lookup_table");
			queryFormatter.addFromTable("rif40_geolevels");
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");
		
			logSQLQuery(
				"getGeoLevelLookupTableName",
				queryFormatter,
				geography.getName(),
				resolutionLevel);
		
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, geography.getName());
			statement.setString(2, resolutionLevel);
			resultSet = statement.executeQuery();
			connection.commit();
			
			if (resultSet.next() == false) {
				//this method assumes that geoLevelSelect is valid
				//Therefore, it must be associated with a lookup table
				assert false;
			}		
			
			result
				= useAppropriateTableNameCase(resultSet.getString(1));
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}

	
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
