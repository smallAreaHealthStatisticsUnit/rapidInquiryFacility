package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;

import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceError;

import com.sun.rowset.CachedRowSetImpl;

import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.HashMap;

/**
 * Create Covariate Loss Report SQL 
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class CovariateLossReport extends BaseSQLManager {
	
	/**
	 * Constructor
	 *
	 * @param 	options	RIFServiceStartupOptions
	 * @param 	manager	SQLManager
	 */
	public CovariateLossReport(final RIFServiceStartupOptions options) {
		super(options);
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
	 * To do:
	 * - No numerator checks yet
	 * - Support age limits
	 *
	 * @param connection			JDBC Connection
	 * @param studyID				studyID string
	 *
	 * @return HashMap<String, SQLGeneralQueryFormatter>
	 */
	public HashMap<String, SQLGeneralQueryFormatter> getSelectQueryFormatters(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
	
		HashMap<String, SQLGeneralQueryFormatter> getCovariateLossReportHash = new HashMap<>();	
		
		CachedRowSetImpl rif40Studies;
		CachedRowSetImpl rif40Investigations;
		CachedRowSetImpl rif40Covariates;
//		CachedRowSetImpl rif40TablesNumerTab;
		CachedRowSetImpl rif40TablesDenomTab;
		
		try {
			rif40Studies=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_studies", 
				"study_id, extract_table, denom_tab, study_name, description, year_start, year_stop, comparison_geolevel_name"); 	
			rif40Investigations=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_investigations",
				"inv_id, inv_name, inv_description, genders, numer_tab, year_start, year_stop, min_age_group, max_age_group");
				// Assumes one study at present
			rif40Covariates=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_inv_covariates",
				"inv_id, covariate_name, min, max, study_geolevel_name, geography");
				// Assumes one study at present
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} 
		
		try {
			String extractTable=getColumnFromResultSet(rif40Studies, "extract_table");
			String comparisonGeolevelName=getColumnFromResultSet(rif40Studies, "comparison_geolevel_name");
			int studyYearStart=Integer.parseInt(getColumnFromResultSet(rif40Studies, "year_start"));
			int studyYearStop=Integer.parseInt(getColumnFromResultSet(rif40Studies, "year_stop"));
			int genders=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "genders"));
			String denomTab=getColumnFromResultSet(rif40Studies, "denom_tab");
			
			int investigationYearStart=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "year_start"));
			int investigationYearStop=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "year_stop"));
			int investigationMinAgeGroup=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "max_age_group"));
			int investigationMaxAgeGroup=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "min_age_group"));
			String invName=getColumnFromResultSet(rif40Investigations, "inv_name");
// 
// No numerator checks as yet 
//			String numerTab=getColumnFromResultSet(rif40Investigations, "numer_tab");
//			if (numerTab == null) {
//				throw new Exception("numerTab is NULL");
//			}	
			if (denomTab == null) {
				throw new Exception("denomTab is NULL");
			}	
//			rif40TablesNumerTab=getRifViewData(connection, true /* column is a String */, "table_name", numerTab, "rif40_tables",
//				"total_field, age_sex_group_field_name");
//				// Assumes one study at present
			rif40TablesDenomTab=getRifViewData(connection, true /* column is a String */, "table_name", denomTab, "rif40_tables",
				"total_field, age_sex_group_field_name");
				// Assumes one study at present
			String denomTabTotalField=getColumnFromResultSet(rif40TablesDenomTab, "total_field");
//			String numerTabTotalField=getColumnFromResultSet(rif40TablesNumerTab, "total_field");
			String denomTabASGField=getColumnFromResultSet(rif40TablesDenomTab, "age_sex_group_field_name");
