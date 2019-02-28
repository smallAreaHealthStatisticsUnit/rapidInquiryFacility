package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.collections.IteratorUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.UpdateQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.datastorage.common.RifLocale;
import org.sahsu.rif.services.rest.RIFResultTableJSONGenerator;
import org.sahsu.rif.services.datastorage.common.RIFTiles;
import org.sahsu.rif.services.graphics.SlippyTile;
import org.sahsu.rif.services.graphics.RIFPdfTiles;
import org.sahsu.rif.services.graphics.RIFTilesException;

import java.io.IOException;

public class ResultsQueryManager extends BaseSQLManager {

	private static String lineSeparator = System.getProperty("line.separator");

	private RIFServiceStartupOptions options;
	
	public ResultsQueryManager(final RIFServiceStartupOptions options) {

		super(options);
		this.options = options;

		FunctionCallerQueryFormatter getTilesQueryFormatter = new FunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);
	}
					
	String getMapBackground(
			final Connection connection,
			final Geography geography)
					throws RIFServiceException {
		String result="{}";
		
		SelectQueryFormatter getMapBackgroundQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getMapBackgroundQueryFormatter1.setDatabaseSchemaName("rif40");
		getMapBackgroundQueryFormatter1.addSelectField("map_background");
		getMapBackgroundQueryFormatter1.addFromTable("rif40_geographies");
		getMapBackgroundQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getMapBackground", getMapBackgroundQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;

		try {
			statement1 = connection.prepareStatement(getMapBackgroundQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
				
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getMapBackground query 1; expected 1 row, got none for geography: " + geography.getName().toUpperCase());
			}
			String mapBackground = resultSet1.getString(1);
			if (mapBackground == null || mapBackground.length() == 0) {
				mapBackground="NONE"; // Default is: OpenStreetMap Mapnik
			}			
			connection.commit();
	
			JSONObject getMapBackground = new JSONObject();
			getMapBackground.put("geography", geography.getName().toUpperCase());
			getMapBackground.put("mapBackground", mapBackground);
			result=getMapBackground.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetMapBackground",
					geography.getName().toUpperCase());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}	
	}	
					
	String getSelectState(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
						
		String result="{}";
		
		SelectQueryFormatter getSelectStateQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getSelectStateQueryFormatter1.setDatabaseSchemaName("rif40");
		getSelectStateQueryFormatter1.addSelectField("select_state");
		getSelectStateQueryFormatter1.addFromTable("rif40_studies");
		getSelectStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("getSelectState", getSelectStateQueryFormatter1, studyID);
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;		

		try {
			statement1 = connection.prepareStatement(getSelectStateQueryFormatter1.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getSelectState query 1; expected 1 row, got none for study_id: " + studyID);
			}
			String selectStateStr = resultSet1.getString(1);
			JSONObject selectState;
			JSONObject getSelectState = new JSONObject();
			getSelectState.put("study_id", studyID);
			if (selectStateStr != null && selectStateStr.length() > 0) {
				try {
					selectState = new JSONObject(selectStateStr);
					getSelectState.put("select_state", selectState);
				}
				catch (JSONException jsonException) {
					throw new RIFServiceException(
						RIFServiceError.JSON_PARSE_ERROR,
						jsonException.getMessage() + "; in: select_state=" + selectStateStr);
				}
			}
			connection.commit();
			result=getSelectState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetSelectState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}
	}	
	
	/** 
	 * Get the covariate loss report for a study
	 * <p>	 
	 * This will LEFT OUTER JOIN the numerator and covariate tables, filter by the study and comparison areas respectively, and 
	 * filter the covariates the max and min ranges defined for the covariate to produce:
	 * Study or Comparison areas (S or C);
	 * Covariate name;
	 * Number of areas at mapping (covariate table) geolevel;
	 * Number of areas that join the numerator to the study or Comparison area for the study defined year and age sex group range;
	 * Number of areas that join the covariate to the study or Comparison area for defined covariate max/min limits;
	 *
     * Returns two x number of covariates records for a risk analysis study using covariates; otherwise raise an error;
	 *
	 * Returned JSON:
	 * {
	 * 	"S": [{
	 * 			"missingyears": "0",
	 * 			"covariateTableDescription": "socio-economic status",
	 * 			"extractminyear": "1995",
	 * 			"studydenominatorcount": 1622176,
	 * 			"extractyears": "2",
	 * 			"denominatorminyear": 1995,
	 * 			"missingstudydenominatorareas": "0",
	 * 			"missingdenominator": 0,
	 * 			"missingstudycovariateareas": "21",
	 * 			"denominatormaxyear": 1996,
	 * 			"extractmaxyear": "1996",
	 * 			"covariatetablename": "COVAR_SAHSULAND_COVARIATES4",
	 * 			"studymappinggeolevelareas": "82",
	 * 			"covariatename": "SES",
	 * 			"missingstudynumeratorcovariatecount": 198,
	 * 			"studyorcomparison": "S",
	 * 			"missingstudydenominatorcovariatecount": 305656,
	 *		    "covariatetablename": "COVAR_SAHSULAND_COVARIATES4",
	 *	   	    "agesexgroupfilter": "age_sex_group BETWEEN 100 AND 221",
	 *		    "icdfilter": "icd LIKE 'C33%' AND icd LIKE 'C340%' AND icd LIKE 'C341%' AND icd LIKE 'C342%' AND icd LIKE 'C343%' AND icd LIKE 'C348%' AND icd LIKE 'C349%' AND icd LIKE '1620%' AND icd LIKE '1622%' AND icd LIKE '1623%' AND icd LIKE '1624%' AND icd LIKE '1625%' AND icd LIKE '1628%' AND icd LIKE '1629%',
	 * 			"studynumeratorcount": 1088
	 * 		}
	 * 	],
	 * 	"C": [{
	 * 			"comparisonnumeratorcount": 12892,
	 * 			"covariateTableDescription": "socio-economic status",
	 * 			"extractminyear": "1995",
	 * 			"missingcomparisondenominatorareas": "0",
	 * 			"extractyears": "2",
	 * 			"denominatorMaxYear": 1996,
	 * 			"missingstudycovariateareas": "0",
	 * 			"denominatorMinYear": 1995,
	 * 			"missingDenominator": 0,
	 * 			"comparisonmappinggeolevelareas": "1",
	 * 			"extractmaxyear": "1996",
	 * 			"covariatetablename": "COVAR_SAHSULAND_COVARIATES4",
	 * 			"covariatename": "SES",
	 * 			"missingstudynumeratorcovariatecount": 1458,
	 * 			"studyorcomparison": "C",
	 * 			"missingstudydenominatorcovariatecount": 2888240,
	 * 			"missingYears": "0",
	 *		    "covariatetablename": "COVAR_SAHSULAND_COVARIATES4",
	 *	   	    "agesexgroupfilter": "age_sex_group BETWEEN 100 AND 221",
	 *		    "icdfilter": "icd LIKE 'C33%' AND icd LIKE 'C340%' AND icd LIKE 'C341%' AND icd LIKE 'C342%' AND icd LIKE 'C343%' AND icd LIKE 'C348%' AND icd LIKE 'C349%' AND icd LIKE '1620%' AND icd LIKE '1622%' AND icd LIKE '1623%' AND icd LIKE '1624%' AND icd LIKE '1625%' AND icd LIKE '1628%' AND icd LIKE '1629%',
	 * 			"comparisondenominatorcount": 2.1073598E7
	 * 		}
	 * 	]
	 * }
	 * </p>
	 *
	 * @param connection			JDBC Connection
	 * @param studyID				studyID string
	 *
	 * @return JSONObject as a string
	 *
	 * @throws RIFServiceException RIF error
	 * @throws RIFSQLException RIF SQL error
     */		
	String getCovariateLossReport(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
						
		String result="{}";
		
		CovariateLossReport covariateLossReport = new CovariateLossReport(options);
		HashMap<String, SQLGeneralQueryFormatter> getCovariateLossReportHash = 
			covariateLossReport.getSelectQueryFormatters(connection, studyID);
			
		JSONObject covariateLossReportJson = new JSONObject();
		
		for (String key : getCovariateLossReportHash.keySet()) {
			PreparedStatement statement1 = null;
			ResultSet resultSet1 = null;
			String sqlQueryText = null;
			try {
				SQLGeneralQueryFormatter getCovariateLossReportFormatter1 = getCovariateLossReportHash.get(key);
				sqlQueryText = logSQLQuery("getCovariateLossReport", getCovariateLossReportFormatter1);

				statement1 = connection.prepareStatement(getCovariateLossReportFormatter1.generateQuery());
				resultSet1 = statement1.executeQuery();
				if (!resultSet1.next()) {
					throw new RIFServiceException(
						RIFServiceError.DATABASE_QUERY_FAILED,
						"getHomogeneity query 1; expected 1+ rows, got NONE for study_id: " + studyID);
				}
				do {
					
					ResultSetMetaData rsmd = resultSet1.getMetaData();
					int columnCount = rsmd.getColumnCount();
					// The column count starts from 1
                    JSONObject studyOrComparisonCovariate = new JSONObject();
					String studyOrComparison = null;
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i); 
						String value = resultSet1.getString(i);
						String columnType = rsmd.getColumnTypeName(i);
						if (name.equals("study_or_comparison")) {
							studyOrComparison=value;
						}
						
						if (value != null && (
								 columnType.equals("integer") || 
								 columnType.equals("bigint") || 
								 columnType.equals("int4") ||
								 columnType.equals("int") ||
								 columnType.equals("smallint"))) {
							try { // Use normal decimal formatting - will cause confusion with coordinates
								Long longVal=Long.parseLong(resultSet1.getString(i));
								studyOrComparisonCovariate.put(jsonCapitalise(name), String.valueOf(longVal));
							}
							catch (Exception exception) {	
								throw new RIFServiceException(
									RIFServiceError.DATABASE_DATATYPE_ERROR,
									"Unable to parseLong(" + 
									columnType + "): " + resultSet1.getString(i) + " for study_id: " + studyID, exception);
							}
						}
						else if (value != null && (
								 columnType.equals("float") || 
								 columnType.equals("float8") || 
								 columnType.equals("double precision") ||
								 columnType.equals("numeric"))) {
							try { // Ditto
								Double doubleVal=Double.parseDouble(resultSet1.getString(i));
								studyOrComparisonCovariate.put(jsonCapitalise(name), String.valueOf(doubleVal));
							}
							catch (Exception exception) {
								throw new RIFServiceException(
									RIFServiceError.DATABASE_DATATYPE_ERROR,
									"Unable to parseDouble(" + 
									columnType + "): " + resultSet1.getString(i) + " for study_id: " + studyID, exception);
							}
						}
						else {
							studyOrComparisonCovariate.put(jsonCapitalise(name), value);
						}
					} /* End of for column loop */

                    String covariateName;
                    try {
                        covariateName=studyOrComparisonCovariate.getString("covariateName");
                        String covariateTableName=studyOrComparisonCovariate.getString("covariateTableName");
                        String covariateTableDescription=null;
                        if (covariateName != null && covariateTableName != null) {
                            covariateTableDescription=getColumnComment(connection, "rif_data", 
                                covariateTableName.toLowerCase(), covariateName.toLowerCase());
                            if (covariateTableDescription != null) {
                                studyOrComparisonCovariate.put("covariateTableDescription", covariateTableDescription);
                            }
                        }
                        else {
                            throw new RIFServiceException(
                                RIFServiceError.DATABASE_QUERY_FAILED,
                                "NULL covariateName or covariateTableName; covariate: " + studyOrComparisonCovariate.toString());
                        }
                    }
                    catch (Exception exception) {
                        throw new RIFServiceException(
                            RIFServiceError.DATABASE_QUERY_FAILED,
                                "JSON/Database error in covariateTableDescription; covariate: " + studyOrComparisonCovariate.toString(),
                                exception);
                    }
					
					if (studyOrComparison == null) {
						throw new RIFServiceException(
							RIFServiceError.DATABASE_QUERY_FAILED,
							"NULL studyOrComparison; covariate: " + studyOrComparisonCovariate.toString());
					}
					else {
                        JSONObject covariate;
                        if (covariateLossReportJson.has(covariateName)) {
                            covariate = covariateLossReportJson.getJSONObject(covariateName);
                        }
                        else {
                            covariate = new JSONObject();
                            covariateLossReportJson.put(covariateName, covariate);
                        }
                        if (covariate.has(studyOrComparison)) {
                            throw new RIFServiceException(
                                RIFServiceError.DATABASE_QUERY_FAILED,
                                "covariate has \"" + studyOrComparison + "\": " + covariate.toString());
                        }
                        else {
                            covariate.put(studyOrComparison, studyOrComparisonCovariate);
                        }
                    }                        
				} while (resultSet1.next()); /* Covariate S/C areas list */
                
				connection.commit();
			} catch(RIFServiceException rifServiceException) {
				throw rifServiceException;
			} catch(SQLException sqlException) {
				//Record original exception, throw sanitised, human-readable version
				throw new RIFSQLException(this.getClass(), sqlException, statement1, sqlQueryText);
			}  finally {
				//Cleanup database resources
				SQLQueryUtility.close(statement1);
				SQLQueryUtility.close(resultSet1);
			}
		} /* End of hash iterator */	
				
		result=covariateLossReportJson.toString();
		return result;
	}		
    
	/** 
	 * Get the Risk Graph data for a study
	 * <p>	 
	 * Example JSON:
     * {
	 *     "males": [{
     *         "studyAreas": "528",
     *          "relativeRisk": "0.906289",
     *          "bandId": "1.0",
     *          "adjusted": "1.0",
     *          "expected": "134.614918",
     *          "genders": "1.0",
     *          "lower95": "0.758929",
     *          "observed": "122.0",
     *          "upper95": "1.082262"
     *         },
     *         {
     *          "studyAreas": "836",
     *          "relativeRisk": "0.956601",
     *          "bandId": "2.0",
     *          "adjusted": "1.0",
     *          "expected": "160.986693",
     *          "genders": "1.0",
     *          "lower95": "0.816841",
     *          "observed": "154.0",
     *          "upper95": "1.120273"
     *         },
     *         {
     *          "studyAreas": "2244",
     *          "relativeRisk": "1.07338",
     *          "bandId": "3.0",
     *          "adjusted": "1.0",
     *          "expected": "398.740546",
     *          "genders": "1.0",
     *          "lower95": "0.976356",
     *          "observed": "428.0",
     *          "upper95": "1.180045"
     *         }
     *        ],
	 *        females: [ ... ],
	 *        both: [ ... ]
	 * }		
	 * 
     * Example SQL:     
	 * WITH b AS (
	 *     SELECT band_id, sex AS genders,
	 *            AVG(exposure_value) AS avg_exposure_value,
	 * 		   AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source,
	 * 		   COUNT(area_id) AS study_areas
 	 *      FROM rif_studies.s196_extract
	 * 	 WHERE study_or_comparison = 'S'
	 *      GROUP BY band_id, sex
	 * 	UNION
	 *     SELECT band_id, 3 AS genders,
	 *            AVG(exposure_value) AS avg_exposure_value,
	 * 		   AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source,
	 *            COUNT(area_id) AS study_areas
	 *       FROM rif_studies.s196_extract
	 * 	 WHERE study_or_comparison = 'S'
	 *      GROUP BY band_id
	 * ), a AS (
	 *     SELECT a.genders, a.band_id, a.adjusted, observed, expected, lower95, upper95, relative_risk,
	 * 	       b.avg_exposure_value,
	 * 		   b.avg_distance_from_nearest_source,
	 *            b.study_areas
	 *       FROM rif_studies.s196_map a
	 * 		LEFT OUTER JOIN b ON (a.band_id = b.band_id AND a.genders = b.genders)
	 * )
	 * SELECT * FROM a
	 *  ORDER BY 1, 2, 3
	 * </p>
	 *
	 * @param connection			JDBC Connection
	 * @param studyID				studyID string
	 *
	 * @return JSONObject as a string
	 *
	 * @throws RIFServiceException RIF error
	 * @throws RIFSQLException RIF SQL error
     */		
	String getRiskGraph(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
						
		String result="{}";
		
        SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
        
        PreparedStatement statement1 = null;
        ResultSet resultSet1 = null;
        String sqlQueryText = null;
        
        JSONObject riskGraphJson = new JSONObject();
        JSONArray riskGraphMales = new JSONArray();
        JSONArray riskGraphFemales = new JSONArray();
        JSONArray riskGraphBoth = new JSONArray();
        try {
            
            boolean hasExposureValue=false;
            boolean hasDistanceFromNearestSource=false;
            try {
                hasDistanceFromNearestSource=doesColumnExist(connection, 
                    "rif_studies", "s" + studyID + "_extract", "distance_from_nearest_source"); 
                hasExposureValue=doesColumnExist(connection, 
                    "rif_studies", "s" + studyID + "_extract", "exposure_value");
            }
            catch (Exception exception) {            
                throw new RIFServiceException(
                    RIFServiceError.DATABASE_QUERY_FAILED,
                    "getRiskGraph unable to determine columns present in extract for study_id: " + studyID, exception);
            }

            queryFormatter.addQueryLine(0, "WITH b AS (");
            queryFormatter.addQueryLine(0, "    SELECT band_id, sex AS genders,");
            if (hasExposureValue) {
                queryFormatter.addQueryLine(0, "           AVG(exposure_value) AS avg_exposure_value,"); 
            }
            if (hasDistanceFromNearestSource) {
                queryFormatter.addQueryLine(0, "		   AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source,");
            }
            queryFormatter.addQueryLine(0, "		   COUNT(area_id) AS study_areas");
            queryFormatter.addQueryLine(0, "      FROM rif_studies.s" + studyID + "_extract");
            queryFormatter.addQueryLine(0, "	 WHERE study_or_comparison = 'S'");
            queryFormatter.addQueryLine(0, "     GROUP BY band_id, sex");
            queryFormatter.addQueryLine(0, "	UNION");
            queryFormatter.addQueryLine(0, "    SELECT band_id, 3 AS genders,");
            if (hasExposureValue) {
                queryFormatter.addQueryLine(0, "           AVG(exposure_value) AS avg_exposure_value,"); 
            }
            if (hasDistanceFromNearestSource) {
                queryFormatter.addQueryLine(0, "		   AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source,");
            }
            queryFormatter.addQueryLine(0, "           COUNT(area_id) AS study_areas");
            queryFormatter.addQueryLine(0, "      FROM rif_studies.s" + studyID + "_extract");
            queryFormatter.addQueryLine(0, "	 WHERE study_or_comparison = 'S'");
            queryFormatter.addQueryLine(0, "     GROUP BY band_id");
            queryFormatter.addQueryLine(0, "), a AS (");
            queryFormatter.addQueryLine(0, "    SELECT a.genders, a.band_id, a.adjusted, observed, expected, lower95, upper95, relative_risk,"); 
            if (hasExposureValue) {
                queryFormatter.addQueryLine(0, "	       b.avg_exposure_value,");
            }
            if (hasDistanceFromNearestSource) {
                queryFormatter.addQueryLine(0, "		   b.avg_distance_from_nearest_source,");
            }
            queryFormatter.addQueryLine(0, "           b.study_areas");
            queryFormatter.addQueryLine(0, "      FROM rif_studies.s" + studyID + "_map a");
            queryFormatter.addQueryLine(0, "		LEFT OUTER JOIN b ON (a.band_id = b.band_id AND a.genders = b.genders)");
            queryFormatter.addQueryLine(0, ")");
            queryFormatter.addQueryLine(0, "SELECT * FROM a");
            queryFormatter.addQueryLine(0, " ORDER BY 1, 2, 3");
        
            sqlQueryText = logSQLQuery("getRiskGraph", queryFormatter);

            statement1 = connection.prepareStatement(queryFormatter.generateQuery());
            resultSet1 = statement1.executeQuery();
            if (!resultSet1.next()) {
                throw new RIFServiceException(
                    RIFServiceError.DATABASE_QUERY_FAILED,
                    "getRiskGraph query 1; expected 1+ rows, got NONE for study_id: " + studyID);
            }
            do {
                
                ResultSetMetaData rsmd = resultSet1.getMetaData();
                int columnCount = rsmd.getColumnCount();
                // The column count starts from 1
                JSONObject riskGraphRow = new JSONObject();
                int genders=-1;
                
                for (int i = 1; i <= columnCount; i++ ) {
                    String name = rsmd.getColumnName(i); 
                    String value = resultSet1.getString(i);
                    String columnType = rsmd.getColumnTypeName(i);
                    if (name.equals("genders")) {
                        genders=resultSet1.getInt(i);
                    }
                    
                    if (value != null && (
                             columnType.equals("integer") || 
                             columnType.equals("bigint") || 
                             columnType.equals("int4") ||
                             columnType.equals("int") ||
                             columnType.equals("smallint"))) {
                        try { // Use normal decimal formatting - will cause confusion with coordinates
                            Long longVal=Long.parseLong(resultSet1.getString(i));
                            riskGraphRow.put(jsonCapitalise(name), String.valueOf(longVal));
                        }
                        catch (Exception exception) {	
                            throw new RIFServiceException(
                                RIFServiceError.DATABASE_DATATYPE_ERROR,
                                "Unable to parseLong(" + 
                                columnType + "): " + resultSet1.getString(i) + " for study_id: " + studyID, exception);
                        }
                    }
                    else if (value != null && (
                             columnType.equals("float") || 
                             columnType.equals("float8") || 
                             columnType.equals("double precision") ||
                             columnType.equals("numeric"))) {
                        try { // Ditto
                            Double doubleVal=Double.parseDouble(resultSet1.getString(i));
                            riskGraphRow.put(jsonCapitalise(name), String.valueOf(doubleVal));
                        }
                        catch (Exception exception) {
                            throw new RIFServiceException(
                                RIFServiceError.DATABASE_DATATYPE_ERROR,
                                "Unable to parseDouble(" + 
                                columnType + "): " + resultSet1.getString(i) + " for study_id: " + studyID, exception);
                        }
                    }
                    else {
                        riskGraphRow.put(jsonCapitalise(name), value);
                    }
                } /* End of for column loop */
              
                switch (genders) {
                    case 1: /* Males */
                        riskGraphMales.put(riskGraphRow);
                        break;
                    case 2: /* Females */
                        riskGraphFemales.put(riskGraphRow);
                        break;
                    case 3: /* Both */
                        riskGraphBoth.put(riskGraphRow);
                        break;
                    default:
                        throw new RIFServiceException(
                            RIFServiceError.DATABASE_DATATYPE_ERROR,
                            "Invalid value for genders: " + genders);
                }
            } while (resultSet1.next()); /* riskGraphRow */
            
            connection.commit();
        } catch(RIFServiceException rifServiceException) {
            throw rifServiceException;
        } catch(SQLException sqlException) {
            //Record original exception, throw sanitised, human-readable version
            throw new RIFSQLException(this.getClass(), sqlException, statement1, sqlQueryText);
        }  finally {
            //Cleanup database resources
            SQLQueryUtility.close(statement1);
            SQLQueryUtility.close(resultSet1);
        }
        
        riskGraphJson.put("females", riskGraphFemales);
        riskGraphJson.put("males", riskGraphMales);
        riskGraphJson.put("both", riskGraphBoth);
        
		result=riskGraphJson.toString();            
		return result;
	}		
    
	/** 
	 * Get the rif40_homogeneity data for a study
	 * <p>	 
	 * Returned JSON:
	 * "adjusted": {
	 *      "females": {
	 * 	    	"linearityP": 0,
	 * 	    	"linearityChi2": 0,
	 * 	    	"explt5": 0,
	 * 	    	"homogeneityDof": 2,
	 * 	    	"homogeneityP": 1.95058679437527E-4,
	 * 	    	"homogeneityChi2": 17.084420248951
	 * 	    },
	 * 	    "males": {
	 * 	    	"linearityP": 0,
	 * 	    	"linearityChi2": 0,
	 * 	    	"explt5": 0,
	 * 	    	"homogeneityDof": 2,
	 * 	    	"homogeneityP": 0.178163986654135,
	 * 	    	"homogeneityChi2": 3.45010175892807
	 * 	    },
	 * 	    "both": {
	 * 	    	"linearityP": 0,
	 * 	    	"linearityChi2": 0,
	 * 	    	"explt5": 0,
	 * 	    	"homogeneityDof": 2,
	 * 	    	"homogeneityP": 0.00337359835580779,
	 * 	    	"homogeneityChi2": 11.3835506858045
	 *      }
	 * },
	 * "unadjusted": {
     * ...
	 * }
	 * </p>
	 *
	 * @param connection			JDBC Connection
	 * @param studyID				studyID string
	 *
	 * @return JSONObject as a string
	 *
	 * @throws RIFServiceException RIF error
	 * @throws RIFSQLException RIF SQL error
     */		
	String getHomogeneity(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
						
		String result="{}";
		
		SelectQueryFormatter getHomogeneityQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getHomogeneityQueryFormatter1.setDatabaseSchemaName("rif40");
		getHomogeneityQueryFormatter1.addSelectField("genders");
		getHomogeneityQueryFormatter1.addSelectField("homogeneity_dof");
		getHomogeneityQueryFormatter1.addSelectField("homogeneity_chi2");
		getHomogeneityQueryFormatter1.addSelectField("homogeneity_p");
		getHomogeneityQueryFormatter1.addSelectField("linearity_chi2");
		getHomogeneityQueryFormatter1.addSelectField("linearity_p");
		getHomogeneityQueryFormatter1.addSelectField("explt5");
		getHomogeneityQueryFormatter1.addSelectField("adjusted");
		
		getHomogeneityQueryFormatter1.addFromTable("rif40_homogeneity");
		getHomogeneityQueryFormatter1.addWhereParameter("study_id");

		String sqlQueryText = logSQLQuery("getHomogeneity", getHomogeneityQueryFormatter1, studyID);
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;		

		try {
			statement1 = connection.prepareStatement(getHomogeneityQueryFormatter1.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			resultSet1 = statement1.executeQuery();
		
			JSONObject homogeneity = new JSONObject();
			JSONObject adjusted = new JSONObject();
			JSONObject unadjusted = new JSONObject();
			for (int i=0; i<3; i++) {
				if (!resultSet1.next() || i >= 6) {
					throw new RIFServiceException(
						RIFServiceError.DATABASE_QUERY_FAILED,
						"getHomogeneity query 1; expected 3-6 rows, got " + i + " for study_id: " + studyID);
				}
				int genders = resultSet1.getInt(1);
				double homogeneityDof = resultSet1.getDouble(2);
				double homogeneityChi2 = resultSet1.getDouble(3);
				double homogeneityP = resultSet1.getDouble(4);
				double linearityChi2 = resultSet1.getDouble(5);
				double linearityP = resultSet1.getDouble(6);
				double explt5 = resultSet1.getDouble(7);
				int adjustedValue = resultSet1.getInt(8);
				
				JSONObject homogeneityItem = new JSONObject();
				homogeneityItem.put("homogeneityDof", homogeneityDof);
				homogeneityItem.put("homogeneityChi2", homogeneityChi2);
				homogeneityItem.put("homogeneityP", homogeneityP);
				homogeneityItem.put("linearityChi2", linearityChi2);
				homogeneityItem.put("linearityP", linearityP);
				homogeneityItem.put("explt5", explt5);
                if (adjustedValue == 1) {
                    if (genders == 1) {
                        adjusted.put("males", homogeneityItem);
                    }
                    else if (genders == 2) {
                        adjusted.put("females", homogeneityItem);
                    }
                    else if (genders == 3) {
                        adjusted.put("both", homogeneityItem);
                    }
                    else {
                        String errorMessage
                                = RIFServiceMessages.getMessage(
                                "sqlResultsQueryManager.invalidHomogeneityGender",
                                Integer.toString(genders),
                                studyID);
                        throw new RIFServiceException(
                                RIFServiceError.DATABASE_QUERY_FAILED,
                                errorMessage);			
                    }
                }
                else {

                    if (genders == 1) {
                        unadjusted.put("males", homogeneityItem);
                    }
                    else if (genders == 2) {
                        unadjusted.put("females", homogeneityItem);
                    }
                    else if (genders == 3) {
                        unadjusted.put("both", homogeneityItem);
                    }
                    else {
                        String errorMessage
                                = RIFServiceMessages.getMessage(
                                "sqlResultsQueryManager.invalidHomogeneityGender",
                                Integer.toString(genders),
                                studyID);
                        throw new RIFServiceException(
                                RIFServiceError.DATABASE_QUERY_FAILED,
                                errorMessage);			
                    }
                }                    
			}
			homogeneity.put("adjusted", adjusted);
			homogeneity.put("unadjusted", unadjusted);
            
			connection.commit();
			result=homogeneity.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			throw new RIFSQLException(this.getClass(), sqlException, statement1, sqlQueryText);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}
	}		
	
	String getPrintState(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
		String result="{}";
		
		SelectQueryFormatter getPrintStateQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPrintStateQueryFormatter1.setDatabaseSchemaName("rif40");
		getPrintStateQueryFormatter1.addSelectField("print_state");
		getPrintStateQueryFormatter1.addFromTable("rif40_studies");
		getPrintStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("getPrintState", getPrintStateQueryFormatter1, studyID);
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;		

		try {
			statement1 = connection.prepareStatement(getPrintStateQueryFormatter1.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPrintState query 1; expected 1 row, got none for study_id: " + studyID);
			}
			String printStateStr = resultSet1.getString(1);
			JSONObject getPrintState = new JSONObject();	
			getPrintState.put("study_id", studyID);
			JSONObject printState;
			if (printStateStr != null && printStateStr.length() > 0) {
				try {
					printState = new JSONObject(printStateStr);
					getPrintState.put("print_state", printState);
				}
				catch (JSONException jsonException) {
					throw new RIFServiceException(
						RIFServiceError.JSON_PARSE_ERROR, 
						jsonException.getMessage() + "; in: print_state=" + printStateStr);
				}
			}
			connection.commit();
			result=getPrintState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPrintState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}
	}		
					
	String setPrintState(
			final Connection connection,
			final String studyID,
			final String printStateText)
					throws RIFServiceException {
		String result="{}";
		
		UpdateQueryFormatter setPrintStateQueryFormatter1 =
				UpdateQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		setPrintStateQueryFormatter1.setDatabaseSchemaName("rif40");
		if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) { // Supports JSON natively
			setPrintStateQueryFormatter1.addUpdateField("print_state", "JSON");
		}
		else { // SQL Server doesn't yet
			setPrintStateQueryFormatter1.addUpdateField("print_state");
		}		
		setPrintStateQueryFormatter1.setUpdateTable("rif40_studies");
		setPrintStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("setPrintState", setPrintStateQueryFormatter1, printStateText, studyID);
	
		PreparedStatement statement1 = null;
		int rc;		

		try {
			statement1 = connection.prepareStatement(setPrintStateQueryFormatter1.generateQuery());
			statement1.setString(1, printStateText);
			statement1.setInt(2, Integer.parseInt(studyID));
			rc = statement1.executeUpdate();
		
			if (rc != 1) { 
				throw new RIFServiceException(
					RIFServiceError.DATABASE_UPDATE_FAILED,
					"setPrintState query 1; expected 1 row, got none for rif40_studies.study_id: " + studyID + " update");
			}
			connection.commit();
	
			JSONObject setPrintState = new JSONObject();		
			setPrintState.put("study_id", studyID);
			setPrintState.put("print_state", printStateText);
			result=setPrintState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToSetPrintState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
		}
	}
					
	String getPostalCodeCapabilities(
			final Connection connection, 
			final Geography geography) throws RIFServiceException {
		String result="{}";

		SelectQueryFormatter getPostalCodeCapabilitiesQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPostalCodeCapabilitiesQueryFormatter1.setDatabaseSchemaName("rif40");
		getPostalCodeCapabilitiesQueryFormatter1.addSelectField("postal_population_table");
		getPostalCodeCapabilitiesQueryFormatter1.addSelectField("srid");
		getPostalCodeCapabilitiesQueryFormatter1.addFromTable("rif40_geographies");
		getPostalCodeCapabilitiesQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getPostalCodeCapabilities", getPostalCodeCapabilitiesQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;
		
		try {
			statement1 = connection.prepareStatement(getPostalCodeCapabilitiesQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPostalCodeCapabilities query 1; expected 1 row, got none for geography: " + geography.getName().toUpperCase());
			}
			String postalPopulationTable = resultSet1.getString(1);
			int srid = resultSet1.getInt(2);
			
			if (resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPostalCodeCapabilities query 1; expected 1 row, got >1 for geography: " + geography.getName().toUpperCase());
			}	

			if (srid == 0) {
				throw new Exception("getPostalCodeCapabilities no/invalid srid defined for geography: " + geography.getName().toUpperCase());
			}
	
			if (postalPopulationTable != null && postalPopulationTable.length() > 0) {
				boolean hasXcoordinate=doesColumnExist(connection, 
					"rif_data", postalPopulationTable.toLowerCase(), "xcoordinate");
				boolean hasYcoordinate=doesColumnExist(connection, 
					"rif_data", postalPopulationTable.toLowerCase(), "ycoordinate");

				if (hasXcoordinate == false || hasYcoordinate == false) {
					throw new Exception("getPostalCodeCapabilities X/Y coordinate columns not found in geography: " + geography.getName().toUpperCase() +
						" postal population table: " + postalPopulationTable + 
						"; hasXcoordinate: " + hasXcoordinate + 
						"; hasYcoordinate: " + hasYcoordinate);
				} 
			}			
			
			JSONObject getPostalCodeCapabilities = new JSONObject();
			getPostalCodeCapabilities.put("geography", geography.getName().toUpperCase());
			if (postalPopulationTable != null && postalPopulationTable.length() > 0) {
				getPostalCodeCapabilities.put("postalPopulationTable", postalPopulationTable);
			}
			getPostalCodeCapabilities.put("srid", srid);
			result=getPostalCodeCapabilities.toString();	
			
			connection.commit();
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCodeCapabilities",
					geography.getName().toUpperCase());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} catch(Exception exception) {
			
			logException(exception);
			
			JSONObject getPostalCodeCapabilities = new JSONObject();
			getPostalCodeCapabilities.put("geography", geography.getName().toUpperCase());
			getPostalCodeCapabilities.put("error", RIFResultTable.quote(exception.getMessage()));
			result=getPostalCodeCapabilities.toString();	
			
			try {
				connection.commit();
			} catch(SQLException sqlException) {
				logSQLException(sqlException);
			}
			
			return result;
			
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}	
		
	}
			
	String getPostalCodes(
			final Connection connection, 
			final Geography geography,
			final String postcode,
			final Locale locale) throws RIFServiceException {
				
		RifLocale rifLocale = new RifLocale(locale);
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();
		
		String cleanPostcode=postcode;
		String result="{}";
		
		SelectQueryFormatter getPostalCodesQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPostalCodesQueryFormatter1.setDatabaseSchemaName("rif40");
		getPostalCodesQueryFormatter1.addSelectField("postal_population_table");
		getPostalCodesQueryFormatter1.addSelectField("srid");
		getPostalCodesQueryFormatter1.addFromTable("rif40_geographies");
		getPostalCodesQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getPostalCodes", getPostalCodesQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;

		try {
			statement1 = connection.prepareStatement(getPostalCodesQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new Exception("getPostalCodes query 1; expected 1 row, got none");
			}
			String postalPopulationTable = resultSet1.getString(1);
			int srid = resultSet1.getInt(2);
			Double xcoordinate=null;
			Double ycoordinate=null;
			
			if (resultSet1.next()) {
				throw new Exception("getPostalCodes query 1; expected 1 row, got >1");
			}
			if (srid == 27700) {
				cleanPostcode=postcode.toUpperCase();
				String outwardCode=null;
				if (cleanPostcode.length() > 3) {
					outwardCode=cleanPostcode.substring(cleanPostcode.length()-3, cleanPostcode.length()); 	// Last 3 characters
				}		
				if (outwardCode == null) {			
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has no outwardCode for geography: " + 
						geography.getName().toUpperCase() + 
						"\"}}";
					connection.commit();
					
					return result;
				}
				outwardCode=outwardCode.trim();
				if (outwardCode.length() != 3) {			
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has null/wrong size outwardCode (" + outwardCode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"\"}}";
					connection.commit();
					
					return result;
				}
				String inwardCode=null;
				if (cleanPostcode.length() > 4) {
					inwardCode=cleanPostcode.substring(0, cleanPostcode.length()-4);	// Character 0 to last 3
				}
				if (inwardCode == null) {
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has no inwardCode for geography: " + 
						geography.getName().toUpperCase() + 
						"; outwardCode; " + outwardCode +
						"\"}}";
					connection.commit();
					
					return result;
				}		
				inwardCode=String.format("%-4s", 									// Pad to 4 characters
					inwardCode.trim()); 											// Trim white space						
				if (inwardCode.length() != 4) {		
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has wrong sized inwardCode (" + inwardCode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"; outwardCode; " + outwardCode +
						"\"}}";
					connection.commit();
					
					return result;
				}
				cleanPostcode = inwardCode + outwardCode;
				if (cleanPostcode.length() != 7) { // error in cleaning logic!
					throw new Exception("getPostalCodes wrong sized cleanPostcode (" + cleanPostcode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"; postcode: " + postcode +
						"; inwardCode; " + inwardCode +
						"; outwardCode; " + outwardCode);
				}		
			}
			
			if (postalPopulationTable == null) {
				throw new Exception("getPostalCodes no postal population table defined for geography: " + geography.getName().toUpperCase() +
					"; postcode: " + postcode);
			}
	
			boolean hasXcoordinate=doesColumnExist(connection, 
				"rif_data", postalPopulationTable.toLowerCase(), "xcoordinate");
			boolean hasYcoordinate=doesColumnExist(connection, 
				"rif_data", postalPopulationTable.toLowerCase(), "ycoordinate");

			if (hasXcoordinate == false || hasYcoordinate == false) {
				throw new Exception("getPostalCodes X/Y coordinate columns not found in geography: " + geography.getName().toUpperCase() +
					" postal population table: " + postalPopulationTable + 
					"; hasXcoordinate: " + hasXcoordinate + 
					"; hasYcoordinate: " + hasYcoordinate);
			} 
		
			SelectQueryFormatter getPostalCodesQueryFormatter2 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			
			getPostalCodesQueryFormatter2.setDatabaseSchemaName("rif_data");
			getPostalCodesQueryFormatter2.addSelectField("*");
			getPostalCodesQueryFormatter2.addFromTable(postalPopulationTable.toLowerCase());
			getPostalCodesQueryFormatter2.addWhereParameter("postcode");
			
			logSQLQuery("getPostalCodes", getPostalCodesQueryFormatter2, cleanPostcode);
			
			statement2 = connection.prepareStatement(getPostalCodesQueryFormatter2.generateQuery());
			statement2.setString(1, cleanPostcode);
			resultSet2 = statement2.executeQuery();
			String[] columnNames;
			String[][] data;
			RIFResultTable.ColumnDataType[] columnDataTypes;
		
			if (resultSet2.next()) {
				int rowCount=0;
				ResultSetMetaData rsmd = resultSet2.getMetaData();
				int columnCount = rsmd.getColumnCount();
							
				columnNames = new String[2];
				columnNames[0] = "Name";
				columnNames[1] = "Value";
				
				data = new String[columnCount][2];
				columnDataTypes = new RIFResultTable.ColumnDataType[2];
				columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[1] = RIFResultTable.ColumnDataType.JSON; // Everything will be JSON valid

				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i); 
					String value = null;
					if (resultSet2.getString(i) != null) {
						value = RIFResultTable.quote(resultSet2.getString(i)); // JSON Stringify
					}
					String columnType = rsmd.getColumnTypeName(i);
					
					if (columnType.equals("timestamp") ||
						columnType.equals("timestamptz") ||
						columnType.equals("datetime")) {
							Timestamp dateTimeValue=resultSet2.getTimestamp(i, calendar);
							value=RIFResultTable.quote(df.format(dateTimeValue));
					}
					else if (value != null && (
							 columnType.equals("integer") || 
							 columnType.equals("bigint") || 
							 columnType.equals("int4") ||
							 columnType.equals("int") ||
							 columnType.equals("smallint"))) {
						try { // Use normal decimal formatting - will cause confusion with coordinates
							Long longVal=Long.parseLong(resultSet2.getString(i));
							if (name.equals("xcoordinate")) {
								xcoordinate=new Double(longVal);
							}
							if (name.equals("ycoordinate")) {
								ycoordinate=new Double(longVal);
							}
//							value=RIFResultTable.quote(NumberFormat.getNumberInstance(locale).format(longVal));
						}
						catch (Exception exception) {
							rifLogger.error(this.getClass(), "Unable to parseLong(" + 
								columnType + "): " + resultSet2.getString(i),
								exception);
							throw exception;
						}
					}
					else if (value != null && (
							 columnType.equals("json"))) {
							value = resultSet2.getString(i); // JSON already
					}
					else if (value != null && (
							 columnType.equals("float") || 
							 columnType.equals("float8") || 
							 columnType.equals("double precision") ||
							 columnType.equals("numeric"))) {
						try { // Ditto
							Double doubleVal=Double.parseDouble(resultSet2.getString(i));
							if (name.equals("xcoordinate")) {
								xcoordinate=new Double(doubleVal);
							}
							if (name.equals("ycoordinate")) {
								ycoordinate=new Double(doubleVal);
							}
//							value=RIFResultTable.quote(NumberFormat.getNumberInstance(locale).format(doubleVal));
						}
						catch (Exception exception) {
							rifLogger.error(this.getClass(), "Unable to parseDouble(" + 
								columnType + "): " + resultSet2.getString(i),
								exception);
							throw exception;
						}
					}
					
					if (value != null) {
						data[i-1][0] = name;
						data[i-1][1] = value;
					}
				} // For loop
				rifLogger.info(getClass(), "get postcode: " + cleanPostcode + 
					" for geography: " + geography.getName().toUpperCase() +
					"; srid: " + srid);
				
				RIFResultTable results = new RIFResultTable();	
					
				results.setColumnProperties(columnNames, columnDataTypes);
				results.setData(data);
				
				JSONObject additionalTableJson = new JSONObject();
				additionalTableJson.put("postalCode", cleanPostcode);
				if (xcoordinate != null && ycoordinate != null) {
					additionalTableJson.put("xcoordinate", xcoordinate);
					additionalTableJson.put("ycoordinate", ycoordinate);
				}
				
				RIFResultTableJSONGenerator rifResultTableJSONGenerator =
						new RIFResultTableJSONGenerator();
				result = rifResultTableJSONGenerator.writeResultTable(results, additionalTableJson);
			}
			else {		
				result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
					"\"warning\": " + 
					"\"Postal code: \\\"" + cleanPostcode + "\\\" for geography: " + geography.getName().toUpperCase() + " is invalid\"}}";
			}
				
			connection.commit();
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCode",
					geography.getName().toUpperCase(),
					cleanPostcode);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCode",
					geography.getDisplayName(),
					cleanPostcode);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}			
			
	}

	RIFResultTable getTileMakerCentroids(
			final Connection connection, 
			final Geography geography,
			final GeoLevelSelect geoLevelSelect) throws RIFServiceException {
				
		boolean hasPopulationWeightedCentroids=false;
		
		try {
			hasPopulationWeightedCentroids=doesColumnExist(connection, 
				"rif_data", "lookup_" + geoLevelSelect.getName().toLowerCase(), "population_weighted_centroid");
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in doesColumnExist()",
				exception);			
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetCentroids",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}				
		
		SelectQueryFormatter getTileMakerCentroidsQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getTileMakerCentroidsQueryFormatter.setDatabaseSchemaName("rif_data");
		getTileMakerCentroidsQueryFormatter.addSelectField(geoLevelSelect.getName().toLowerCase());
		getTileMakerCentroidsQueryFormatter.addSelectField("areaname");
		if (hasPopulationWeightedCentroids) {
			getTileMakerCentroidsQueryFormatter.addSelectField("geographic_centroid");
			getTileMakerCentroidsQueryFormatter.addSelectField("population_weighted_centroid");
		}
		else {
			getTileMakerCentroidsQueryFormatter.addSelectField("geographic_centroid");
		}
		getTileMakerCentroidsQueryFormatter.addFromTable("lookup_" + geoLevelSelect.getName().toLowerCase());

		logSQLQuery("getTileMakerCentroids", getTileMakerCentroidsQueryFormatter);
								
		PreparedStatement resultCounterStatement = null;
		PreparedStatement statement = null;
		ResultSet resultCounterSet;
		ResultSet resultSet = null;

		try {
			//Count the number of results first
			resultCounterStatement = connection.prepareStatement(getTileMakerCentroidsQueryFormatter.generateQuery());
			resultCounterSet = resultCounterStatement.executeQuery();

			int totalNumberRowsInResults = 0;
			while (resultCounterSet.next()) {
				totalNumberRowsInResults++;
			}

			//get the results
			statement = connection.prepareStatement(getTileMakerCentroidsQueryFormatter.generateQuery());

			RIFResultTable results = new RIFResultTable();

			String[] columnNames;
			String[][] data;
			RIFResultTable.ColumnDataType[] columnDataTypes;
			if (hasPopulationWeightedCentroids) {
				columnNames = new String[6];
				data = new String[totalNumberRowsInResults][6];
				columnDataTypes = new RIFResultTable.ColumnDataType[6];
			}
			else {
				columnNames = new String[4];
				data = new String[totalNumberRowsInResults][4];
				columnDataTypes = new RIFResultTable.ColumnDataType[4];
			}
			
			columnNames[0] = "id";
			columnNames[1] = "name";
			columnNames[2] = "x";
			columnNames[3] = "y";
			if (hasPopulationWeightedCentroids) {
				columnNames[4] = "pop_x";
				columnNames[5] = "pop_y";
			}

			columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;
			if (hasPopulationWeightedCentroids) {
				columnDataTypes[4] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[5] = RIFResultTable.ColumnDataType.TEXT;
			}

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
				
				if (hasPopulationWeightedCentroids) {
					String pop_weighted_coords = resultSet.getString(4);
					if (pop_weighted_coords == null) {
						data[ithRow][4] = null;
						data[ithRow][5] = null;	
					}
					else {
						pop_weighted_coords=pop_weighted_coords.split(":")[2];
						String pop_x = pop_weighted_coords.split(",")[0];
						String pop_y = pop_weighted_coords.split(",")[1];
						pop_x = pop_x.replaceAll("[^0-9?!\\.-]","");
						pop_y = pop_y.replaceAll("[^0-9?!\\.-]","");
						data[ithRow][4] = pop_x;
						data[ithRow][5] = pop_y;	
					}					
				}
				ithRow++;
			}

			results.setColumnProperties(columnNames, columnDataTypes);
			results.setData(data);
			connection.commit();

			return results;
		} catch(SQLException sqlException) {
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
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}

	public String getTileMakerTiles(
			final Connection connection, 
			final Geography geography,
			final GeoLevelSelect geoLevelSelect, 
			final Integer zoomlevel, 
			final Integer x,
			final Integer y,
			String tileType) throws RIFServiceException, RIFTilesException, SQLException {
		
		String result = null;
		RIFTiles rifTiles = new RIFTiles(options);
		RIFTilesCache rifTilesCache = new RIFTilesCache(options);
		RIFPdfTiles rifPdfTiles = new RIFPdfTiles(options);
		SlippyTile slippyTile = new SlippyTile(zoomlevel, x, y); // Will raise RIFTilesException is x/y/zoomlevel are invalid
		if (tileType == null) {
			tileType="topojson";
		}
		else if (tileType.equals("geojson")) {
			result=rifTilesCache.getCachedGeoJsonTile(geography.getName().toLowerCase(), slippyTile, geoLevelSelect.getName().toLowerCase());
			if (result != null) {
				return result;
			}
		}
 		else if (tileType.equals("png")) {	
			result=rifTilesCache.getCachedPngTile(geography.getName().toLowerCase(), slippyTile, geoLevelSelect.getName().toLowerCase());
			if (result != null) {
				return result; // In base64
			}
		}

		HashMap<String, String> hmap = rifTiles.getTopoJsonTile(connection, geography.getName().toUpperCase(), 
			geoLevelSelect.getName().toUpperCase(), slippyTile);
		result=hmap.get("topoJSON");
		try {
			SlippyTile parentTile=null;
			if (result == null) { // Not found
				if (slippyTile.getZoomlevel() > 0) {
					parentTile = slippyTile.getParentTile(); // Will raise RIFTilesException is no parent
				}
				
				if (tileType.equals("topojson") || parentTile == null) {	
					String tileDoesNotExistError="Tile does not exist for geography: " + geography.getName().toUpperCase() +
					   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + 
					   "; slippyTile: " + slippyTile.toString() + lineSeparator + 
					   "parentTile: " + (parentTile != null ? parentTile.toString() : "no parent");
					throw new RIFTilesException(new Exception(tileDoesNotExistError), slippyTile);
				}
				else { // Find valid parent with topoJSON
					do {
						hmap = rifTiles.getTopoJsonTile(connection, geography.getName().toUpperCase(), 
							geoLevelSelect.getName().toUpperCase(), parentTile);
						result=hmap.get("topoJSON");
						if (result == null) { // Not found
							if (parentTile != null && parentTile.getZoomlevel() > 0) {
								parentTile = parentTile.getParentTile(); // Will raise RIFTilesException is no parent
							}	
							else {
								parentTile=null;
							}	
						}						
					} while (result == null && parentTile != null);
							
					if (result == null) { // Not found
							String tileDoesNotExistError="Tile does not exist for geography: " + geography.getName().toUpperCase() +
							   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + 
							   "; slippyTile: " + slippyTile.toString() + lineSeparator + 
							   "parentTile: " + (parentTile != null ? parentTile.toString() : "no parent");
							throw new RIFTilesException(new Exception(tileDoesNotExistError), slippyTile);
					}
					else {
						rifLogger.info(getClass(), "Using parentTile: " + parentTile.toString() +
						   "; for slippyTile: " + slippyTile.toString() +
						   "; length: " + result.length());
					}
				}
			}
			else {
				rifLogger.info(getClass(), "get tile for geography: " + geography.getName().toUpperCase() +
										   "; tileType: " + tileType +
										   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + 
										   "; slippyTile: " + slippyTile.toString() +
										   "; length: " + result.length());	
			}
			
			if (tileType.equals("topojson")) {		
				return result;
			}
			else if (tileType.equals("geojson") || tileType.equals("png")) {	
				if (result != null && result.length() > 0 && 
					!result.equals(rifTiles.getNullTopoJSONTile()) /* Null TopoJSON tile */) {
					try {
						
						JSONObject tileTopoJson = new JSONObject(result);
						
						boolean addBoundingBoxToTile=false;
						if (tileType.equals("geojson")) {
							addBoundingBoxToTile=true;
						}
						JSONObject tileGeoJson = rifTiles.topoJson2geoJson(connection, 
							tileTopoJson, 
							geography.getName().toUpperCase(),
							slippyTile, hmap, geoLevelSelect.getName().toUpperCase(), addBoundingBoxToTile);
						
						if (tileType.equals("png")) {	
							result = rifPdfTiles.geoJson2png(tileGeoJson, geography.getName().toUpperCase(), slippyTile, 
								geoLevelSelect.getName().toUpperCase());
							if (result == null) {
								throw new RIFServiceException(
									RIFServiceError.JSON_PARSE_ERROR,
									"Unable to generate " + geoLevelSelect.getName().toUpperCase() + " PNG tile");
							}
						}
						else {
							result = tileGeoJson.toString();
						}
						rifLogger.info(getClass(), 
							"topoJson2geoJson tile for geography: " + geography.getName().toUpperCase() +
							"; tileType: " + tileType +
							"; geolevel: " + geoLevelSelect.getName().toUpperCase() + lineSeparator + 
						    "; slippyTile: " + slippyTile.toString() +
							"; length: " + result.length());
				
						return result;
					}
					catch (JSONException jsonException) {
						throw new RIFServiceException(
							RIFServiceError.JSON_PARSE_ERROR,
							jsonException.getMessage() + "; in: topojson[0-300]=" + result.substring(1, 300));
					}
				}
				else if (tileType.equals("png") && result != null && result.length() > 0 && 
					result.equals(rifTiles.getNullTopoJSONTile() /* Null TopoJSON tile */)) {
									
					try {				
						SlippyTile nullSlippyTile = new SlippyTile(0, 0, 0);
						result=rifTilesCache.getCachedPngTile("NULL", nullSlippyTile, "NULL");
						if (result == null) {
							JSONObject nullTileGeoJson = new JSONObject(rifTiles.getNullGeoJSONTile());
							result = rifPdfTiles.geoJson2png(nullTileGeoJson, "NULL" /* Geography */, nullSlippyTile, "NULL" /* Geolevel name */);
							
							if (result == null) {
								throw new RIFServiceException(
									RIFServiceError.JSON_PARSE_ERROR,
									"Unable to generate NULL PNG tile");
							}
							else {
								rifLogger.info(getClass(), "Generated NULL PNG tile");
							}
								
						}
						return result; // In base64
					}
					catch (JSONException jsonException) {
						throw new RIFServiceException(
							RIFServiceError.JSON_PARSE_ERROR,
							jsonException.getMessage() + "; in generate NULL PNG tile");
					}
				}
				else if (tileType.equals("geojson") && result != null && result.length() > 0 && 
					result.equals(rifTiles.getNullTopoJSONTile() /* Null TopoJSON tile */)) {
									
					try {		
						JSONArray bboxJson = slippyTile.tile2boundingBox();		
						result = rifTiles.getNullGeoJSONTile(bboxJson); // Add bounding box for debug purposes
						return result; 
					}
					catch (JSONException jsonException) {
						throw new RIFServiceException(
							RIFServiceError.JSON_PARSE_ERROR,
							jsonException.getMessage() + "; in generate NULL GeoJSON tile");
					}
				}
				else {
					return result;
				}
			}
			else {
				throw new RIFServiceException(RIFServiceError.INVALID_TILE_TYPE, "Invalid tileType: " + tileType);
			}
			
		} catch(RIFTilesException rifTilesException) {
			throw rifTilesException;
		} catch(RIFServiceException exception) { // Also catch RIFSQLException super class
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTiles",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
					
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage, exception);
		} 
	}
	
	//get 'global' geography attribute table
				/* Instead of the topoJSON tile returned by getTileMakerTiles... it returns:
					data {
						attributes: [{
							area_id,
							name,
							band
						}, ...
						]
					}
				 */			
	public String getTileMakerAttributes(
			final Connection connection, final Geography geography,
			final GeoLevelSelect geoLevelSelect) throws RIFServiceException {

		//STEP 1: get the lookup table name
		/*
		SELECT lookup_table, lookup_desc_column
		  FROM [sahsuland_dev].[rif40].[rif40_geolevels]
		 WHERE geography     = 'EWS2011'
		   AND geolevel_name = 'MSOA2011';
		*/

		SelectQueryFormatter getLookupTableQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getLookupTableQueryFormatter.setDatabaseSchemaName(SCHEMA_NAME);
		getLookupTableQueryFormatter.addSelectField("a", "lookup_table");
		getLookupTableQueryFormatter.addSelectField("a", "lookup_desc_column");
		getLookupTableQueryFormatter.addFromTable("rif40_geolevels a");
		getLookupTableQueryFormatter.addWhereParameter("geography");
		getLookupTableQueryFormatter.addWhereParameter("geolevel_name");

		logSQLQuery(
				"getTileMakerAttributes",
				getLookupTableQueryFormatter,
				geography.getName().toUpperCase(),
				geoLevelSelect.getName().toUpperCase());

		//For tile table name
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		//For map tiles
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;

		try {

			statement = connection.prepareStatement(getLookupTableQueryFormatter.generateQuery());
			statement.setString(1, geography.getName().toUpperCase());
			statement.setString(2, geoLevelSelect.getName().toUpperCase());

			resultSet = statement.executeQuery();
			resultSet.next();

			//This is the tile table name for this geography
			String myLookupTable = "rif_data." + resultSet.getString(1);
			String myLookupDescName = resultSet.getString(2);

			SelectQueryFormatter getTileMakerAttributesQueryFormatter
					= new MSSQLSelectQueryFormatter();

			//STEP 2: get the tiles
			/*
				SELECT msoa2011, areaname
				FROM [rif_data].lookup_msoa2011
				ORDER BY 1;
			*/

			getTileMakerAttributesQueryFormatter.addSelectField("a", geoLevelSelect.getName().toLowerCase());
			getTileMakerAttributesQueryFormatter.addSelectField("a", myLookupDescName.toLowerCase());
			getTileMakerAttributesQueryFormatter.addFromTable(myLookupTable + " a");
			getTileMakerAttributesQueryFormatter.addQueryLine(0, "   ORDER BY 1");

			logSQLQuery(
					"getTileMakerAttributes",
					getTileMakerAttributesQueryFormatter);

			statement2 = connection.prepareStatement(getTileMakerAttributesQueryFormatter.generateQuery());

			resultSet2 = statement2.executeQuery();
			int rowCount=0;
			
			JSONObject attributes = new JSONObject();
			JSONArray attributesArray = new JSONArray();
			String result="";
			if (resultSet2.next()) {

				do {	
					JSONObject attributesData = new JSONObject();
					rowCount++;				
					String areaId = resultSet2.getString(1);
					String areaName = resultSet2.getString(2);
					
					attributesData.put("area_id", areaId);
					attributesData.put("name", areaName);
					attributesArray.put(attributesData);
				} while (resultSet2.next());
				
				attributes.put("attributes", attributesArray);
				result=attributes.toString();
				
				rifLogger.info(getClass(), "get tile attributes for geography: " + geography.getName().toUpperCase() +
										   "; geolevel: " + geoLevelSelect.getName().toUpperCase() +
										   "; rows: " + rowCount + 
										   "; length: " + result.length());
			}
			else {
				throw new Exception("get tile attributes for geography: " + geography.getName().toUpperCase() +
				   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + " failed; no rows returned");
			}
 
			connection.commit();
			return result;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTileAttributes",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);	
		} catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTileAttributes",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}
	}	
}
