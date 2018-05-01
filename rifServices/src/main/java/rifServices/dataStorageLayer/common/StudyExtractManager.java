package rifServices.dataStorageLayer.common;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Locale;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudySubmission;

public interface StudyExtractManager extends SQLManager {
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	String getStudyExtractFIleName(
			User user,
			String studyID)
					throws RIFServiceException;
	
	/**
	 * Get study extract
	 *
	 * @param  connection	Database specfic Connection object assigned from pool
	 * @param  user 		Database username of logged on user.
	 * @param  rifStudySubmission 		RIFStudySubmission object.
	 * @param  String 		zoomLevel (as text!).
	 * @param  studyID 		Study_id (as text!).
	 *
	 * @return 				FileInputStream
	 *
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	FileInputStream getStudyExtract(
			Connection connection,
			User user,
			RIFStudySubmission rifStudySubmission,
			String zoomLevel,
			String studyID)
					throws RIFServiceException;
	
	/**
	 * Get textual extract status of a study.
	 * <p>
	 * This fucntion determines whether a study can be extracted from the database and the results returned to the user in a ZIP file
	 * </p>
	 * <p>
	 * Returns the following textual strings:
	 * <il>
	 *   <li>STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
	 *     <ul>
	 *	     <li>C: created, not verified;</li>
	 *	     <li>V: verified, but no other work done; [NOT USED BY MIDDLEWARE]</li>
	 *	     <li>E: extracted imported or created, but no results or maps created;</li>
	 *	     <li>R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]</li>
	 *	     <li>W: R warning. [NOT USED BY MIDDLEWARE]</li>
	 *     <ul>
	 *   </li>
	 *   <li>STUDY_FAILED_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
	 *	     <li>G: Extract failure, extract, results or maps not created;</li>
	 *	     <li>F: R failure, R has caught one or more exceptions [depends on the exception handler]</li>
	 *   </li>
	 *   <li>STUDY_EXTRACTABLE_NEEDS_ZIPPING: returned for the following rif40_studies.study_state code/meaning of: S: R success;
	 *       when the ZIP extrsct file has not yet been created
	 *   </il>
	 *   <li>STUDY_EXTRABLE_ZIPPID: returned for the following rif40_studies.study_statu  code/meaning of: S: R success;
	 *       when the ZIP extrsct file has been created
	 *   </il>
	 *   <il>STUDY_ZIP_FAILED: returned for the following rif40_studies.study_statu  code/meaning of: S: R success;
	 *       when the ZIP extract error file has been created
	 *   </il>
	 *   <il>STUDY_ZIP_IN_PROGRESS: returned for the following rif40_studies.study_statu  code/meaning of: S: R success;
	 *       when the ZIP extract file has been created
	 *   </il>
	 * </il>
	 * </p>
	 *
	 * @param  connection	Database specfic Connection object assigned from pool
	 * @param  user 		Database username of logged on user.
	 * @param  rifStudySubmission 		RIFStudySubmission object.
	 * @param  studyID 		Study_id (as text!).
	 *
	 * @return 				Textual extract status as exscaped JSON, e.g. {status: STUDY_NOT_FOUND}
	 *
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	String getExtractStatus(
			Connection connection,
			User user,
			RIFStudySubmission rifStudySubmission,
			String studyID
	                       )
					throws RIFServiceException;
	
	/**
	 * Get the JSON setup file for a run study.
	 * <p>
	 * This function returns the JSON setup file for a run study, including the print setup
	 * </p>
	 *
	 * @param  connection	Database specfic Connection object assigned from pool
	 * @param  user 		Database username of logged on user.
	 * @param  rifStudySubmission 		RIFStudySubmission object.
	 * @param  studyID 		Study_id (as text!).
	 *
	 * @return 				Textual extract status as exscaped JSON, e.g. {status: STUDY_NOT_FOUND}
	 *
	 * e.g.
{
	"rif_job_submission": {
		"submitted_by": "peter",
		"job_submission_date": "24/08/2017 16:18:38",
		"project": {
			"name": "TEST",
			"description": "Test project"
		},
		"disease_mapping_study": {
			"name": "1002 LUNG CANCER",
			"description": "",
			"geography": {
				"name": "SAHSULAND",
				"description": "SAHSULAND"
			},
			"disease_mapping_study_area": {
				"geo_levels": {
					"geolevel_select": {
						"name": "SAHSU_GRD_LEVEL1"
					},
					"geolevel_area": {
						"name": ""
					},
					"geolevel_view": {
						"name": "SAHSU_GRD_LEVEL4"
					},
					"geolevel_to_map": {
						"name": "SAHSU_GRD_LEVEL4"
					}
				},
				"map_areas": {
					"map_area": [{
							"id": "01",
							"gid": "01",
							"label": "01",
							"band": 1
						}
					]
				}
			},
			"comparison_area": {
				"geo_levels": {
					"geolevel_select": {
						"name": "SAHSU_GRD_LEVEL1"
					},
					"geolevel_area": {
						"name": ""
					},
					"geolevel_view": {
						"name": "SAHSU_GRD_LEVEL1"
					},
					"geolevel_to_map": {
						"name": "SAHSU_GRD_LEVEL1"
					}
				},
				"map_areas": {
					"map_area": [{
							"id": "01",
							"gid": "01",
							"label": "01",
							"band": 1
						}
					]
				}
			},
			"investigations": {
				"investigation": [{
						"title": "TEST 1002",
						"health_theme": {
							"name": "cancers",
							"description": "covering various types of cancers"
						},
						"numerator_denominator_pair": {
							"numerator_table_name": "NUM_SAHSULAND_CANCER",
							"numerator_table_description": "cancer numerator",
							"denominator_table_name": "POP_SAHSULAND_POP",
							"denominator_table_description": "population health file"
						},
						"age_band": {
							"lower_age_group": {
								"id": 0,
								"name": "0",
								"lower_limit": "0",
								"upper_limit": "0"
							},
							"upper_age_group": {
								"id": 0,
								"name": "85PLUS",
								"lower_limit": "85",
								"upper_limit": "255"
							}
						},
						"health_codes": {
							"health_code": [{
									"code": "C33",
									"name_space": "icd10",
									"description": "Malignant neoplasm of trachea",
									"is_top_level_term": "no"
								}, {
									"code": "C340",
									"name_space": "icd10",
									"description": "Main bronchus",
									"is_top_level_term": "no"
								}, {
									"code": "C341",
									"name_space": "icd10",
									"description": "Upper lobe, bronchus or lung",
									"is_top_level_term": "no"
								}, {
									"code": "C342",
									"name_space": "icd10",
									"description": "Middle lobe, bronchus or lung",
									"is_top_level_term": "no"
								}, {
									"code": "C343",
									"name_space": "icd10",
									"description": "Lower lobe, bronchus or lung",
									"is_top_level_term": "no"
								}, {
									"code": "C348",
									"name_space": "icd10",
									"description": "Overlapping lesion of bronchus and lung",
									"is_top_level_term": "no"
								}, {
									"code": "C349",
									"name_space": "icd10",
									"description": "Bronchus or lung, unspecified",
									"is_top_level_term": "no"
								}
							]
						},
						"year_range": {
							"lower_bound": 1995,
							"upper_bound": 1996
						},
						"year_intervals": {
							"year_interval": [{
									"start_year": "1995",
									"end_year": "1995"
								}, {
									"start_year": "1996",
									"end_year": "1996"
								}
							]
						},
						"years_per_interval": 1,
						"sex": "Both",
						"covariates": []
					}
				]
			}
		},
		"calculation_methods": {
			"calculation_method": {
				"name": "het_r_procedure",
				"code_routine_name": "het_r_procedure",
				"description": "Heterogenous (HET) model type",
				"parameters": {
					"parameter": []
				}
			}
		},
		"rif_output_options": {
			"rif_output_option": ["Data", "Maps", "Ratios and Rates"]
		}
	}
}
	 * @param  locale 		locale
	 * @param  url e.g. http://localhost:8080.
	 *
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	String getJsonFile(
			Connection connection,
			User user,
			RIFStudySubmission rifStudySubmission,
			String studyID,
			Locale locale,
			String url)
					throws RIFServiceException;
	
	/**
     * Create study extract.
	 *
     * @param Connection connection (required)
     * @param User user (required)
     * @param RIFStudySubmission rifStudySubmission (required)
     * @param String zoomLevel (required)
     * @param String studyID (required)
     * @param Locale locale (required)
     * @param String url [deduced from calling URL] (required)
     * @return JSONObject [front end saves as JSON5 file]
     */
	void createStudyExtract(
			Connection connection,
			User user,
			RIFStudySubmission rifStudySubmission,
			String zoomLevel,
			String studyID,
			Locale locale,
			String url)
					throws RIFServiceException;
	
	String getRif40StudyState(
			Connection connection,
			String studyID)
									throws Exception;
}