//			String numerTabASGField=getColumnFromResultSet(rif40TablesNumerTab, "age_sex_group_field_name");
				
			if (invName == null) {
				throw new Exception("invName is NULL");
			}			
			if (denomTabTotalField == null) {
				throw new Exception("denomTabTotalField is NULL");
			}			
			if (denomTabASGField == null) {
				throw new Exception("denomTabASGField is NULL");
			}			
			if (comparisonGeolevelName == null) {
				throw new Exception("comparisonGeolevelName is NULL");
			}
				
			if (!rif40Covariates.next()) {
				throw new Exception("rif40Covariates ResultSet(): expected 1+ rows, got none");
			}
			
			ResultSetMetaData rif40CovariatesMetaData = rif40Covariates.getMetaData();
			int columnCount = rif40CovariatesMetaData.getColumnCount();
			String covariateName = null;
			String studyGeolevelName = null;
			String studyGeography = null;
			double minValue = 0;
			double maxValue = 0;
			do {
				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					if (rif40CovariatesMetaData.getColumnName(i).equals("covariate_name")) {
						covariateName = rif40Covariates.getString(i);
					}
					else if (rif40CovariatesMetaData.getColumnName(i).equals("geography")) {
						studyGeography = rif40Covariates.getString(i);
					}
					else if (rif40CovariatesMetaData.getColumnName(i).equals("study_geolevel_name")) {
						studyGeolevelName = rif40Covariates.getString(i);
					}
					else if (rif40CovariatesMetaData.getColumnName(i).equals("min")) {
						minValue = rif40Covariates.getDouble(i);
					}
					else if (rif40CovariatesMetaData.getColumnName(i).equals("max")) {
						maxValue = rif40Covariates.getDouble(i);
					}
				}
				
				if (covariateName == null) {
					throw new Exception("covariateName is NULL");
				}
				if (studyGeolevelName == null) {
					throw new Exception("studyGeolevelName is NULL");
				}
				if (studyGeography == null) {
					throw new Exception("studyGeography is NULL");
				}
				
				CachedRowSetImpl rif40Geolevels = getRifViewData(connection, true /* column is a String */, 
					"geolevel_name", studyGeolevelName, 
					"geography", studyGeography, 
					"rif40_geolevels",
					"covariate_table");
				String covariateTableName=getColumnFromResultSet(rif40Geolevels, "covariate_table");			
				if (covariateTableName == null) {
					throw new Exception("covariateTableName is NULL");
				}
				
				SQLGeneralQueryFormatter getStudyCovariateLossReportQueryFormatter = 
					generateStudyAreaCovariateLossReportQueryFormatter(
						studyID,
						covariateName,
						minValue,
						maxValue,
						invName,
						denomTab,
						denomTabTotalField,
						studyGeolevelName,
						denomTabASGField,
						genders,
						studyYearStart,
						studyYearStop,
						covariateTableName,
						extractTable,
						investigationMinAgeGroup,
						investigationMaxAgeGroup);
				getCovariateLossReportHash.put("S: " + covariateName, getStudyCovariateLossReportQueryFormatter);
				SQLGeneralQueryFormatter getComparisonCovariateLossReportQueryFormatter = 
					generateCovariateAreaCovariateLossReportQueryFormatter(
						studyID,
						covariateName,
						minValue,
						maxValue,
						invName,
						denomTab,
						denomTabTotalField,
						studyGeolevelName,
						comparisonGeolevelName,
						denomTabASGField,
						genders,
						studyYearStart,
						studyYearStop,
						covariateTableName,
						extractTable,
						investigationMinAgeGroup,
						investigationMaxAgeGroup);
				getCovariateLossReportHash.put("C: " + covariateName, getComparisonCovariateLossReportQueryFormatter);
			} while (rif40Covariates.next());
		
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version	
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToFetchRifViewData",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage,
					exception);
		} 
		
		return getCovariateLossReportHash;
	}
	
	/** 
	 * Generate Study Area Covariate Loss Report Query Formatter
	 * <p>
     * Example SQL>
	 *
     * WITH study_areas AS (
     * 	SELECT area_id
     * 	  FROM rif40.rif40_study_areas
     * 	 WHERE study_id = 563
     * ), study_summary AS (
     * 		SELECT 'SES' AS covariate_name, 							-* RIF40_INV_COVARIATES covariate_name *-
     * 			   COUNT(DISTINCT(b.year)) AS extractYears,
     *	 		   MIN(b.year) AS extractMinYear,
     * 			   MAX(b.year) AS extractMaxYear,
     * 			   COUNT(DISTINCT(a.area_id)) AS studyMappingGeolevelAreas, 
     *	 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missingStudyDenominatorAreas,  
     *	 		   SUM(b.my_new_investigation) AS studyNumeratorCount, 	-* RIF40_INVESTIGATION inv_name *-
     *	 		   SUM(b.total_pop) AS studyDenominatorCount,  
     *	 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.area_id ELSE NULL END)) AS missingStudyCovariateAreas,
     *	 		   SUM(b.total_pop)-SUM(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.total_pop ELSE 9 END) AS missingStudyDenominatorCovariateCount,  
     *	 		   SUM(b.my_new_investigation)-SUM(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.my_new_investigation ELSE 9 END) AS misingStudyNumeratorCovariateCount 
     * 	 FROM study_areas a 
     * 			LEFT OUTER JOIN rif_studies.s563_extract b ON 
     * 				(a.area_id = b.area_id AND b.study_or_comparison = 'S')
	 *      GROUP BY b.study_or_comparison
     * ), study_denominator_check AS (
     * 		SELECT COUNT(DISTINCT(d1.year)) AS years,
     * 			   MIN(d1.year) AS minYear,
     * 			   MAX(d1.year) AS maxYear,
     * 			   SUM(COALESCE(d1.total, 0)) AS totalPop			-* RIF40_TABLES total_field *-
     * 		  FROM rif40_study_areas s, pop_sahsuland_pop d1 	
     * 			LEFT OUTER JOIN covar_sahsuland_covariates4 c1 ON (	-* Covariates *-
     * 					d1.sahsu_grd_level4 = c1.sahsu_grd_level4	-* RIF40_INV_COVARIATES study geolevel name *-
     * 				AND CASE (3) 									-* RIF40_INVESTIGATION Genders *-
     * 						WHEN d1.age_sex_group THEN TRUE  		-* Both *-
     * 				        WHEN FLOOR(d1.age_sex_group/100) THEN TRUE ELSE FALSE END /* RIF40_TABLES AGE_SEX_GROUP field name *-
     * 				AND c1.year = d1.year)
     * 		 WHERE d1.year BETWEEN 1995	AND 1996					-* investigation year filter *-
     * 		   AND s.area_id  = d1.sahsu_grd_level4	
     * 		   AND s.area_id  IS NOT NULL							-* Exclude NULL geolevel *-
     * 		   AND s.study_id = 563									-* Current study ID *-
     * )
     * SELECT a.*, 
     *        b.years-a.extractYears AS missingYears,
     * 	   b.totalPop-a.studyDenominatorCount AS missingDenominator,
     * 	   b.minYear AS denominatorMinYear,
     * 	   b.maxYear AS denominatorMaxYear
     *   FROM study_summary a, study_denominator_check b;
     * 			
     *  covariate_name | extractyears | extractminyear | extractmaxyear | studymappinggeolevelareas | missingstudydenominatorareas | studynumeratorcount | studydenominatorcount | missingstudycovariateareas | missingstudydenominatorcovariatecount | misingstudynumeratorcovariatecount | missingyears | missingdenominator | denominatorminyear | denominatormaxyear
     * ----------------+--------------+----------------+----------------+---------------------------+------------------------------+---------------------+-----------------------+----------------------------+---------------------------------------+------------------------------------+--------------+--------------------+--------------------+--------------------
     *  SES            |            1 |           1996 |           1996 |                       889 |                            0 |                4230 |               7048116 |                          0 |                                     0 |                                  0 |            1 |            7026120 |               1995 |               1996
     * (1 row)
     *
	 * @param studyID					studyID string
	 * @param covariateName				RIF40_INV_COVARIATES covariate name 
	 * @param minValue					RIF40_INV_COVARIATES covariate minimum Value
	 * @param maxValue					RIF40_INV_COVARIATES covariate maximum Value
	 * @param invName					RIF40_INVESTIGATIONS investigation name
	 * @param denomTab					RIF40_STUDIES denominator table name
	 * @param denomTabTotalField		RIF40_TABLES denominator total field name
	 * @param studyGeolevelName			RIF40_INV_COVARIATES study geolevel name
	 * @param denomTabASGField			RIF40_TABLES denominator age sex group field name
	 * @param genders					RIF40_INVESTIGATIONS genders
	 * @param studyYearStart			RIF40_INVESTIGATIONS year start
	 * @param studyYearStop				RIF40_INVESTIGATIONS year stop
	 * @param covariateTableName		RIF40_GEOEVELS covariate table name
	 * @param extractTable				extractTable extract table name
	 * @param investigationMinAgeGroup	RIF40_INVESTIGATIONS min_age_group
	 * @param investigationMaxAgeGroup	RIF40_INVESTIGATIONS max_age_group
	 *
	 * @return SQLGeneralQueryFormatter
	 */
	private SQLGeneralQueryFormatter generateStudyAreaCovariateLossReportQueryFormatter(
			final String studyID,
			final String covariateName,
			final double minValue,
			final double maxValue,
			final String invName,
			final String denomTab,
			final String denomTabTotalField,
			final String studyGeolevelName,
			final String denomTabASGField,
			final int genders,
			final int studyYearStart,
			final int studyYearStop,
			final String covariateTableName,
			final String extractTable,
			final int investigationMinAgeGroup,
			final int investigationMaxAgeGroup) {
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
		queryFormatter.addQueryLine(0, "WITH study_areas AS (");
		queryFormatter.addQueryLine(0, "	SELECT area_id");
		queryFormatter.addQueryLine(0, "	  FROM rif40.rif40_study_areas");
		queryFormatter.addQueryLine(0, "	 WHERE study_id = " + studyID);
		queryFormatter.addQueryLine(0, "), study_summary AS (");
		queryFormatter.addQueryLine(0, "	SELECT '" + covariateName + "' AS covariate_name, 			/* RIF40_INV_COVARIATES covariate_name */");
		queryFormatter.addQueryLine(0, "		   b.study_or_comparison AS studyOrComparison,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(b.year)) AS extractYears,");
		queryFormatter.addQueryLine(0, "		   MIN(b.year) AS extractMinYear,");
		queryFormatter.addQueryLine(0, "		   MAX(b.year) AS extractMaxYear,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id)) AS studyMappingGeolevelAreas,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missingStudyDenominatorAreas,");
		queryFormatter.addQueryLine(0, "		   SUM(b." + invName + ") AS studyNumeratorCount, 	/* RIF40_INVESTIGATION inv_name */");
		queryFormatter.addQueryLine(0, "		   SUM(b.total_pop) AS studyDenominatorCount,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.area_id ELSE NULL END)) AS missingStudyCovariateAreas,");
		queryFormatter.addQueryLine(0, "		   SUM(b.total_pop)-SUM(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.total_pop ELSE 9 END) AS missingStudyDenominatorCovariateCount,  ");
		queryFormatter.addQueryLine(0, "		   SUM(b." + invName + ")-SUM(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b." + invName + " ELSE 9 END) AS misingStudyNumeratorCovariateCount"); 
		queryFormatter.addQueryLine(0, "	  FROM study_areas a ");
		queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN rif_studies." + extractTable + " b ON");
		queryFormatter.addQueryLine(0, "				(a.area_id = b.area_id AND b.study_or_comparison = 'S')");
		queryFormatter.addQueryLine(0, "     GROUP BY b.study_or_comparison");
		queryFormatter.addQueryLine(0, "), study_denominator_check AS (");
		queryFormatter.addQueryLine(0, "	SELECT COUNT(DISTINCT(d1.year)) AS years,");
		queryFormatter.addQueryLine(0, "			   MIN(d1.year) AS minYear,");
		queryFormatter.addQueryLine(0, "			   MAX(d1.year) AS maxYear,");
		queryFormatter.addQueryLine(0, "			   SUM(COALESCE(d1." + denomTabTotalField + ", 0)) AS totalPop			/* RIF40_TABLES total_field */");
		queryFormatter.addQueryLine(0, "		  FROM rif40_study_areas s, " + denomTab + " d1");
		queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN " + covariateTableName + " c1 ON (	/* Covariates */");
		queryFormatter.addQueryLine(0, "					d1." + studyGeolevelName + " = c1." + studyGeolevelName + "	/* RIF40_INV_COVARIATES study geolevel name */");
		queryFormatter.addQueryLine(0, "				AND CASE (" + genders + ") 									/* RIF40_INVESTIGATION Genders */ ");
		queryFormatter.addQueryLine(0, "						WHEN d1." + denomTabASGField + " THEN TRUE  		/* Both */");
		queryFormatter.addQueryLine(0, "				        WHEN FLOOR(d1." + denomTabASGField + "/100) THEN TRUE ELSE FALSE END /* RIF40_TABLES AGE_SEX_GROUP field name */");
		queryFormatter.addQueryLine(0, "				AND c1.year = d1.year)");
		queryFormatter.addQueryLine(0, "		 WHERE d1.year BETWEEN " + studyYearStart + " AND " + studyYearStop + "					/* investigation year filter */");
		queryFormatter.addQueryLine(0, "		   AND s.area_id  = d1." + studyGeolevelName + "	");
		queryFormatter.addQueryLine(0, "		   AND s.area_id  IS NOT NULL							/* Exclude NULL geolevel */");
		queryFormatter.addQueryLine(0, "		   AND s.study_id = " + studyID + "							/* Current study ID */");
		queryFormatter.addQueryLine(0, ")");
		queryFormatter.addQueryLine(0, "SELECT a.*,");
 		queryFormatter.addQueryLine(0, "       b.years-a.extractYears AS missingYears,");
		queryFormatter.addQueryLine(0, "	   b.totalPop-a.studyDenominatorCount AS missingDenominator,");
		queryFormatter.addQueryLine(0, "	   b.minYear AS denominatorMinYear,");
		queryFormatter.addQueryLine(0, "	   b.maxYear AS denominatorMaxYear");
		queryFormatter.addQueryLine(0, "  FROM study_summary a, study_denominator_check b;");
		
		return queryFormatter;
	}
	

	/** 
	 * Generate Comparison Area Covariate Loss Report Query Formatter
	 * <p>
     * Example SQL>
	 *
     * WITH comparison_areas AS (
     * 		SELECT area_id
     * 		  FROM rif40.rif40_comparison_areas
     * 		 WHERE study_id = 563
     * ), comparison_summary AS (
     * SELECT 'SES' AS covariate_name, 							/- RIF40_INV_COVARIATES covariate_name -/
     * 		   COUNT(DISTINCT(b.year)) AS extractYears,
     * 		   MIN(b.year) AS extractMinYear,
     * 		   MAX(b.year) AS extractMaxYear,
     * 		   COUNT(DISTINCT(a.area_id)) AS comparisonMappingGeolevelAreas, 
     * 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missingComparisonDenominatorAreas,  
     * 		   SUM(b.my_new_investigation) AS comparisonNumeratorCount, 	/- RIF40_INVESTIGATION inv_name -/
     * 		   SUM(b.total_pop) AS comparisonDenominatorCount,  
     * 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.area_id ELSE NULL END)) AS missingStudyCovariateAreas,
     * 		   SUM(b.total_pop)-SUM(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.total_pop ELSE 9 END) AS missingStudyDenominatorCovariateCount,  
     * 		   SUM(b.my_new_investigation)-SUM(CASE WHEN b.ses BETWEEN '1' AND '5' THEN b.my_new_investigation ELSE 9 END) AS misingStudyNumeratorCovariateCount 
     * 	  FROM comparison_areas a 
     * 			LEFT OUTER JOIN rif_studies.s563_extract b ON 
     * 				(a.area_id = b.area_id AND b.study_or_comparison = 'C')
	 *     GROUP BY b.study_or_comparison
     * ), comparison_denominator_check AS (
     * 	SELECT COUNT(DISTINCT(d1.year)) AS years,
     * 			   MIN(d1.year) AS minYear,
     * 			   MAX(d1.year) AS maxYear,
     * 			   SUM(COALESCE(d1.total, 0)) AS totalPop			/- RIF40_TABLES total_field -/
     * 		  FROM rif40_comparison_areas s, pop_sahsuland_pop d1 	
     * 			LEFT OUTER JOIN covar_sahsuland_covariates4 c1 ON (	/- Covariates -/
     * 					d1.sahsu_grd_level4 = c1.sahsu_grd_level4	/- RIF40_INV_COVARIATES study geolevel name -/
     * 				AND CASE (3) 									/- RIF40_INVESTIGATION Genders -/ 
     * 						WHEN d1.age_sex_group THEN TRUE  		/- Both -/
     * 				        WHEN FLOOR(d1.age_sex_group/100) THEN TRUE ELSE FALSE END /* RIF40_TABLES AGE_SEX_GROUP field name -/
     * 				AND c1.year = d1.year)
     * 		 WHERE d1.year BETWEEN 1995	AND 1996					/- investigation year filter -/
     * 		   AND s.area_id  = d1.sahsu_grd_level1					/- Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name -/
     * 		   AND s.area_id  IS NOT NULL							/- Exclude NULL geolevel -/
     * 		   AND s.study_id = 563									/- Current study ID -/
     * )
     * SELECT a.*, 
     *        b.years-a.extractYears AS missingYears,
     * 	   b.totalPop-a.comparisonDenominatorCount AS missingDenominator,
     * 	   b.minYear AS denominatorMinYear,
     * 	   b.maxYear AS denominatorMaxYear
     *   FROM comparison_summary a, comparison_denominator_check b;
     *  covariate_name | extractyears | extractminyear | extractmaxyear | comparisonmappinggeolevelareas | missingcomparisondenominatorareas | comparisonnumeratorcount | comparisondenominatorcount | missingstudycovariateareas | missingstudydenominatorcovariatecount | misingstudynumeratorcovariatecount | missingyears | missingdenominator | denominatorminyear | denominatormaxyear
     * ----------------+--------------+----------------+----------------+--------------------------------+-----------------------------------+--------------------------+----------------------------+----------------------------+---------------------------------------+------------------------------------+--------------+--------------------+--------------------+--------------------
     *  SES            |            1 |           1996 |           1996 |                              1 |0                                  |                     6260 |                   10550510 |                          0 |                                     0 |                                  0 |            1 |           10523088 |               1995 |               1996
     * (1 row)
     *
	 * @param studyID					studyID string
	 * @param covariateName				RIF40_INV_COVARIATES covariate name 
	 * @param minValue					RIF40_INV_COVARIATES covariate minimum Value
	 * @param maxValue					RIF40_INV_COVARIATES covariate maximum Value
	 * @param invName					RIF40_INVESTIGATIONS investigation name
	 * @param denomTab					RIF40_STUDIES denominator table name
	 * @param denomTabTotalField		RIF40_TABLES denominator total field name
	 * @param studyGeolevelName			RIF40_INV_COVARIATES study geolevel name
	 * @param comparisonGeolevelName	extractTable comparison geolevel name
	 * @param denomTabASGField			RIF40_TABLES denominator age sex group field name
	 * @param genders					RIF40_INVESTIGATIONS genders
	 * @param studyYearStart			RIF40_INVESTIGATIONS year start
	 * @param studyYearStop				RIF40_INVESTIGATIONS year stop
	 * @param covariateTableName		RIF40_GEOEVELS covariate table name
	 * @param extractTable				extractTable extract table name
	 * @param investigationMinAgeGroup	RIF40_INVESTIGATIONS min_age_group
	 * @param investigationMaxAgeGroup	RIF40_INVESTIGATIONS max_age_group
	 *
	 * @return SQLGeneralQueryFormatter
	 */
	private SQLGeneralQueryFormatter generateCovariateAreaCovariateLossReportQueryFormatter(
			final String studyID,
			final String covariateName,
			final double minValue,
			final double maxValue,
			final String invName,
			final String denomTab,
			final String denomTabTotalField,
			final String studyGeolevelName,
			final String comparisonGeolevelName,
			final String denomTabASGField,
			final int genders,
			final int studyYearStart,
			final int studyYearStop,
			final String covariateTableName,
			final String extractTable,
			final int investigationMinAgeGroup,
			final int investigationMaxAgeGroup) {
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
		queryFormatter.addQueryLine(0, "WITH comparison_areas AS (");
		queryFormatter.addQueryLine(0, "	SELECT area_id");
		queryFormatter.addQueryLine(0, "	  FROM rif40.rif40_comparison_areas");
		queryFormatter.addQueryLine(0, "	 WHERE study_id = " + studyID);
		queryFormatter.addQueryLine(0, "), comparison_summary AS (");
		queryFormatter.addQueryLine(0, "	SELECT '" + covariateName + "' AS covariate_name, 			/* RIF40_INV_COVARIATES covariate_name */");
		queryFormatter.addQueryLine(0, "		   b.study_or_comparison AS studyOrComparison,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(b.year)) AS extractYears,");
		queryFormatter.addQueryLine(0, "		   MIN(b.year) AS extractMinYear,");
		queryFormatter.addQueryLine(0, "		   MAX(b.year) AS extractMaxYear,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id)) AS comparisonMappingGeolevelAreas,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missingComparisonDenominatorAreas,");
		queryFormatter.addQueryLine(0, "		   SUM(b." + invName + ") AS comparisonNumeratorCount, 	/* RIF40_INVESTIGATION inv_name */");
		queryFormatter.addQueryLine(0, "		   SUM(b.total_pop) AS comparisonDenominatorCount,");
		queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.area_id ELSE NULL END)) AS missingStudyCovariateAreas,");
		queryFormatter.addQueryLine(0, "		   SUM(b.total_pop)-SUM(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.total_pop ELSE 9 END) AS missingStudyDenominatorCovariateCount,");
		queryFormatter.addQueryLine(0, "		   SUM(b." + invName + ")-SUM(CASE WHEN b." + covariateName.toLowerCase() + 
			" BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b." + invName + " ELSE 9 END) AS misingStudyNumeratorCovariateCount"); 
		queryFormatter.addQueryLine(0, "	  FROM comparison_areas a");
		queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN rif_studies." + extractTable + " b ON");
		queryFormatter.addQueryLine(0, "				(a.area_id = b.area_id AND b.study_or_comparison = 'C')");
		queryFormatter.addQueryLine(0, "     GROUP BY b.study_or_comparison");
		queryFormatter.addQueryLine(0, "), comparison_denominator_check AS (");
		queryFormatter.addQueryLine(0, "	SELECT COUNT(DISTINCT(d1.year)) AS years,");
		queryFormatter.addQueryLine(0, "			   MIN(d1.year) AS minYear,");
		queryFormatter.addQueryLine(0, "			   MAX(d1.year) AS maxYear,");
		queryFormatter.addQueryLine(0, "			   SUM(COALESCE(d1." + denomTabTotalField + ", 0)) AS totalPop			/* RIF40_TABLES total_field */");
		queryFormatter.addQueryLine(0, "		  FROM rif40_comparison_areas s, " + denomTab + " d1");
		queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN " + covariateTableName + " c1 ON (	/* Covariates */");
		queryFormatter.addQueryLine(0, "					d1." + studyGeolevelName + " = c1." + studyGeolevelName + "	/* RIF40_INV_COVARIATES study geolevel name */");
		queryFormatter.addQueryLine(0, "				AND CASE (" + genders + ") 									/* RIF40_INVESTIGATION Genders */");
		queryFormatter.addQueryLine(0, "						WHEN d1." + denomTabASGField + " THEN TRUE  		/* Both */");
		queryFormatter.addQueryLine(0, "				        WHEN FLOOR(d1." + denomTabASGField + "/100) THEN TRUE ELSE FALSE END /* RIF40_TABLES AGE_SEX_GROUP field name */");
		queryFormatter.addQueryLine(0, "				AND c1.year = d1.year)");
		queryFormatter.addQueryLine(0, "		 WHERE d1.year BETWEEN " + studyYearStart + " AND " + studyYearStop + "					/* investigation year filter */");
		queryFormatter.addQueryLine(0, "		   AND s.area_id  = d1." + comparisonGeolevelName + "					/* Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name */");
		queryFormatter.addQueryLine(0, "		   AND s.area_id  IS NOT NULL							/* Exclude NULL geolevel */");
//		queryFormatter.addQueryLine(0, "		   AND d1." + denomTabASGField + " BETWEEN " + investigationMinAgeGroup + " AND " + investigationMaxAgeGroup);
		queryFormatter.addQueryLine(0, "		   AND s.study_id = " + studyID + "								/* Current study ID */");
		queryFormatter.addQueryLine(0, ")");
		queryFormatter.addQueryLine(0, "SELECT a.*,"); 
		queryFormatter.addQueryLine(0, "       b.years-a.extractYears AS missingYears,");
		queryFormatter.addQueryLine(0, "	   b.totalPop-a.comparisonDenominatorCount AS missingDenominator,");
		queryFormatter.addQueryLine(0, "	   b.minYear AS denominatorMinYear,");
		queryFormatter.addQueryLine(0, "	   b.maxYear AS denominatorMaxYear");
		queryFormatter.addQueryLine(0, "  FROM comparison_summary a, comparison_denominator_check b;");
		
		return queryFormatter;
	}
  
	/*
	 * Fetch RIF view data for study/table
	 *
	 * @param: connection 		Connection,
	 * @param: columnsAreString Boolean,
	 * @param: columnName1 		String,
	 * @param: columnValue1 	String,
	 * @param: columnName2 		String,
	 * @param: columnValue2 	String,
	 * @param: tableName 		String,
	 * @param: columnList 		String
	 *
	 * @returns: CachedRowSetImpl
	 */
	private CachedRowSetImpl getRifViewData(
			final Connection connection,
			final boolean columnsAreString,
			final String columnName1,
			final String columnValue1,
			final String columnName2,
			final String columnValue2,
			final String tableName,
			final String columnList)
			throws RIFServiceException {
		SQLGeneralQueryFormatter rifViewDataQueryFormatter = new SQLGeneralQueryFormatter();			
		CachedRowSetImpl cachedRowSet;
		try {
			rifViewDataQueryFormatter.addQueryLine(0, "SELECT " + columnList);
			rifViewDataQueryFormatter.addQueryLine(0, "  FROM rif40." + tableName.toLowerCase());
			rifViewDataQueryFormatter.addQueryLine(0, " WHERE " + columnName1 + " = ?");
			rifViewDataQueryFormatter.addQueryLine(0, "   AND " + columnName2 + " = ?");
			if (columnsAreString) {
				String[] params = new String[2];
				params[0]=columnValue1;
				params[1]=columnValue2;
				
				cachedRowSet = createCachedRowSet(connection, rifViewDataQueryFormatter,
					"getRifViewData", params);
			}
			else {
				int[] params = new int[2];
				params[0]=Integer.parseInt(columnValue1);
				params[1]=Integer.parseInt(columnValue2);
				
				cachedRowSet = createCachedRowSet(connection, rifViewDataQueryFormatter,
					"getRifViewData", params);
			}
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetRifViewData",
					tableName,
					columnValue1,
					columnList);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage,
					exception);
		} 
		
		return cachedRowSet;
	}

	/*
	 * Fetch RIF view data for study/table
	 *
	 * @param: connection 		Connection,
	 * @param: columnsAreString Boolean,
	 * @param: columnName1 		String,
	 * @param: columnValue1 	String,
	 * @param: tableName 		String,
	 * @param: columnList 		String
	 *
	 * @returns: CachedRowSetImpl
	 */	
	private CachedRowSetImpl getRifViewData(
			final Connection connection,
			final boolean columnsAreString,
			final String columnName1,
			final String columnValue1,
			final String tableName,
			final String columnList)
			throws RIFServiceException {
		SQLGeneralQueryFormatter rifViewDataQueryFormatter = new SQLGeneralQueryFormatter();			
		CachedRowSetImpl cachedRowSet;
		try {
			rifViewDataQueryFormatter.addQueryLine(0, "SELECT " + columnList);
			rifViewDataQueryFormatter.addQueryLine(0, "  FROM rif40." + tableName.toLowerCase());
			rifViewDataQueryFormatter.addQueryLine(0, " WHERE " + columnName1 + " = ?");
			if (columnsAreString) {
				String[] params = new String[1];
				params[0]=columnValue1;
				
				cachedRowSet = createCachedRowSet(connection, rifViewDataQueryFormatter,
					"getRifViewData", params);
			}
			else {
				int[] params = new int[1];
				params[0]=Integer.parseInt(columnValue1);
				
				cachedRowSet = createCachedRowSet(connection, rifViewDataQueryFormatter,
					"getRifViewData", params);
			}
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetRifViewData",
					tableName,
					columnValue1,
					columnList);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage,
					exception);
		} 
		
		return cachedRowSet;
	}	

}