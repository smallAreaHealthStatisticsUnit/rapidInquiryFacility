package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;

import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceError;

import javax.sql.rowset.CachedRowSet;

import java.sql.ResultSetMetaData;
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
     * - Numerator checks (validate extract numerator counts)
     *
     * Check result set column names capitalisation on SQL server
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

        CachedRowSet rif40Studies;
        CachedRowSet rif40Investigations;
        CachedRowSet rif40Covariates;
		CachedRowSet rif40TablesNumerTab;
        CachedRowSet rif40TablesDenomTab;

        try {
            rif40Studies=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_studies",
                    "study_id, extract_table, denom_tab, study_name, description, year_start, year_stop, comparison_geolevel_name");
            rif40Investigations=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_investigations",
                    "inv_id, inv_name, inv_description, genders, numer_tab, year_start, year_stop, min_age_group, max_age_group");
            // Assumes one study at present
            rif40Covariates=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_inv_covariates",
                    "inv_id, covariate_name, min, max, study_geolevel_name, geography");
            // Assumes one study at present
        } catch(Exception exception) {
            //Record original exception, throw sanitised, human-readable version
            String errorMessage
                    = RIFServiceMessages.getMessage(
                    "sqlResultsQueryManager.unableToGetRifViewData",
                    studyID);
            throw new RIFServiceException(
                    RIFServiceError.DATABASE_QUERY_FAILED,
                    errorMessage,
                    exception);
        }

        String icdFilter=createIcdFilter(connection, studyID);
        
        try {
            String extractTable=getColumnFromResultSet(rif40Studies, "extract_table");
            String comparisonGeolevelName=getColumnFromResultSet(rif40Studies, "comparison_geolevel_name");
//			int studyYearStart=Integer.parseInt(getColumnFromResultSet(rif40Studies, "year_start"));
//			int studyYearStop=Integer.parseInt(getColumnFromResultSet(rif40Studies, "year_stop"));
            int genders=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "genders"));
            String denomTab=getColumnFromResultSet(rif40Studies, "denom_tab");

            int investigationYearStart=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "year_start"));
            int investigationYearStop=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "year_stop"));
            int investigationMinAgeGroup=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "min_age_group"));
            int investigationMaxAgeGroup=Integer.parseInt(getColumnFromResultSet(rif40Investigations, "max_age_group"));
            String invName=getColumnFromResultSet(rif40Investigations, "inv_name");
