package rifServices.dataStorageLayer.ms;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Locale;

import org.json.JSONObject;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.dataStorageLayer.common.GetStudyJSON;
import rifServices.dataStorageLayer.common.RifZipFile;
import rifServices.dataStorageLayer.common.StudyExtractManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

public class MSSQLStudyExtractManager extends MSSQLAbstractSQLManager
		implements StudyExtractManager {

	private static String EXTRACT_DIRECTORY;
	private static String TAXONOMY_SERVICES_SERVER;
	private static final String STUDY_QUERY_SUBDIRECTORY = "study_query";
	private static final String STUDY_EXTRACT_SUBDIRECTORY = "study_extract";
	private static final String RATES_AND_RISKS_SUBDIRECTORY = "rates_and_risks";
	private static final String GEOGRAPHY_SUBDIRECTORY = "geography";

	private static final int BASE_FILE_STUDY_NAME_LENGTH = 100;
	private static String lineSeparator = System.getProperty("line.separator");
	private RIFServiceStartupOptions rifServiceStartupOptions;
	private static DatabaseType databaseType;

	public MSSQLStudyExtractManager(
		final RIFServiceStartupOptions rifServiceStartupOptions) {

		
		super(rifServiceStartupOptions);
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		EXTRACT_DIRECTORY = this.rifServiceStartupOptions.getExtractDirectory();
		TAXONOMY_SERVICES_SERVER = this.rifServiceStartupOptions.getTaxonomyServicesServer();
		databaseType=this.rifServiceStartupOptions.getRifDatabaseType();
	}

	@Override
	public String getStudyExtractFIleName(
			final User user,
			final String studyID)
					throws RIFServiceException {
						
		StringBuilder fileName = new StringBuilder();
		fileName.append(user.getUserID());		
		fileName.append("_");
		fileName.append("s" + studyID);
		fileName.append(".zip");
		
		return fileName.toString();	
	} 

	/**
	 * Get study extract
	 *
	 * @param  connection	Database specfic Connection object assigned from pool
	 * @param  user 		Database username of logged on user.
	 * @param  rifStudySubmission 		RIFStudySubmission object.
	 * @param  zoomLevel (as text!).
	 * @param  studyID 		Study_id (as text!).
	 *
	 * @return 				FileInputStream 
	 * 
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */	
	@Override
	public FileInputStream getStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID)
					throws RIFServiceException {
						
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		FileInputStream zipStream=rifZipFile.getStudyExtract(
			connection,
			user,
			rifStudySubmission,
			zoomLevel,
			studyID);
		return zipStream;
	}
	
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
	 *   <il>STUDY_NOT_FOUND: returned where the studyID was not found in rif40_studies
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
	 * @return 				Textual extract status, e.g. {status: STUDY_NOT_FOUND} 
	 * 
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	@Override
	public String getExtractStatus(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID
			)
					throws RIFServiceException {
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		String extractStatus=rifZipFile.getExtractStatus(
			connection,
			user,
			rifStudySubmission,
			studyID);
		return extractStatus;
	}
	
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
	 * @param  tomcatServer e.g. http://localhost:8080.
	 * 
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	@Override
	public String getJsonFile(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID,
			final Locale locale,
			final String tomcatServer)
					throws RIFServiceException {
		String result;

		try {
			JSONObject json = new JSONObject();
			GetStudyJSON getStudyJSON = new GetStudyJSON(this);
			JSONObject rif_job_submission=getStudyJSON.addRifStudiesJson(connection, 
				studyID, locale, tomcatServer, TAXONOMY_SERVICES_SERVER);
			rif_job_submission.put("created_by", user.getUserID());
			json.put("rif_job_submission", rif_job_submission);
			result=json.toString();
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "MSSQLStudyExtractManager ERROR", exception);
				
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.getJsonFile",
					user.getUserID(),
					studyID);
			RIFServiceException rifServiceExeption
				= new RIFServiceException(
					RIFServiceError.JSONFILE_CREATE_FAILED, 
					errorMessage);
			throw rifServiceExeption;
		}
		return result;
	}
		
	/** 
     * Create study extract. 
	 *
     * @param connection (required)
     * @param user (required)
     * @param rifStudySubmission (required)
     * @param zoomLevel (required)
     * @param studyID (required)
     * @param locale (required)
     * @param tomcatServer [deduced from calling URL] (required)
     * @return JSONObject [front end saves as JSON5 file]
     */		
	@Override
	public void createStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID,
			final Locale locale,
			final String tomcatServer)
					throws RIFServiceException {
						
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		rifZipFile.createStudyExtract(connection,
			user,
			rifStudySubmission,
			zoomLevel,
			studyID,
			locale,
			tomcatServer,
			TAXONOMY_SERVICES_SERVER);

	}

	public String getRif40StudyState(
			final Connection connection,
			final String studyID)
					throws Exception {
		String studyState;
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		studyState=rifZipFile.getRif40StudyState(connection,
			studyID);
		
		return studyState;
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