// 
// No numerator checks as yet 
//
			String numerTab=getColumnFromResultSet(rif40Investigations, "numer_tab");
			if (numerTab == null) {
				throw new Exception("numerTab is NULL");
			}	
            if (denomTab == null) {
                throw new Exception("denomTab is NULL");
            }
			rif40TablesNumerTab=getRifViewData(connection, true /* column is a String */, "table_name", numerTab, "rif40_tables",
				"total_field, age_sex_group_field_name");
            rif40TablesDenomTab=getRifViewData(connection, true /* column is a String */, "table_name", denomTab, "rif40_tables",
                    "total_field, age_sex_group_field_name");
            // Assumes one study at present
            String denomTabTotalField=getColumnFromResultSet(rif40TablesDenomTab, "total_field");
			String numerTabTotalField=getColumnFromResultSet(rif40TablesNumerTab, "total_field");
            String denomTabASGField=getColumnFromResultSet(rif40TablesDenomTab, "age_sex_group_field_name");
			String numerTabASGField=getColumnFromResultSet(rif40TablesNumerTab, "age_sex_group_field_name");

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

                CachedRowSet rif40Geolevels = getRifViewData(connection, true /* column is a String */,
                        "geolevel_name", studyGeolevelName,
                        "geography", studyGeography,
                        "rif40_geolevels",
                        "covariate_table");

                String covariateTableName=getColumnFromResultSet(rif40Geolevels, "covariate_table");
                if (covariateTableName == null) {
                    throw new Exception("covariateTableName is NULL");
                }
                int investigationMinAgeSexGroup=0;
                int investigationMaxAgeSexGroup=221;
                switch (genders) {
                       case 1: /* Males */
                            investigationMinAgeSexGroup=100+investigationMinAgeGroup;
                            investigationMaxAgeSexGroup=100+investigationMaxAgeGroup;
                            break;
                       case 2: /* Females */
                            investigationMinAgeSexGroup=200+investigationMinAgeGroup;
                            investigationMaxAgeSexGroup=200+investigationMaxAgeGroup;
                            break;
                       case 3: /* Both */
                            investigationMinAgeSexGroup=100+investigationMinAgeGroup;
                            investigationMaxAgeSexGroup=200+investigationMaxAgeGroup;
                            break;
                       default:
                            throw new Exception("rif40Covariates ResultSet(): invalid gender: " + genders);
                }
                SQLGeneralQueryFormatter getStudyCovariateLossReportQueryFormatter =
                        generateCovariateLossReportQueryFormatter(
                                studyID,
                                "S",
                                covariateName,
                                minValue,
                                maxValue,
                                invName,
                                numerTab,
                                denomTab,
                                denomTabTotalField,
                                numerTabTotalField,
                                studyGeolevelName,
                                comparisonGeolevelName,
                                denomTabASGField,
                                numerTabASGField,
                                investigationYearStart,
                                investigationYearStop,
                                covariateTableName,
                                extractTable,
                                investigationMinAgeSexGroup,
                                investigationMaxAgeSexGroup,
                                icdFilter);
                getCovariateLossReportHash.put("S: " + covariateName, getStudyCovariateLossReportQueryFormatter);
                SQLGeneralQueryFormatter getComparisonCovariateLossReportQueryFormatter =
                        generateCovariateLossReportQueryFormatter(
                                studyID,
                                "C",
                                covariateName,
                                minValue,
                                maxValue,
                                invName,
                                numerTab,
                                denomTab,
                                denomTabTotalField,
                                numerTabTotalField,
                                studyGeolevelName,
                                comparisonGeolevelName,
                                denomTabASGField,
                                numerTabASGField,
                                investigationYearStart,
                                investigationYearStop,
                                covariateTableName,
                                extractTable,
                                investigationMinAgeSexGroup,
                                investigationMaxAgeSexGroup,
                                icdFilter);
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
     * Create ICD filter
     *
     * @param connection			JDBC Connection
     * @param studyID				studyID string
     *     
     * @return String
     */
    private String createIcdFilter(
        final Connection connection,
        final String studyID) throws RIFServiceException {

        String icdFilter = null;
        CachedRowSet rif40InvConditions = null;
        try {
            rif40InvConditions=getRifViewData(connection, false /* column is a String */, "study_id", studyID, "rif40_inv_conditions",
                    "condition");
        } catch(Exception exception) {
            //Record original exception, throw sanitised, human-readable version
            String errorMessage
                    = RIFServiceMessages.getMessage(
                    "sqlResultsQueryManager.unableToGetRifViewData",
                    studyID);
            throw new RIFServiceException(
                    RIFServiceError.DATABASE_QUERY_FAILED,
                    errorMessage,
                    exception);
        }    

        try {
            if (!rif40InvConditions.next()) {
                throw new Exception("rif40Covariates ResultSet(): expected 1+ rows, got none");
            }
            
            do {
                if (icdFilter == null) {
                    icdFilter = "(" + rif40InvConditions.getString(1);
                }
                else {
                    icdFilter+=" OR " + rif40InvConditions.getString(1);
                }
            } while (rif40InvConditions.next());
            icdFilter+=")";
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
        
        return icdFilter;
    }
    
    /**
     * Generate Covariate Loss Report Query Formatter
     * <p>
     * Example SQL>
     *
     * WITH areas AS (
     * 	SELECT area_id
     * 	  FROM rif40.rif40_comparison_areas
     * 	 WHERE study_id = 193
     * ), summary AS (
     * 	SELECT 'SES' AS covariate_name, 			-* RIF40_INV_COVARIATES covariate_name *-
     * 		   'COVAR_SAHSULAND_COVARIATES4' AS covariate_table_name,
     * 		   'POP_SAHSULAND_POP' AS denominator_table_name,
     * 		   'NUM_SAHSULAND_CANCER' AS numerator_table_name,
     * 		   b.study_or_comparison,
     * 		   'age_sex_group BETWEEN 100 AND 221' As age_sex_group_filter,
     * 		   'CASE WHEN b.ses BETWEEN ''1.0'' AND ''5.0''' AS covariate_filter,
     * 		   '[icd] LIKE ''C33%'' -* Value filter *- AND [icd] LIKE ''C34%'' -* Value filter *-' AS icd_filter,
     * 		   COUNT(DISTINCT(b.year)) AS extract_years,
     * 		   MIN(b.year) AS extract_min_year,
     * 		   MAX(b.year) AS extract_max_year,
     * 		   COUNT(DISTINCT(a.area_id)) AS mapping_geolevel_areas,
     * 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missing_denominator_areas,
     * 		   SUM(b.my_new_investigation) AS numerator_count, 	-* RIF40_INVESTIGATION inv_name *-
     * 		   SUM(b.total_pop) AS denominator_count,
     * 		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b.ses BETWEEN '1.0' AND '5.0' THEN b.area_id ELSE NULL END)) AS missing_covariate_areas,
     * 		   SUM(b.total_pop)-SUM(CASE WHEN b.ses BETWEEN '1.0' AND '5.0' THEN b.total_pop ELSE 0 END) AS missing_denominator_covariate_count,
     * 		   100*(SUM(b.total_pop)-SUM(CASE WHEN b.ses BETWEEN '1.0' AND '5.0' THEN b.total_pop ELSE 0 END))/SUM(b.total_pop) AS missing_denominator_covariate_pct,
     * 		   SUM(b.my_new_investigation)-SUM(CASE WHEN b.ses BETWEEN '1.0' AND '5.0' THEN b.my_new_investigation ELSE 0 END) AS missing_numerator_covariate_count,
	 *  	   100*(SUM(b.my_new_investigation)-SUM(CASE WHEN b.ses BETWEEN '1.0' AND '5.0' THEN b.my_new_investigation ELSE 0 END))/SUM(b.my_new_investigation) AS missing_numerator_covariate_pct
     * 	  FROM areas a
     * 			LEFT OUTER JOIN rif_studies.s193_extract b ON
     * 				(a.area_id = b.area_id AND b.study_or_comparison = 'C')
     *      GROUP BY b.study_or_comparison
     * ), denominator_check AS (
     * 	SELECT COUNT(DISTINCT(d1.year)) AS years,
     * 			   MIN(d1.year) AS denominator_min_year,
     * 			   MAX(d1.year) AS denominator_max_year,
     * 			   SUM(COALESCE(d1.total, 0)) AS total_pop			-* RIF40_TABLES total_field *-
     * 		  FROM rif40.rif40_comparison_areas s, rif_data.pop_sahsuland_pop d1
     * 			LEFT OUTER JOIN rif_data.covar_sahsuland_covariates4 c1 ON (	-* Covariates *-
     * 					d1.sahsu_grd_level4 = c1.sahsu_grd_level4	-* RIF40_INV_COVARIATES study geolevel name *-
     * 				AND c1.year = d1.year)
     * 		 WHERE d1.year BETWEEN 1995 AND 1996                    -* investigation year filter *-
     * 		   AND d1.age_sex_group BETWEEN 100 AND 221             -* Derived AGE_SEX_GROUP field range *-
     * 		   AND s.area_id  = d1.sahsu_grd_level1					-* Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name *-
     * 		   AND s.area_id  IS NOT NULL							-* Exclude NULL geolevel *-
     * 		   AND s.study_id = 193								    -* Current study ID *-
     * ), numerator_check AS (
     * 	SELECT COUNT(DISTINCT(n1.year)) AS years,
     * 			   MIN(n1.year) AS numerator_min_year,
     * 			   MAX(n1.year) AS numerator_max_year,
     * 			   COUNT(n1.total) AS total_cases			-* RIF40_TABLES total_field *-
     * 		  FROM rif40.rif40_comparison_areas s, rif_data.num_sahsuland_cancer n1
     * 			LEFT OUTER JOIN rif_data.covar_sahsuland_covariates4 c1 ON (	-* Covariates *-
     * 					n1.sahsu_grd_level4 = c1.sahsu_grd_level4	-* RIF40_INV_COVARIATES study geolevel name *-
     * 				AND c1.year = n1.year)
     * 		 WHERE n1.year BETWEEN 1995 AND 1996                    -* investigation year filter *-
     * 		   AND n1.age_sex_group BETWEEN 100 AND 221             -* Derived AGE_SEX_GROUP field range *-
     * 		   AND s.area_id  = n1.sahsu_grd_level1					-* Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name *-
     * 		   AND s.area_id  IS NOT NULL							-* Exclude NULL geolevel *-
     * 		   AND s.study_id = 193								    -* Current study ID *-
     * 		   AND [icd] LIKE 'C33%' -* Value filter *- AND [icd] LIKE 'C34%' -* Value filter *-
     * )
     * SELECT a.*,
     *        b.years-a.extract_years AS missing_extract_years,
     * 	   b.total_pop-a.denominator_count AS missing_denominator,
     * 	   c.total_cases-a.numerator_count AS missing_numerator,
     * 	   b.denominator_min_year,
     * 	   b.denominator_max_year,
     * 	   c.numerator_min_year,
     * 	   c.numerator_max_year
     *   FROM summary a, denominator_check b, numerator_check c;
      *
     * @param studyID					studyID string
     * @param studyOrComparison			S or C
     * @param covariateName				RIF40_INV_COVARIATES covariate name
     * @param minValue					RIF40_INV_COVARIATES covariate minimum Value
     * @param maxValue					RIF40_INV_COVARIATES covariate maximum Value
     * @param invName					RIF40_INVESTIGATIONS investigation name
     * @param numerTab					RIF40_STUDIES numerator table name
     * @param denomTab					RIF40_STUDIES denominator table name
     * @param denomTabTotalField		RIF40_TABLES denominator total field name
     * @param numerTabTotalField		RIF40_TABLES numerator total field name
     * @param studyGeolevelName			RIF40_INV_COVARIATES study geolevel name
     * @param comparisonGeolevelName	extractTable comparison geolevel name
     * @param denomTabASGField			RIF40_TABLES denominator age sex group field name
     * @param numerTabASGField			RIF40_TABLES numerator age sex group field name
     * @param investigationYearStart	RIF40_INVESTIGATIONS year start
     * @param investigationYearStop		RIF40_INVESTIGATIONS year stop
     * @param covariateTableName		RIF40_GEOEVELS covariate table name
     * @param extractTable				extractTable extract table name
     * @param investigationMinAgeSexGroup	Derived AGE_SEX_GROUP field min range
     * @param investigationMaxAgeSexGroup	Derived AGE_SEX_GROUP field max range
     * @param icdFilter                 ICD code filter
     *
     * @return SQLGeneralQueryFormatter
     */
    private SQLGeneralQueryFormatter generateCovariateLossReportQueryFormatter(
            final String studyID,
            final String studyOrComparison,
            final String covariateName,
            final double minValue,
            final double maxValue,
            final String invName,
            final String numerTab,
            final String denomTab,
            final String denomTabTotalField,
            final String numerTabTotalField,
            final String studyGeolevelName,
            final String comparisonGeolevelName,
            final String denomTabASGField,
            final String numerTabASGField,
            final int investigationYearStart,
            final int investigationYearStop,
            final String covariateTableName,
            final String extractTable,
            final int investigationMinAgeSexGroup,
            final int investigationMaxAgeSexGroup,
            final String icdFilter) {

        SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

        queryFormatter.addQueryLine(0, "WITH areas AS (");
        queryFormatter.addQueryLine(0, "	SELECT area_id");
        if (studyOrComparison == "S") {
            queryFormatter.addQueryLine(0, "	  FROM rif40.rif40_study_areas");
        }
        else {
            queryFormatter.addQueryLine(0, "	  FROM rif40.rif40_comparison_areas");
        }
        queryFormatter.addQueryLine(0, "	 WHERE study_id = " + studyID);
        queryFormatter.addQueryLine(0, "), summary AS (");
        queryFormatter.addQueryLine(0, "	SELECT '" + covariateName + 
            "' AS covariate_name, 			/* RIF40_INV_COVARIATES covariate_name */");
        queryFormatter.addQueryLine(0, "		   '" + covariateTableName + "' AS covariate_table_name,");
        queryFormatter.addQueryLine(0, "		   '" + denomTab + "' AS denominator_table_name,");
        queryFormatter.addQueryLine(0, "		   '" + numerTab + "' AS numerator_table_name,");
        queryFormatter.addQueryLine(0, "		   b.study_or_comparison,");
        queryFormatter.addQueryLine(0, "		   'age_sex_group BETWEEN " + 
            investigationMinAgeSexGroup + " AND " + investigationMaxAgeSexGroup + "' AS age_sex_group_filter,");
        queryFormatter.addQueryLine(0, "		   'CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN ''" + minValue + "'' AND ''" + maxValue + "''' AS covariate_filter,");
        queryFormatter.addQueryLine(0, "		   '" + icdFilter.replaceAll("\'", "''").replace("OR", "OR</br>") /* escape DB 's */ + "' AS icd_filter,");
        queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(b.year)) AS extract_years,");
        queryFormatter.addQueryLine(0, "		   MIN(b.year) AS extract_min_year,");
        queryFormatter.addQueryLine(0, "		   MAX(b.year) AS extract_max_year,");
        queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id)) AS mapping_geolevel_areas,");
        queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(b.area_id)) AS missing_extract_areas,");
        queryFormatter.addQueryLine(0, "		   SUM(b." + invName.toLowerCase() + ") AS numerator_count, 	/* RIF40_INVESTIGATION inv_name */");
        queryFormatter.addQueryLine(0, "		   SUM(b.total_pop) AS denominator_count,");
        queryFormatter.addQueryLine(0, "		   COUNT(DISTINCT(a.area_id))-COUNT(DISTINCT(CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.area_id ELSE NULL END)) AS missing_covariate_areas,");
        queryFormatter.addQueryLine(0, "		   SUM(b.total_pop)-SUM(CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.total_pop ELSE 0 END) AS missing_denominator_covariate_count,");
        queryFormatter.addQueryLine(0, "		   100*(SUM(b.total_pop)-SUM(CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b.total_pop ELSE 0 END))/SUM(b.total_pop)" +
                " AS missing_denominator_covariate_pct,");
        queryFormatter.addQueryLine(0, "		   SUM(b." + invName.toLowerCase() + ")-SUM(CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b." + invName.toLowerCase() + " ELSE 0 END) " +
                 "AS missing_numerator_covariate_count,");
        queryFormatter.addQueryLine(0, "		   100*(SUM(b." + invName.toLowerCase() + ")-SUM(CASE WHEN b." + covariateName.toLowerCase() +
                " BETWEEN '" + minValue + "' AND '" + maxValue + "' THEN b." + invName.toLowerCase() + " ELSE 0 END))/SUM(b." + invName.toLowerCase() + ") " +
                 "AS missing_numerator_covariate_pct");
        queryFormatter.addQueryLine(0, "	  FROM areas a");
        queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN rif_studies." + extractTable.toLowerCase() + " b ON");
        queryFormatter.addQueryLine(0, "				(a.area_id = b.area_id AND b.study_or_comparison = '" + studyOrComparison + "')");
        queryFormatter.addQueryLine(0, "     GROUP BY b.study_or_comparison");
        queryFormatter.addQueryLine(0, "), denominator_check AS (");
        queryFormatter.addQueryLine(0, "	SELECT COUNT(DISTINCT(d1.year)) AS years,");
        queryFormatter.addQueryLine(0, "			   MIN(d1.year) AS denominator_min_year,");
        queryFormatter.addQueryLine(0, "			   MAX(d1.year) AS denominator_max_year,");
        queryFormatter.addQueryLine(0, "			   SUM(COALESCE(d1." + denomTabTotalField.toLowerCase() + ", 0)) " +
            "AS total_pop			/* RIF40_TABLES total_field */");
        if (studyOrComparison == "S") {
            queryFormatter.addQueryLine(0, "		  FROM rif40.rif40_study_areas s, rif_data." + denomTab.toLowerCase() + " d1");       
        }
        else {		
            queryFormatter.addQueryLine(0, "		  FROM rif40.rif40_comparison_areas s, rif_data." + denomTab.toLowerCase() + " d1");
        }
        queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN rif_data." + covariateTableName.toLowerCase() + 
            " c1 ON (	/* Covariates */");
        // Covariate join ALWAYS at study geolevel name
        queryFormatter.addQueryLine(0, "					d1." + studyGeolevelName.toLowerCase() + 
            " = c1." + studyGeolevelName.toLowerCase() + "	 /* RIF40_INV_COVARIATES study geolevel name */");
        queryFormatter.addQueryLine(0, "				AND c1.year = d1.year)");
        queryFormatter.addQueryLine(0, "		 WHERE d1.year BETWEEN " + investigationYearStart + " AND " + investigationYearStop + 
               "                    /* investigation year filter */");
        queryFormatter.addQueryLine(0, "		   AND d1." + denomTabASGField.toLowerCase() + " BETWEEN " + 
            investigationMinAgeSexGroup + " AND " + investigationMaxAgeSexGroup + "             /* Derived AGE_SEX_GROUP field range */");
        if (studyOrComparison == "S") {
            queryFormatter.addQueryLine(0, "		   AND s.area_id  = d1." + studyGeolevelName.toLowerCase() + 
                "					/* study geolevel join: RIF40_INV_COVARIATES study geolevel name */");        
        }
        else {
            queryFormatter.addQueryLine(0, "		   AND s.area_id  = d1." + comparisonGeolevelName.toLowerCase() + 
                "					/* Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name */");
        }
        queryFormatter.addQueryLine(0, "		   AND s.area_id  IS NOT NULL							/* Exclude NULL geolevel */");
        queryFormatter.addQueryLine(0, "		   AND s.study_id = " + studyID + "					    			/* Current study ID */");
        queryFormatter.addQueryLine(0, "), numerator_check AS (");
        queryFormatter.addQueryLine(0, "	SELECT COUNT(DISTINCT(n1.year)) AS years,");
        queryFormatter.addQueryLine(0, "			   MIN(n1.year) AS numerator_min_year,");
        queryFormatter.addQueryLine(0, "			   MAX(n1.year) AS numerator_max_year,");
        if (numerTabTotalField != null) {
            queryFormatter.addQueryLine(0, "			   SUM(COALESCE(n1." + numerTabTotalField.toLowerCase() + ", 0)) " +
                "AS total_cases			/* RIF40_TABLES total_field */");
        } 
        else {
            queryFormatter.addQueryLine(0, "			   COUNT(1) " +
                "AS total_cases			/* RIF40_TABLES total_field IS NULL */");
        }
        if (studyOrComparison == "S") {
            queryFormatter.addQueryLine(0, "		  FROM rif40.rif40_study_areas s, rif_data." + numerTab.toLowerCase() + " n1");       
        }
        else {		
            queryFormatter.addQueryLine(0, "		  FROM rif40.rif40_comparison_areas s, rif_data." + numerTab.toLowerCase() + " n1");
        }
        queryFormatter.addQueryLine(0, "			LEFT OUTER JOIN rif_data." + covariateTableName.toLowerCase() + 
            " c1 ON (	/* Covariates */");
        // Covariate join ALWAYS at study geolevel name
        queryFormatter.addQueryLine(0, "					n1." + studyGeolevelName.toLowerCase() + 
            " = c1." + studyGeolevelName.toLowerCase() + "	 /* RIF40_INV_COVARIATES study geolevel name */");
        queryFormatter.addQueryLine(0, "				AND c1.year = n1.year)");
        queryFormatter.addQueryLine(0, "		 WHERE n1.year BETWEEN " + investigationYearStart + " AND " + investigationYearStop + 
               "                    /* investigation year filter */");
        queryFormatter.addQueryLine(0, "		   AND n1." + numerTabASGField.toLowerCase() + " BETWEEN " + 
            investigationMinAgeSexGroup + " AND " + investigationMaxAgeSexGroup + "             /* Derived AGE_SEX_GROUP field range */");
        if (studyOrComparison == "S") {
            queryFormatter.addQueryLine(0, "		   AND s.area_id  = n1." + studyGeolevelName.toLowerCase() + 
                "					/* study geolevel join: RIF40_INV_COVARIATES study geolevel name */");        
        }
        else {
            queryFormatter.addQueryLine(0, "		   AND s.area_id  = n1." + comparisonGeolevelName.toLowerCase() + 
                "					/* Comparison geolevel join: RIF40_INVESTIGATIONS comparison_geolevel_name */");
        }
        queryFormatter.addQueryLine(0, "		   AND s.area_id  IS NOT NULL							/* Exclude NULL geolevel */");
        queryFormatter.addQueryLine(0, "		   AND s.study_id = " + studyID + "						    		/* Current study ID */");
        queryFormatter.addQueryLine(0, "		   AND " + icdFilter);
        queryFormatter.addQueryLine(0, ")");
        
        queryFormatter.addQueryLine(0, "SELECT a.*,");
        queryFormatter.addQueryLine(0, "       b.years-a.extract_years AS missing_extract_years,");
        queryFormatter.addQueryLine(0, "	   b.total_pop-a.denominator_count AS missing_denominator,");
        queryFormatter.addQueryLine(0, "	   c.total_cases-a.numerator_count AS missing_numerator,");
        queryFormatter.addQueryLine(0, "	   b.denominator_min_year,");
        queryFormatter.addQueryLine(0, "	   b.denominator_max_year,");
        queryFormatter.addQueryLine(0, "	   c.numerator_min_year,");
        queryFormatter.addQueryLine(0, "	   c.numerator_max_year");
        queryFormatter.addQueryLine(0, "  FROM summary a, denominator_check b, numerator_check c;");

        return queryFormatter;
    }
    
}