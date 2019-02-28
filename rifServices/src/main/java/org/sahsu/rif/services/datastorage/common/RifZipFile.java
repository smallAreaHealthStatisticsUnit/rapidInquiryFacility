package org.sahsu.rif.services.datastorage.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.fileformats.RIFStudySubmissionContentHandler;
import org.sahsu.rif.services.graphics.RIFGraphics;
import org.sahsu.rif.services.graphics.RIFGraphicsOutputType;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import javax.sql.rowset.CachedRowSet;

public class RifZipFile {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static String EXTRACT_DIRECTORY;
	private static int printingDPI;
	
	private static final String STUDY_QUERY_SUBDIRECTORY = "study_query";
	private static final int BASE_FILE_STUDY_NAME_LENGTH = 100;

	private RIFServiceStartupOptions rifServiceStartupOptions;
	private static DatabaseType databaseType;
	private static int denominatorPyramidWidthPixels;
	
	private final SQLManager manager;

	/**
     * Constructor.
     * 
     * @param rifServiceStartupOptions (required)
     */
	public RifZipFile(
			final RIFServiceStartupOptions rifServiceStartupOptions, SQLManager manager) {
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		this.manager = manager;
		
		try {
			EXTRACT_DIRECTORY = this.rifServiceStartupOptions.getExtractDirectory();
			databaseType=this.rifServiceStartupOptions.getRifDatabaseType();
			printingDPI=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("printingDPI", 1000);
			denominatorPyramidWidthPixels=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
						"denominatorPyramidWidthPixels", 3543);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RifZipFile() constructor");
			throw new NullPointerException();
		}
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
	public FileInputStream getStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID)
					throws RIFServiceException {
		//Validate parameters
		String temporaryDirectoryPath = null;
		File submissionZipFile = null;
		FileInputStream fileInputStream = null;
		
		try {
			//Establish the phrase that will be used to help name the main zip
			//file and data files within its directories
			String baseStudyName = createBaseStudyFileName(rifStudySubmission, studyID);

			Path temporaryDirectory = createTemporaryDirectoryPath(studyID);
			if (temporaryDirectory.toFile().exists()) {
				rifLogger.info(this.getClass(), "Found R temporary directory: "
				                                + temporaryDirectory.toString());
			}
			else {
				throw new Exception("R temporary directory: "  + 
					temporaryDirectory.toString() + " was not created by Statistics_JRI.R");
			}
			
			submissionZipFile = createSubmissionZipFile(
					user,
					baseStudyName);
					
			if (submissionZipFile.isFile()) { // No file (i.e. NULL) handled in *AbstractRIFWebServiceResource.java
				fileInputStream = new FileInputStream(submissionZipFile);	
				rifLogger.info(this.getClass(), "Fetched ZIP file: " + 
					submissionZipFile.getAbsolutePath());
			}
			else {
				rifLogger.info(this.getClass(), "Unable to fetch ZIP file: " + 
					submissionZipFile.getAbsolutePath() + "; file does not exist");
			}
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "RifZipFile ERROR", exception);
				
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToGetStudyExtract",
					user.getUserID(),
					submissionZipFile.getAbsolutePath());
			throw new RIFServiceException(
					RIFServiceError.ZIPFILE_CREATE_FAILED,
					errorMessage);
		}	

		return fileInputStream;
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
	 public String getExtractStatus(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID
			)
					throws RIFServiceException {
		String result=null;
		File submissionZipFile = null;
		String zipFileName="UNKNOWN";
		
		try {
			//Establish the phrase that will be used to help name the main zip
			//file and data files within its directories
			
			String studyStatus=getRif40StudyState(connection, studyID);
			if (studyStatus == null) { 	// Study ID does not exist. You will not get this
										// [this is raised as an exception in the calling function: RIFStudySubmission.getRIFStudySubmission()]
				throw new Exception("STUDY_NOT_FOUND: " + studyID);
			}
			if (result != null && studyStatus != null) { 
				switch (studyStatus.charAt(0)) {
					case 'C':
					case 'V':
					case 'E':
					case 'R':
					case 'W':
						result="STUDY_INCOMPLETE_NOT_ZIPPABLE";
						break;
					case 'G':
					case 'F':
						result="STUDY_FAILED_NOT_ZIPPABLE";
						break;
					case 'S':	/* R success */
						break;
					default:
						throw new Exception("Invalid rif40_studies.study_state: " + studyStatus);
				}
			}
			
			if (result == null) {
				String baseStudyName 
				= createBaseStudyFileName(rifStudySubmission, studyID);
				
				submissionZipFile = createSubmissionZipFile(
						user,
						baseStudyName);
				File submissionZipSavFile = createSubmissionZipFile(
					user,
					baseStudyName + ".sav");		
				zipFileName=submissionZipFile.getAbsolutePath();
				String errFileName=zipFileName.replace(".zip", ".err");
				File errFile= new File(errFileName);
				
				if (errFile.isFile()) { // ZIP extract error file has been created
					result="STUDY_ZIP_FAILED";
				}
				else if (submissionZipSavFile.isFile()) { // .sav ZIP file exists - in progress
					result="STUDY_ZIP_IN_PROGRESS";
				}
				else if (submissionZipFile.isFile()) { // ZIP file exists - no need to recreate
					result="STUDY_EXTRACTBLE_ZIPPID";
				}
				else { // No zip file 
					result="STUDY_EXTRACTABLE_NEEDS_ZIPPING";
				}
			}
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "RifZipFile ERROR", exception);
				
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToGetExtractStatus",
					user.getUserID(),
					studyID,
					zipFileName);
			throw new RIFServiceException(
				RIFServiceError.ZIPFILE_GET_STATUS_FAILED,
				errorMessage);
		}

		return "{\"status\":\"" + result + "\"}";
	}
	
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
     * @param String taxonomyServicesServer [parameter] (required)
     * @return JSONObject [front end saves as JSON5 file]
     */
	public void createStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID,
			final Locale locale,
			final String url,
			final String taxonomyServicesServer)
					throws RIFServiceException {

		//Validate parameters
		File submissionZipFile = null;
		ZipOutputStream submissionZipOutputStream = null;
		File submissionZipSavFile = null;
		File submissionZipErrorFile = null;
		
		try {
			//Establish the phrase that will be used to help name the main zip
			//file and data files within its directories
			String baseStudyName = createBaseStudyFileName(rifStudySubmission, studyID);

			Path temporaryDirectoryPath = createTemporaryDirectoryPath(studyID);
			if (temporaryDirectoryPath.toFile().exists()) {
				rifLogger.info(this.getClass(), "Found R temporary directory: "
				                                + temporaryDirectoryPath.toString());
			}
			else {
				throw new Exception("R temporary directory: "  + 
					temporaryDirectoryPath.toString() + " was not created by Statistics_JRI.R");
			}
				
			submissionZipSavFile = createSubmissionZipFile(
					user,
					baseStudyName + ".sav");
			submissionZipFile = createSubmissionZipFile(
					user,
					baseStudyName);
			if (submissionZipFile != null) {
				String submissionZipErrorFileName = submissionZipFile.getAbsolutePath();
				if (submissionZipErrorFileName != null) {
					submissionZipErrorFileName=submissionZipErrorFileName.replace(".zip", ".err");
				}
				submissionZipErrorFile=new File(submissionZipErrorFileName);
			}
			if (submissionZipFile.isFile()) { // ZIP file exists - no need to recreate
				Thread.sleep(500); // Sleep to allow JS promises time to work
				rifLogger.info(this.getClass(), "No need to create ZIP file: " + 
					submissionZipFile.getAbsolutePath() + "; already exists");
			}
			else if (submissionZipSavFile.isFile()) { // Sav file exists - being created
				Thread.sleep(500); // Sleep to allow JS promises time to work
				rifLogger.info(this.getClass(), "No need to create ZIP file: " + 
					submissionZipSavFile.getAbsolutePath() + "; being created");
			}
			else { // No zip file - can be created 
				submissionZipOutputStream = new ZipOutputStream(new FileOutputStream(submissionZipSavFile));
							
				CachedRowSet rif40Studies=getRif40Studies(connection, studyID);	
				CachedRowSet rif40Investigations=getRif40Investigations(connection, studyID);	
					// Assumes one study at present
				rifLogger.info(this.getClass(), 
					"Create study extract for: " + studyID + "; databaseType: " + databaseType);
				String denominatorHTML=addDenominator(
					user,
					connection, 
					temporaryDirectoryPath.toFile(),
					studyID,
					1 /* Header level */,
					locale,
					rif40Studies);
				String numeratorHTML=addNumerator(
					user,
					connection, 
					temporaryDirectoryPath.toFile(),
					studyID,
					1 /* Header level */,
					locale,
					rif40Studies);	
					
				addJsonFile(
						temporaryDirectoryPath.toFile(),
						submissionZipOutputStream,
						connection, user, studyID, locale, url);

				addCssFile(
						temporaryDirectoryPath.toFile(),
						submissionZipOutputStream,
						studyID,
						"RIFStudyHeader.css");
				addCssFile(
						temporaryDirectoryPath.toFile(),
						submissionZipOutputStream,
						studyID,
						"RIFPopulationPyramid.css");
						
				RifGeospatialOutputs rifGeospatialOutputs = 
					new RifGeospatialOutputs(rifServiceStartupOptions, manager);
				String mapHTML=rifGeospatialOutputs.writeGeospatialFiles(
						connection,
						temporaryDirectoryPath.toFile(),
						baseStudyName,
						zoomLevel,
						rifStudySubmission,
						rif40Studies,
						rif40Investigations,
						locale);
						
				addHtmlFile(
						temporaryDirectoryPath.toFile(),
						submissionZipOutputStream,
						connection, user, studyID, locale, url, taxonomyServicesServer,
						denominatorHTML, numeratorHTML, mapHTML);
						
				//write the study the user made when they first submitted their query
				writeQueryFile(
						submissionZipOutputStream,
						user,
						baseStudyName,
						rifStudySubmission);
						
				addAllFilesToZip(
					temporaryDirectoryPath.toFile(),
					submissionZipOutputStream,
					null);

				/*
				writeStatisticalPostProcessingFiles(
					connection,
					temporaryDirectoryPath,
					submissionZipOutputStream,				
					baseStudyName,
					rifStudySubmission);

				writeTermsAndConditionsFiles(
					submissionZipOutputStream);	
				 */	
				submissionZipOutputStream.flush();
				submissionZipOutputStream.close();
				submissionZipSavFile.renameTo(submissionZipFile);
				rifLogger.info(this.getClass(), "Created ZIP file: " + 
					submissionZipFile.getAbsolutePath());
			}
		}
		catch(OutOfMemoryError outOfMemoryError) {
			
			MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long maxMemory = heapUsage.getMax() / (1024*1024);
            long usedMemory = heapUsage.getUsed() / (1024*1024);
			
			rifLogger.error(this.getClass(), "createStudyExtract() OutOfMemoryError; " + 
				"heap usage: " + usedMemory + "M, " + maxMemory + "M", outOfMemoryError);
			String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlStudyStateManager.error.unableToCreateStudyExtract",
						user.getUserID(),
						submissionZipFile.getAbsolutePath());
			try {
				if (submissionZipOutputStream != null) {
					submissionZipOutputStream.flush();
					submissionZipOutputStream.close();
				}
				if (submissionZipSavFile != null) {
					submissionZipSavFile.delete();
				}
			}
			catch(Exception err) {
				rifLogger.warning(this.getClass(), 
					"createStudyExtract() close ZIP stream ERROR: " + err.getMessage());
			}
			
			try {
				Exception exception=new Exception("OutOfMemoryError: " + 
				"; Memory Use :" + usedMemory + "M/" + maxMemory + "M", outOfMemoryError);
				// Dump error to file
				writeErrorFile(exception, submissionZipErrorFile);
			}
			catch (Exception e) {
				rifLogger.error(this.getClass(), "writeErrorFile() ERROR", e);
			}
			
			RIFServiceException rifServiceExeption
				= new RIFServiceException(
					RIFServiceError.ZIPFILE_CREATE_FAILED, 
					errorMessage);
			throw rifServiceExeption;						
		}
		catch(Exception exception) {
			String errorMessage = null;
			try {
				rifLogger.error(this.getClass(), "createStudyExtract() ERROR", exception);
				String submissionZipFilePath = "[unable to deduce file name]";
				if (submissionZipFile != null) {
					submissionZipFilePath=submissionZipFile.getAbsolutePath();
				}
				if (exception.getMessage() != null &&
					exception.getMessage().
					equals("Taxonomy service still initialising; please run again in 5 minutes")) {
					errorMessage
						= RIFServiceMessages.getMessage(
							"sqlStudyStateManager.error.taxonomyInitialiseError",
							user.getUserID(),
							submissionZipFilePath);	
				}
				else {		
					errorMessage
						= RIFServiceMessages.getMessage(
							"sqlStudyStateManager.error.unableToCreateStudyExtract",
							user.getUserID(),
							submissionZipFilePath);
				}
			}
			catch (NullPointerException nullPointerException) {
				rifLogger.error(this.getClass(), "createStudyExtract() NullPointerException", 
					nullPointerException);	
			}
//			temporaryDirectory.delete();
				
			try {
				if (submissionZipOutputStream != null) {
					submissionZipOutputStream.flush();
					submissionZipOutputStream.close();
				}
				if (submissionZipSavFile != null) {
					submissionZipSavFile.delete();
				}
			}
			catch(Exception err) {
				rifLogger.warning(this.getClass(), 
					"createStudyExtract() close ZIP stream ERROR: " + err.getMessage());
			}
			
			try {
				// Dump error to file
				writeErrorFile(exception, submissionZipErrorFile);
			}
			catch (Exception e) {
				rifLogger.error(this.getClass(), "writeErrorFile() ERROR", e);
			}

			throw new RIFServiceException(
				RIFServiceError.ZIPFILE_CREATE_FAILED,
				errorMessage, exception);
		}
	}

	/** Write error file for exception
	  *
	  * @param Exception exception, 
	  * @param File submissionZipErrorFile
	  */
	private void writeErrorFile(
			final Exception exception,
			final File submissionZipErrorFile)
				throws Exception {

		BufferedWriter errorWriter = null;
		if (submissionZipErrorFile == null) {
			rifLogger.warning(this.getClass(), "Unable to create ZIP file exception trace");
			return;
		}
		else if (submissionZipErrorFile.exists()) {
			submissionZipErrorFile.delete();
		}
		
		try {
			rifLogger.info(this.getClass(), "Create ZIP file exception trace: " + 
				submissionZipErrorFile.getAbsolutePath());
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement element : exception.getStackTrace()) {
				sb.append(element.toString() + lineSeparator);
			}
			errorWriter = new BufferedWriter(new FileWriter(submissionZipErrorFile));
			errorWriter.write("EXCEPTION: " + exception.getMessage() + lineSeparator + 
				lineSeparator + "TRACE: " + lineSeparator + sb.toString());
		}
		finally {
			if (errorWriter != null) {
				errorWriter.close();
			}
		}				
	}
	
	private void addCssFile(
			final File temporaryDirectory,
			final ZipOutputStream submissionZipOutputStream,
			final String studyID,
			final String cssFileName) 
			throws Exception {
			
		String cssFileText=readFile(cssFileName);
		rifLogger.info(this.getClass(), "Adding CSS for report file: " + temporaryDirectory.getAbsolutePath() + File.separator + 
			cssFileName + " to ZIP file");
		
		File file=new File(temporaryDirectory.getAbsolutePath() + File.separator + cssFileName);
		ZipEntry zipEntry = new ZipEntry(cssFileName);

		submissionZipOutputStream.putNextEntry(zipEntry);
		byte[] b=cssFileText.toString().getBytes();
		submissionZipOutputStream.write(b, 0, b.length);

		submissionZipOutputStream.closeEntry();	
	}
	
	private void addZipDir(
			final File temporaryDirectory,
			final ZipOutputStream submissionZipOutputStream,
			final String dirName) 
			throws Exception {
			
		String zipDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName + File.separator;
		rifLogger.info(this.getClass(), "Adding " + dirName + " directory: " + zipDirName + 
			" to ZIP file");
		
		ZipEntry zipEntry = new ZipEntry(zipDirName);
		submissionZipOutputStream.putNextEntry(zipEntry);
		submissionZipOutputStream.closeEntry();	
	}
	
	private void addHtmlFile(
			final File temporaryDirectory,
			final ZipOutputStream submissionZipOutputStream,
			final Connection connection,
			final User user,
			final String studyID,
			final Locale locale,
			final String url,
			final String taxonomyServicesServer,
			final String denominatorHTML,
			final String numeratorHTML,
			final String mapHTML) 
			throws Exception {
				
		GetStudyJSON getStudyJSON = new GetStudyJSON(manager);

		StringBuilder htmlFileText=new StringBuilder();
		htmlFileText.append(readFile("RIFStudyHeader.html") + lineSeparator);
		htmlFileText.append("<body>" + lineSeparator);
		htmlFileText.append("<div>" + lineSeparator);
		htmlFileText.append("  <div>" + lineSeparator);
		htmlFileText.append("  <ul class=\"nav\">" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a class=\"active\" href=\"#rif40_studies\">Studies</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_study_status\">Status</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_investigations\">Investigations</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_inv_covariates\">Covariates</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_inv_conditions\">Conditions</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_study_areas\">Study area</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#rif40_comparison_areas\">Comparison area</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#denominator\">Denominator</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#numerator\">Numerator</a></li>" + lineSeparator);
		htmlFileText.append("    <li class=\"nav\"><a href=\"#maps\">Maps</a></li>" + lineSeparator);
		htmlFileText.append("  </ul>" + lineSeparator);
		htmlFileText.append("  </div>" + lineSeparator);
		htmlFileText.append("  <div style=\"margin-left:25%;padding:1px 16px;height:1000px;\">" + lineSeparator);

		addTableToHtmlReport(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			"rif40_studies", // Table 
			null, // Common table expression
			null, // Joined table
			"username,study_id,study_name," + lineSeparator +
			"summary,description,other_notes,study_date," + lineSeparator +
			"geography,study_type," + lineSeparator +
//			"denom_tab,direct_stand_tab,covariate_table," + lineSeparator +
//			"year_start,year_stop," + lineSeparator +
//			"max_age_group,min_age_group," + lineSeparator +
			"study_geolevel_name,comparison_geolevel_name," + lineSeparator +
//			"map_table,extract_table,description" + lineSeparator +
//			"suppression_value,extract_permitted,transfer_permitted,authorised_by,authorised_on,authorised_notes," + lineSeparator +
			"project,project_description," + lineSeparator +
			"CASE WHEN stats_method = 'HET' THEN 'Heterogenous'" + lineSeparator +
			"     WHEN stats_method = 'BYM' THEN 'Besag, York and Mollie'" + lineSeparator +
			"     WHEN stats_method = 'CAR' THEN 'Conditional Auto Regression'" + lineSeparator +
			"     ELSE 'NONE' END AS stats_method", // Column list
			null  	/* GROUP BY */,
			null  	/* ORDER BY */,
			"1"		/* Expected rows */,
			true	/* Rotate */, 1 /* headerLevel */, locale, url);

		addTableToHtmlReport(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			"rif40_study_status", // Table 	
			null, // Common table expression
			null, // Joined table
			"study_state,creation_date,message", // Column list
			null  	/* GROUP BY */,
			"ith_update"    	/* ORDER BY */,
			"1+"		/* Expected rows 0+ */,
			false		/* Rotate */, 1 /* headerLevel */, locale, url);
			
		addTableToHtmlReport(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			"rif40_investigations", // Table 	
			"WITH study_num_denom AS (" + lineSeparator +
			"	SELECT a.study_id, b.geography, b.theme_description," + lineSeparator +
			"		   b.numerator_table, b.numerator_description," + lineSeparator +
			"		   b.denominator_table, b.denominator_description," + lineSeparator +
			"		   dmin.fieldname min_age_group, dmax.fieldname max_age_group" + lineSeparator +
			"	  FROM rif40.rif40_studies a, rif40_num_denom b, rif40.rif40_tables c," + lineSeparator +
			"		   rif40.rif40_age_groups dmin, rif40.rif40_age_groups dmax" + lineSeparator +
			"	 WHERE a.geography       = b.geography" + lineSeparator +
			"	   AND a.denom_tab       = b.denominator_table" + lineSeparator +
			"	   AND b.numerator_table = c.table_name" + lineSeparator +
			"	   AND c.age_group_id    = dmin.age_group_id" + lineSeparator +
			"	   AND c.age_group_id    = dmax.age_group_id" + lineSeparator +
			"	   AND a.min_age_group   = dmin.offset" + lineSeparator +
			"	   AND a.max_age_group   = dmax.offset" + lineSeparator +
			")", // Common table expression
			" LEFT OUTER JOIN study_num_denom d ON (d.study_id = t.study_id)", // Joined table
			"t.inv_id,t.inv_name,t.inv_description,t.year_start,t.year_stop," + lineSeparator +
			"CASE"+ lineSeparator +
			"	WHEN t.genders = 1 THEN 'Males'" + lineSeparator +
			"	WHEN t.genders = 2 THEN 'Females'" + lineSeparator +
			"	WHEN t.genders = 3 THEN 'Males and Females'" + lineSeparator +
			"	ELSE 'Unknown'" + lineSeparator +
			"END AS genders," + lineSeparator +
			"d.theme_description,d.numerator_table, d.numerator_description," + lineSeparator + 
			"d.denominator_table, d.denominator_description," + lineSeparator +
			"d.min_age_group, d.max_age_group," + lineSeparator +
			"t.mh_test_type,t.classifier,t.classifier_bands", // Column list
			null  	/* GROUP BY */,
			"t.inv_id"  	/* ORDER BY */,
			"1+"		/* Expected rows 1+ */,
			true		/* Rotate */, 1 /* headerLevel */, locale, url);
			
		addTableToHtmlReport(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			"rif40_inv_covariates", // Table 
			null, // Common table expression	
			null, // Joined table
			"inv_id,covariate_name,min,max,geography,study_geolevel_name", // Column list
			null  	/* GROUP BY */,
			"inv_id,covariate_name"    	/* ORDER BY */,
			"0+"		/* Expected rows 0+ */,
			false		/* Rotate */, 1 /* headerLevel */, locale, url);
	
		addInvConditions(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			getStudyJSON, locale, 1 /* headerLevel */, url, taxonomyServicesServer);
			
		addStudyAndComparisonAreas(htmlFileText, connection, studyID,
			"rif40", // Owner
			"rif40", // Schema
			getStudyJSON, locale, url);
		
		htmlFileText.append(denominatorHTML);
		htmlFileText.append(numeratorHTML);
		htmlFileText.append(mapHTML);
		
		htmlFileText.append("  </div>" + lineSeparator);
		htmlFileText.append("</div>" + lineSeparator);
		htmlFileText.append("</body>" + lineSeparator);
		htmlFileText.append("</html>" + lineSeparator);
		String htmlFileName="RIFstudy_" + studyID + ".html";
		rifLogger.info(this.getClass(), "Adding HTML report file: " + temporaryDirectory.getAbsolutePath() + File.separator + 
			htmlFileName + " to ZIP file");
		
		File file=new File(temporaryDirectory.getAbsolutePath() + File.separator + htmlFileName);
		ZipEntry zipEntry = new ZipEntry(htmlFileName);

		submissionZipOutputStream.putNextEntry(zipEntry);
		byte[] b=htmlFileText.toString().getBytes();
		submissionZipOutputStream.write(b, 0, b.length);

		submissionZipOutputStream.closeEntry();	

		if (getStudyJSON.getTaxonomyInitialiseError()) {	
			rifLogger.error(this.getClass(), 
				"Taxonomy service still initialising; please run again in 5 minutes");
			throw new Exception("Taxonomy service still initialising; please run again in 5 minutes");
		}		
	}
	
	private String addNumerator(
			final User user,
			final Connection connection,
			final File temporaryDirectory,
			final String studyID,
			final int headerLevel,
			final Locale locale,
			CachedRowSet rif40Studies
			)
			throws Exception {
				
		StringBuilder htmlFileText=new StringBuilder();
		
		htmlFileText.append("    <h" + headerLevel + " id=\"numerator\">Numerator</h" + headerLevel + ">" + lineSeparator);
		htmlFileText.append("    <p>" + lineSeparator);				

		String extractTable=manager.getColumnFromResultSet(rif40Studies, "extract_table");
		int yearStart=Integer.parseInt(manager.getColumnFromResultSet(rif40Studies, "year_start"));
		int yearStop=Integer.parseInt(manager.getColumnFromResultSet(rif40Studies, "year_stop"));
		
		CachedRowSet rif40ExtraxctMaxMinYear=getStudyStartEndYear(connection, extractTable);
		int minYear=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "min_year"));
		int maxYear=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "max_year"));
		int minSex=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "min_sex"));
		int maxSex=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "max_sex"));
				
		SQLGeneralQueryFormatter investigationsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		investigationsQueryFormatter.addQueryLine(0, "SELECT inv_id, inv_name, inv_description");
		investigationsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_investigations");
		investigationsQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		investigationsQueryFormatter.addQueryLine(0, " ORDER BY inv_id");
	
		PreparedStatement statement = manager.createPreparedStatement(connection, investigationsQueryFormatter);
		try {	
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				int rowCount=0;
				do {	
					rowCount++;
					String invId=resultSet.getString(1);
					String invName=resultSet.getString(2);
					String invDescription=resultSet.getString(3);
					if (invDescription == null) {
						invDescription="(No description)";
					}
					
					htmlFileText.append("      <h" + (headerLevel+1) + 
						">Numerator by year for investigation " + rowCount + " id: " + invId + "</h" + 
						(headerLevel+1) + ">" + lineSeparator);
					htmlFileText.append("      <p>" + invDescription + lineSeparator);
					
					String[] queryArgs = new String[6];
					queryArgs[0]="rif_studies." + extractTable; // 1: Extract table name; e.g. s367_extract
					queryArgs[1]=invName;						// 2: Investigation field name; e.g. test_1002
					queryArgs[2]=Integer.toString(yearStart);	// 3: Min year
					queryArgs[3]=Integer.toString(yearStop);	// 4: Max year
					queryArgs[4]=Integer.toString(minSex);		// 5: Min sex
					queryArgs[5]=Integer.toString(maxSex);		// 6: Max sex 		
					addTableToHtmlReport(htmlFileText, connection, null /* studyID */,
						"rif40",		// Owner
						"rif_studies",	// Schema
						extractTable, 	// Table
						"numeratorReport.sql", // queryFileName
						queryArgs,
						"Numerator by year report for study: " + studyID + "; investigation: " + invDescription, // Title
						"1+"		/* Expected rows 0+ */,
						false		/* Rotate */, (headerLevel+1) /* headerLevel */, locale /* locale */, null /* tomcatServer */); 

					htmlFileText.append("      </p>" + lineSeparator);		
				} while (resultSet.next());
			}
			else {
				throw new Exception("No investigations found for study: " + studyID);
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + investigationsQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		htmlFileText.append("    </p>" + lineSeparator);
		
		return htmlFileText.toString();		
	}
					
	private String addDenominator(
			final User user,
			final Connection connection,
			final File temporaryDirectory,
			final String studyID,
			final int headerLevel,
			final Locale locale,
			CachedRowSet rif40Studies
			)
			throws Exception {
					
		StringBuilder htmlFileText=new StringBuilder();
		
		GetStudyJSON getStudyJSON = new GetStudyJSON(manager);
		RIFGraphics rifGraphics = new RIFGraphics(rifServiceStartupOptions, manager);
		
		JSONObject studyData=getStudyJSON.getStudyData(
			connection, studyID);
		int yearStart=studyData.getInt("year_start");
		int yearStop=studyData.getInt("year_stop");
		String svgCss=readFile("RIFPopulationPyramid.css");

		htmlFileText.append("    <h" + headerLevel + " id=\"denominator\">Denominator</h" + headerLevel + ">" + lineSeparator);
		htmlFileText.append("      <h" + (headerLevel+1) + ">Denominator by year</h" + (headerLevel+1) + ">" + lineSeparator);
		htmlFileText.append("      <p>" + lineSeparator);
		htmlFileText.append("      </p>" + lineSeparator);		

		String denominatorDirName=addDirToTemporaryDirectoryPath(user, studyID, 
			"reports" + File.separator + "denominator");
		String extractTable=manager.getColumnFromResultSet(rif40Studies, "extract_table");
		String denominatorTable=manager.getColumnFromResultSet(rif40Studies, "denom_tab");
		String studyDescription=manager.getColumnFromResultSet(rif40Studies, "description",
			true /* allowNulls */, false /* allowNoRows */);
		if (studyDescription == null) {
			studyDescription=manager.getColumnFromResultSet(rif40Studies, "study_name",
				true /* allowNulls */, false /* allowNoRows */);		
			if (studyDescription == null) {
				studyDescription="No study name or description";
			}
		}
		CachedRowSet rif40ExtraxctMaxMinYear=getStudyStartEndYear(connection, extractTable);
		int minYear=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "min_year"));
		int maxYear=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "max_year"));
		int minSex=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "min_sex"));
		int maxSex=Integer.parseInt(manager.getColumnFromResultSet(rif40ExtraxctMaxMinYear, "max_sex"));
		String[] queryArgs = new String[5];
		queryArgs[0]="rif_studies." + extractTable; // 1: Extract table name; e.g. s367_extract
		queryArgs[1]=Integer.toString(yearStart);	// 2: Min year
		queryArgs[2]=Integer.toString(yearStop);	// 3: Max year
		queryArgs[3]=Integer.toString(minSex);		// 4: Min sex
		queryArgs[4]=Integer.toString(maxSex);		// 5: Max sex 		
		addTableToHtmlReport(htmlFileText, connection, null /* studyID */,
			"rif40",		// Owner
			"rif_studies",	// Schema
			extractTable, // Table
			"denominatorReport.sql", // queryFileName
			queryArgs,
			"Denominator by year report for study: " + studyID, // Title
			"1+"		/* Expected rows 0+ */,
			false		/* Rotate */, (headerLevel+1) /* headerLevel */, locale /* locale */, null /* tomcatServer */); 

		htmlFileText.append("      <h" + (headerLevel+1) + ">Population Pyramids</h" + (headerLevel+1) + ">" + lineSeparator);
		htmlFileText.append("      <p>" + lineSeparator);		
		htmlFileText.append("      <div>" + lineSeparator);
		htmlFileText.append("        <form id=\"downloadForm\" method=\"get\" action=\"reports\\denominator\\RIFdenominator_pyramid_" + 
			studyID + "_" + printingDPI + "dpi_" + minYear + ".png\">" + lineSeparator);
		htmlFileText.append("        Year: <select id=\"populationPyramidList\">" + lineSeparator);					

		for (int i=minYear; i<=maxYear; i++) {
			if (i == yearStart) { // Selected
				htmlFileText.append("          <option value=\"reports\\denominator\\RIFdenominator_pyramid_" + 
					studyID + "_" + printingDPI + "dpi_" + i + ".png\" selected />" + i + "</option>" + 
					lineSeparator);

			}
			else {
				htmlFileText.append("          <option value=\"reports\\denominator\\RIFdenominator_pyramid_" + 
					studyID + "_" + printingDPI + "dpi_" + i + ".png\" />" + i + "</option>" + lineSeparator);

			}

			String svgText=rifGraphics.getPopulationPyramid(connection, extractTable, denominatorTable, 
				studyDescription, studyID, i,
				true /* treeForm: true - classic tree form; false: stack to right */,
				false /* enablePostscript: setting to true also disables the gradients */);	
			rifGraphics.addSvgFile(
				temporaryDirectory,
				"reports" + File.separator + "denominator",
				"RIFdenominator_treepyramid_",
				studyID,
				i,
				svgText); 
			svgText=rifGraphics.getPopulationPyramid(connection, extractTable, denominatorTable, 
				studyDescription, studyID, i,
				false /* treeForm: true - classic tree form; false: stack to right */,
				false /* enablePostscript: setting to true also disables the gradients */);	
			rifGraphics.addSvgFile(
				temporaryDirectory,
				"reports" + File.separator + "denominator",
				"RIFdenominator_pyramid_",
				studyID,
				i,
				svgText); 	
			svgText=rifGraphics.getPopulationPyramid(connection, extractTable, denominatorTable, 
				studyDescription, studyID, i,
				true /* treeForm: true - classic tree form; false: stack to right */,
				true /* enablePostscript: setting to true also disables the gradients */);	
			rifGraphics.addSvgFile(
				temporaryDirectory,
				"reports" + File.separator + "denominator",
				"FOPdenominator_treepyramid_",
				studyID,
				i,
				svgText); 
			svgText=rifGraphics.getPopulationPyramid(connection, extractTable, denominatorTable, 
				studyDescription, studyID, i,
				false /* treeForm: true - classic tree form; false: stack to right */,
				true /* enablePostscript: setting to true also disables the gradients */);	
			rifGraphics.addSvgFile(
				temporaryDirectory,
				"reports" + File.separator + "denominator",
				"FOPdenominator_pyramid_",
				studyID,
				i,
				svgText); 
		}
		htmlFileText.append("        </select>" + lineSeparator);
		
		htmlFileText.append("        Graphics Format: <select id=\"populationPyramidFileType\">" + lineSeparator);
		Set<RIFGraphicsOutputType> htmlOutputTypes = EnumSet.of( // Can be viewed in browser
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_PNG,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_JPEG,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_TIFF,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_SVG);
		Iterator <RIFGraphicsOutputType> htmlOutputTypeIter = htmlOutputTypes.iterator();
		int j=0;
		while (htmlOutputTypeIter.hasNext()) {
			String selected="";
			String disabled="";
			RIFGraphicsOutputType outputType=htmlOutputTypeIter.next();
			j++;
			if (outputType.getGraphicsExtentsion().equals("png")) {
				selected="selected";
			}
			if (!outputType.isRIFGraphicsOutputTypeEnabled()) {
				disabled="disabled";
			}
			htmlFileText.append("          <option value=\"" + 
				outputType.getGraphicsExtentsion() +
				"\" " + disabled + " " +
				"id=\"" + outputType.getRIFGraphicsOutputTypeShortName().toLowerCase() + "Select\" " + 
				"title=\"" + outputType.getRIFGraphicsOutputTypeDescription() + "\" " + 
				selected + " />" + outputType.getRIFGraphicsOutputTypeShortName() + " (" + 
					outputType.getRIFGraphicsOutputTypeDescription() +
				")</option>" + lineSeparator);
		}
		htmlFileText.append("        </select>" + lineSeparator);
		
		htmlFileText.append("        Pyramid type: <select id=\"populationPyramidType\">" + lineSeparator);
		htmlFileText.append("          <option value=\"tree\" title=\"Tree\" />Tree</option>" + lineSeparator);
		htmlFileText.append("          <option value=\"stackedRight\" tile=\"Stacked to the right\" selected />Stacked to the right</option>" + lineSeparator);
		htmlFileText.append("        </select>" + lineSeparator);
		

		htmlFileText.append("          <button id=\"downloadButton\" type=\"submit\">Download PNG</button>" + lineSeparator);
		htmlFileText.append("        </form>" + lineSeparator);
		htmlFileText.append("      </div>" + lineSeparator);
		htmlFileText.append("      <img src=\"reports\\denominator\\RIFdenominator_pyramid_" + 
			studyID + "_" + printingDPI +	"dpi_" + minYear + 
			".png\" id=\"denominator_pyramid\" width=\"80%\" />" + lineSeparator);
		htmlFileText.append("    </p>" + lineSeparator);
		
		for (int year=minYear; year<=maxYear; year++) {
			Set<RIFGraphicsOutputType> allOutputTypes = EnumSet.of(
				RIFGraphicsOutputType.RIFGRAPHICS_JPEG,
				RIFGraphicsOutputType.RIFGRAPHICS_PNG,
				RIFGraphicsOutputType.RIFGRAPHICS_TIFF,    // Requires 1.9.2 or higher Batik
				RIFGraphicsOutputType.RIFGRAPHICS_EPS,
				RIFGraphicsOutputType.RIFGRAPHICS_PS);
			Iterator <RIFGraphicsOutputType> allOutputTypeIter = allOutputTypes.iterator();
			while (allOutputTypeIter.hasNext()) {
				RIFGraphicsOutputType outputType=allOutputTypeIter.next();
				if (outputType.isRIFGraphicsOutputTypeEnabled()) {	
					if (outputType.doesRIFGraphicsOutputTypeUseFop()) {	
						rifGraphics.addGraphicsFile(
							temporaryDirectory,							/* Study scratch space diretory */
							"reports" + File.separator + "denominator", /* directory */
							"FOPdenominator_treepyramid_", 				/* File prefix */
							studyID,
							year,
							outputType,
							denominatorPyramidWidthPixels);
						rifGraphics.addGraphicsFile(
							temporaryDirectory,							/* Study scratch space diretory */
							"reports" + File.separator + "denominator", /* directory */
							"FOPdenominator_pyramid_", 					/* File prefix */
							studyID,
							year,
							outputType,
							denominatorPyramidWidthPixels);
					}
					else {
						rifGraphics.addGraphicsFile(
							temporaryDirectory,							/* Study scratch space diretory */
							"reports" + File.separator + "denominator", /* directory */
							"RIFdenominator_treepyramid_", 				/* File prefix */
							studyID,
							year,
							outputType,
							denominatorPyramidWidthPixels);
						rifGraphics.addGraphicsFile(
							temporaryDirectory,							/* Study scratch space diretory */
							"reports" + File.separator + "denominator", /* directory */
							"RIFdenominator_pyramid_", 					/* File prefix */
							studyID,
							year,
							outputType,
							denominatorPyramidWidthPixels);
					}
				}
			}				
		}	
		
		return htmlFileText.toString();
	}
		
	private CachedRowSet getStudyStartEndYear(
			final Connection connection,
			final String extractTable)
			throws Exception {
		SQLGeneralQueryFormatter extractTableQueryFormatter = new SQLGeneralQueryFormatter();		
		
		extractTableQueryFormatter.addQueryLine(0, "SELECT MIN(year) AS min_year, MAX(year) AS max_year,");
		extractTableQueryFormatter.addQueryLine(0, "       MIN(sex) AS min_sex, MAX(sex) AS max_sex");
		extractTableQueryFormatter.addQueryLine(0, "  FROM rif_studies." + extractTable.toLowerCase());
		extractTableQueryFormatter.addQueryLine(0, " WHERE study_or_comparison = 'S'");

		CachedRowSet cachedRowSet=manager.createCachedRowSet(connection, extractTableQueryFormatter,
			"getStudyStartEndYear");	
		
		return cachedRowSet;
	}
	
	private CachedRowSet getRif40Studies(
			final Connection connection,
			final String studyID)
			throws Exception {
		SQLGeneralQueryFormatter rif40StudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		
		rif40StudiesQueryFormatter.addQueryLine(0, "SELECT study_id, extract_table, map_table, denom_tab,");
		rif40StudiesQueryFormatter.addQueryLine(0, "       study_name, description, year_start, year_stop, select_state, print_state");
		rif40StudiesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_studies");
		rif40StudiesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");

		int[] params = new int[1];
		params[0]=Integer.parseInt(studyID);
		CachedRowSet cachedRowSet=manager.createCachedRowSet(connection, rif40StudiesQueryFormatter,
			"getRif40Studies", params);	
		
		return cachedRowSet;
	}

	/*
	 * Fetch inv_id, inv_name, inv_description, genders, numer_tab, year_start, year_stop, 
	 * min_age_group, max_age_group from RIF40_INVESTIGATIONS for study
	 *
	 * @param: Connection connection,
	 * @param: String studyID
	 *
	 * @returns: CachedRowSet
	 */
	private CachedRowSet getRif40Investigations(
			final Connection connection,
			final String studyID)
			throws Exception {
		SQLGeneralQueryFormatter rif40StudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		
		rif40StudiesQueryFormatter.addQueryLine(0, "SELECT inv_id, inv_name, inv_description,");
		rif40StudiesQueryFormatter.addQueryLine(0, "       genders, numer_tab,");
		rif40StudiesQueryFormatter.addQueryLine(0, "       year_start, year_stop, min_age_group, max_age_group");
		rif40StudiesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_investigations");
		rif40StudiesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");

		int[] params = new int[1];
		params[0]=Integer.parseInt(studyID);
		
		return manager.createCachedRowSet(connection, rif40StudiesQueryFormatter,
			"getRif40Investigations", params);
	}	
	
	/*
	 * Fetch study_geolevel_name, comparison_geolevel_name, geography from RIF40_STUDIES for study
	 *
	 * @param: Connection connection,
	 * @param: String studyID
	 *
	 * @returns: CachedRowSet
	 */	
	private void addStudyAndComparisonAreas(
			final StringBuilder htmlFileText,
			final Connection connection,
			final String studyID,
			final String ownerName,
			final String schemaName,
			final GetStudyJSON getStudyJSON,
			final Locale locale,
			final String url)
			throws Exception {
		SQLGeneralQueryFormatter studyAndComparisonReportQueryFormatter = new SQLGeneralQueryFormatter();		
		
		ResultSet resultSet = null;
		
		studyAndComparisonReportQueryFormatter.addQueryLine(0, "SELECT study_geolevel_name, comparison_geolevel_name, geography");
		studyAndComparisonReportQueryFormatter.addQueryLine(0, "  FROM " + schemaName + ".rif40_studies");
		studyAndComparisonReportQueryFormatter.addQueryLine(0, " WHERE study_id = ?");

		PreparedStatement statement = manager.createPreparedStatement(connection, studyAndComparisonReportQueryFormatter);
		try {
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			String studyGeolevelName=null;
			String comparisonGeolevelName=null;
			String geographyName=null;
			
			if (resultSet.next()) {
				studyGeolevelName=resultSet.getString(1);
				comparisonGeolevelName=resultSet.getString(2);
				geographyName=resultSet.getString(3);	
				JSONObject studyGeolevel=getStudyJSON.getLookupTableName(
					connection, studyGeolevelName, geographyName);
				JSONObject comparisonGeolevel=getStudyJSON.getLookupTableName(
					connection, comparisonGeolevelName, geographyName);
		
				addTableToHtmlReport(htmlFileText, connection, studyID,
					ownerName,	// Owner
					schemaName,	// Schema
					"rif40_study_areas", // Table
					null, // Common table expression
					"LEFT OUTER JOIN rif_data." +			
						studyGeolevel.getString("lookup_table").toLowerCase() + 
						" b ON (t.area_id = b." + studyGeolevelName.toLowerCase() + ")", // Joined table 	
					"t.area_id, t.band_id, b." + 
						studyGeolevel.getString("lookup_desc_column").toLowerCase() + 
						" AS label", // Column list
					null  	/* GROUP BY */,
					"2, 1"    	/* ORDER BY */,
					"1+"		/* Expected rows 0+ */,
					false		/* Rotate */, 1 /* headerLevel */, locale, url);
	
				addTableToHtmlReport(htmlFileText, connection, studyID,
					ownerName,
					schemaName,
					"rif40_comparison_areas", // Table
					null, // Common table expression
					"LEFT OUTER JOIN rif_data." +			
						comparisonGeolevel.getString("lookup_table").toLowerCase() + 
						" b ON (t.area_id = b." + comparisonGeolevelName.toLowerCase() + ")", // Joined table 	
					"t.area_id, b." + 
						comparisonGeolevel.getString("lookup_desc_column").toLowerCase() + 
						" AS label", // Column list
					null  	/* GROUP BY */,
					"1"    	/* ORDER BY */,
					"1+"		/* Expected rows 0+ */,
					false		/* Rotate */, 1 /* headerLevel */, locale, url);
			}		
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + studyAndComparisonReportQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}	
	}
	
	private String getTableComment(Connection connection, String schemaName, String tableName,
		String defaultComment)
			throws Exception {
		SQLGeneralQueryFormatter tableCommentQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		if (databaseType == DatabaseType.POSTGRESQL) {
			tableCommentQueryFormatter.addQueryLine(0, // Postgres
				"SELECT obj_description('" + schemaName + "." + tableName + "'::regclass) AS table_comment");
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			tableCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS table_comment"); // SQL Server
			tableCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'table', ?, NULL, NULL)");
			tableCommentQueryFormatter.addQueryLine(0, "UNION");
			tableCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS table_comment");
			tableCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'view', ?, NULL, NULL)");
		}
		else {
			throw new Exception("getTableComment(): invalid databaseType: " + 
				databaseType);
		}
		PreparedStatement statement = manager.createPreparedStatement(connection,
				tableCommentQueryFormatter);
		
		String tableComment=tableName;
		try {			
			
			if (databaseType == DatabaseType.SQL_SERVER) {
				statement.setString(1, schemaName);	
				statement.setString(2, tableName);
				statement.setString(3, schemaName);	
				statement.setString(4, tableName);	
			}			
			resultSet = statement.executeQuery();
			if (resultSet.next()) {		
				tableComment=resultSet.getString(1);
				if (tableComment == null) {
					tableComment=defaultComment;
				}
				if (resultSet.next()) {		
					throw new Exception("getTableComment(): expected 1 row, got >1");
				}
			}
			else {
				rifLogger.warning(this.getClass(), "getTableComment(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + tableCommentQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		return tableComment;
	}

	private void addInvConditions(
			final StringBuilder htmlFileText,
			final Connection connection,
			final String studyID,
			final String ownerName,
			final String schemaName,
			final GetStudyJSON getStudyJSON,
			final Locale locale,
			final int headerLevel,
			final String url,
			final String taxonomyServicesServer)
			throws Exception {
			
		String tableName="rif40_inv_conditions";	
		String tableComment=getTableComment(connection, "rif40", tableName, null);
		
		SQLGeneralQueryFormatter invConditionsQueryFormatter = new SQLGeneralQueryFormatter();		
				
		invConditionsQueryFormatter.addQueryLine(0, "SELECT inv_id,numer_tab,min_condition,max_condition,");
		invConditionsQueryFormatter.addQueryLine(0, "       outcome_group_name,condition");
		invConditionsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_inv_conditions");
		invConditionsQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		invConditionsQueryFormatter.addQueryLine(0, " ORDER BY inv_id,line_number");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				invConditionsQueryFormatter);
		ResultSet resultSet = null;
		htmlFileText.append("    <h" + headerLevel + " id=\"" + tableName + "\">Conditions</h" + headerLevel + ">" + lineSeparator);
			
		try {
			int rowCount = 0;
				
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				StringBuffer headerText = new StringBuffer();
				String minCondition;
				String maxCondition;
				String outComeType;
						
				Boolean endSpan=false;
				String invId="";
				String numerTab="";
				int spanCount=0;
				
				String[] commentArray = new String[columnCount+1];
				int commentArrayLength=0;
					
				htmlFileText.append("    <p>" + lineSeparator);
				htmlFileText.append("      <table id=\"" + tableName + "_table\" border=\"1\" summary=\"" + tableName + "\">" +  lineSeparator);
				htmlFileText.append("        <caption><em>" + tableComment + "</em></caption>" + 
					lineSeparator);

				headerText.append("        <tr>" + lineSeparator);
				do {	
					rowCount++;
					
					String statementNumber=null;
					StringBuffer bodyText = new StringBuffer();
					minCondition="";
					maxCondition=null;
					outComeType="";
					
					bodyText.append("        <tr>" + lineSeparator);
					ArrayList<String> conditionList = new ArrayList<String>();
					ArrayList<String> descriptionList = new ArrayList<String>();
					ArrayList<String> outcomeTypeList = new ArrayList<String>();
					
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);	
						String columnType = rsmd.getColumnTypeName(i);
						
						if (value == null) {
							value="&nbsp;";
						}
						
						if (name.equals("min_condition")) {
							if (!value.equals("&nbsp;")) {
								JSONObject taxonomyObject = getStudyJSON.getHealthCodeDescription(url, taxonomyServicesServer, value);
								minCondition=taxonomyObject.getString("description");
							}
						}
						else if (name.equals("max_condition")) {
							if (!value.equals("&nbsp;")) {
								// Add: please run again in 5 minutes support
								JSONObject taxonomyObject = getStudyJSON.getHealthCodeDescription(url, taxonomyServicesServer, value);
								maxCondition=taxonomyObject.getString("description");
							}
						}						
						else if (name.equals("outcome_group_name")) {
							if (!value.equals("&nbsp;")) {
								outComeType=getOutcomeType(connection, value);
							}
						}
						else {
							if (rowCount == 1) {
								
								String columnComment=manager.getColumnComment(connection,
									schemaName, tableName, name /* Column name */);
								if (name.equals("numer_tab")) {
									name="numerator_table";
								}
								headerText.append("          <th title=\"" + columnComment + "\">" + 
									name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ") + 
									"<!-- " + columnType + " -->" + "</th>" + lineSeparator);
						
								commentArray[commentArrayLength]="        <li class=\"dictionary\"><em>" + name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ") +
									"</em>: " + columnComment + "</li>" + lineSeparator;
								commentArrayLength++;
							}
							
							bodyText.append("          <td>" + value + 
								"       </td><!-- Column: " + i +
								"; row: " + rowCount +
								" -->" + lineSeparator);
							if (name.equals("inv_id") && invId.equals("")) {
								invId=value;
							}
							else if (name.equals("numer_tab") && numerTab.equals("")) {
								numerTab=value;
							}
							else if (name.equals("inv_id") && !invId.equals(value)) {
								bodyText.append("          <!-- Detect end span, name: " + name +
									"; old value: " + invId + "; new value: " + value + " -->" + 
									lineSeparator);
								endSpan=true;
								invId=value;
							}	
							else if (name.equals("numer_tab") && !numerTab.equals(value)) {
								bodyText.append("          <!-- Detect end span, name: " + name +
									"; old value: " + numerTab + "; new value: " + value + " -->" + 
									lineSeparator);
								endSpan=true;
								numerTab=value;
							}	
							else if (name.equals("condition")) {
								conditionList.add(value);
							}
						}
				
					}
					
					if (rowCount == 1) {
						headerText.append("          <th title=\"Taxonomy outcome type\">Outcome type</th>" + lineSeparator);
						headerText.append("          <th title=\"Taxonomy description\">Description</th>" + lineSeparator);
						headerText.append("        </tr>" + lineSeparator);
						htmlFileText.append(headerText.toString());
					}

					bodyText.append("          <td>");
					
					if (outComeType != null) {
						bodyText.append(outComeType);
					}
					else {
						bodyText.append("&nbsp;");
					}
					bodyText.append("</td>" + lineSeparator);
					outcomeTypeList.add(outComeType);
					
					bodyText.append("          <td>" + minCondition);
					if (maxCondition != null) {
						bodyText.append(" - " + maxCondition);
						descriptionList.add(minCondition + " - " + maxCondition);
					}
					else {
						descriptionList.add(minCondition);
					}
					bodyText.append("</td>" + lineSeparator);
					
					bodyText.append("        </tr>" + lineSeparator);
					
					if (endSpan) {
						bodyText.append("        <!-- endSpan: " + spanCount + 
							"; outcomeTypeList: " + outcomeTypeList.size() + 
							"; conditionList: " + conditionList.size() + 
							"; descriptionList: " + descriptionList.size() + 
							" -->" + lineSeparator);
						endSpan=false;
						outcomeTypeList.clear();
						conditionList.clear();
						descriptionList.clear();
						spanCount=0;
					}
					else {
						spanCount++;
					}
					htmlFileText.append(bodyText.toString());
				} while (resultSet.next());
				
				htmlFileText.append("      </table>" + lineSeparator);
				
				htmlFileText.append("    </p>" + lineSeparator);
				htmlFileText.append("    <p>" + lineSeparator);
				
				commentArray[commentArrayLength]="        <li class=\"dictionary\"><em>Outcome type</em>: Taxonmomy Outcome type</li>" + lineSeparator;
				commentArray[commentArrayLength]="        <li class=\"dictionary\"><em>Description</em>: Taxonmomy description</li>" + lineSeparator;
				commentArrayLength++;
				htmlFileText.append("      <ul class=\"dictionary\">" + lineSeparator);
				for (int j = 0; j < commentArrayLength; j++) {
					htmlFileText.append(commentArray[j]);
				}
				htmlFileText.append("      </ul>" + lineSeparator);	
				htmlFileText.append("    </p>" + lineSeparator);		
			}
			else {
				htmlFileText.append("    <p>No data found</p>" + lineSeparator);
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + invConditionsQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}	
	}	
	
	/**
	 * Get outcome type. Will return the current ontology version e.g. icd10 even if icd9 codes 
	 * are actually being used
	 *
     * @param String outcome_group_name (required)
	 * @return outcome type string
     */	
	private String getOutcomeType(Connection connection, String outcome_group_name) 
					throws Exception {
		SQLGeneralQueryFormatter rifOutcomeGroupsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		if (outcome_group_name == null) {
			throw new Exception("Null outcome_group_name");
		}
		
 		rifOutcomeGroupsQueryFormatter.addQueryLine(0, 
			"SELECT a.outcome_type, b.current_version"); 
		rifOutcomeGroupsQueryFormatter.addQueryLine(0, 
			"  FROM rif40.rif40_outcome_groups a, rif40.rif40_outcomes b"); 
		rifOutcomeGroupsQueryFormatter.addQueryLine(0, 
			" WHERE a.outcome_group_name = ? AND a.outcome_type = b.outcome_type");
		PreparedStatement statement = manager.createPreparedStatement(connection,
			rifOutcomeGroupsQueryFormatter);
		String outcomeGroup=null;
		try {			
			statement.setString(1, outcome_group_name);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				outcomeGroup=resultSet.getString(1) + resultSet.getString(2);
				if (resultSet.next()) {
					throw new Exception("getOutcomeType(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("getOutcomeType(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifOutcomeGroupsQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		return outcomeGroup;
	}	
	
	private void addTableToHtmlReport(
			final StringBuilder htmlFileText,
			final Connection connection,
			final String studyID,
			final String ownerName,
			final String schemaName,
			final String tableName,
			final String commonTableExpression,
			final String joinedTable,
			final String columnList,
			final String groupBy,
			final String orderBy,
			final String expectedRows,
			final boolean rotate,
			final int headerLevel,
			final Locale locale,
			final String url)
			throws Exception {
		
		String tableHeader=tableName.replace("rif40_", "");
		if (tableHeader.substring(0, 4).equals("inv_")) {
			tableHeader=tableName.replace("rif40_inv_", "");
		}
		htmlFileText.append("    <h" + headerLevel + " id=\"" + tableName + "\">" + 
			tableHeader.substring(0, 1).toUpperCase() + tableHeader.substring(1).replace("_", " ") + 
			"</h" + headerLevel + ">" + lineSeparator);
		String valueLavel="Value";
		if (tableHeader.length() > 2 && 
		    tableHeader.substring(tableHeader.length()-1, tableHeader.length()).
				equals("s")) {
			valueLavel=tableHeader.substring(0, 1).toUpperCase() + 
				tableHeader.substring(1, tableHeader.length()-1).
					replace("_", " ");
		}
		else if (tableHeader.equals("Studies")) {
			valueLavel="Study";
		}
		SQLGeneralQueryFormatter htmlReportQueryFormatter = new SQLGeneralQueryFormatter();	
		if (commonTableExpression != null) {
			htmlReportQueryFormatter.addQueryLine(0, commonTableExpression);
		}
		htmlReportQueryFormatter.addQueryLine(0, "SELECT " + columnList);
		htmlReportQueryFormatter.addQueryLine(0, "  FROM " + schemaName + "." + tableName + " t ");
			// Note the alias is always t!
		if (joinedTable != null) {
			htmlReportQueryFormatter.addQueryLine(0, joinedTable);
		}
		else {
			htmlReportQueryFormatter.addQueryLine(0, "/* No joined table */");
		}
		if (studyID != null) {
			htmlReportQueryFormatter.addQueryLine(0, " WHERE t.study_id = ?");
		}
		if (groupBy != null) {
			htmlReportQueryFormatter.addQueryLine(0, " GROUP BY " + groupBy);
		}
		if (orderBy != null) {
			htmlReportQueryFormatter.addQueryLine(0, " ORDER BY " + orderBy);
		}
		String defaultTitle="Report: " + tableName + " for study: " + studyID;
		
		executeHTmlReport(htmlFileText, connection, htmlReportQueryFormatter, expectedRows,
			rotate, studyID, schemaName, tableName, valueLavel, defaultTitle, locale, url);
	}

	private void addTableToHtmlReport(
			final StringBuilder htmlFileText,
			final Connection connection,
			final String studyID,
			final String ownerName,
			final String schemaName,
			final String tableName,
			final String queryFileName,
			final String[] queryArgs,
			final String title,
			final String expectedRows,
			final boolean rotate,
			final int headerLevel,
			final Locale locale,
			final String url)
			throws Exception {
		
		String tableHeader=tableName.replace("rif40_", "");
		if (tableHeader.substring(0, 4).equals("inv_")) {
			tableHeader=tableName.replace("rif40_inv_", "");
		}
		htmlFileText.append("    <h" + headerLevel + " id=\"" + tableName + "\">" + 
			tableHeader.substring(0, 1).toUpperCase() + tableHeader.substring(1).replace("_", " ") + 
			"</h" + headerLevel + ">" + lineSeparator);
		String valueLavel="Value";
		if (tableHeader.length() > 2 && 
		    tableHeader.substring(tableHeader.length()-1, tableHeader.length()).
				equals("s")) {
			valueLavel=tableHeader.substring(0, 1).toUpperCase() + 
				tableHeader.substring(1, tableHeader.length()-1).
					replace("_", " ");
		}
		else if (tableHeader.equals("Studies")) {
			valueLavel="Study";
		}
		SQLGeneralQueryFormatter htmlReportQueryFormatter = new SQLGeneralQueryFormatter();	
		htmlReportQueryFormatter.createQueryFromFile(queryFileName, queryArgs, databaseType);
		
		executeHTmlReport(htmlFileText, connection, htmlReportQueryFormatter, expectedRows,
			rotate, studyID, schemaName, tableName, valueLavel, title, locale, url);
	}
		
	private void executeHTmlReport(
			final StringBuilder htmlFileText,
			Connection connection, 
			SQLGeneralQueryFormatter htmlReportQueryFormatter,
			final String expectedRows,
			final boolean rotate,
			String studyID,
			final String schemaName,
			final String tableName,
			final String valueLavel,
			final String defaultTitle,
			final Locale locale,
			final String url)
				throws Exception {	

		if (locale == null) {
			throw new Exception("locale is null");
		}
		
		RifLocale rifLocale = new RifLocale(locale);			
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();
		
		String tableComment=getTableComment(connection, schemaName, tableName, defaultTitle);
		
		ResultSet resultSet = null;
		PreparedStatement statement = manager.createPreparedStatement(connection,
				htmlReportQueryFormatter);
		try {			
			int rowCount = 0;
				
			if (studyID != null) {
				statement.setInt(1, Integer.parseInt(studyID));	
			}	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				StringBuffer headerText = new StringBuffer();
				
				String[] commentArray = new String[columnCount];
					
				htmlFileText.append("    <p>" + lineSeparator);
				htmlFileText.append("      <table id=\"" + tableName + "_table\" border=\"1\" summary=\"" + tableName + "\">" +  lineSeparator);
				htmlFileText.append("        <caption><em>" + tableComment + "</em></caption>" + 
					lineSeparator);

				if (rotate) {
					headerText.append("        <tr>" + lineSeparator +
						"          <th>Attribute</th>" + lineSeparator);	
				}
				else {
					headerText.append("        <tr>" + lineSeparator);
				}
				do {	
					rowCount++;
					
					String statementNumber=null;
					StringBuffer bodyText = new StringBuffer();
					String[] rotatedRowsArray = new String[columnCount];
					
					if (!rotate) {
						bodyText.append("        <tr>" + lineSeparator);
					}
					else {
						headerText.append("          <th>" + valueLavel + ": " + rowCount + "</th>" + lineSeparator);
					}
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);	
						String columnType = rsmd.getColumnTypeName(i);
						
						if (columnType.equals("timestamp") ||
						    columnType.equals("timestamptz") ||
						    columnType.equals("datetime")) {
							Timestamp dateTimeValue=resultSet.getTimestamp(i, calendar);
							value=df.format(dateTimeValue) + "<!-- DATE: " + name + " -->";
						}
						else if (name.equals("year_start") || 
								 name.equals("year_stop") || 
								 name.equals("year")) {
							value = value + " <!-- YEAR: " + name + " -->";	// Do not process value
						}
						else if (value != null && (
								 columnType.equals("integer") || 
							     columnType.equals("bigint") || 
								 columnType.equals("int4") ||
								 columnType.equals("int") ||
								 columnType.equals("smallint"))) {
							try {
								Long longVal=Long.parseLong(value);
								value=NumberFormat.getNumberInstance(locale).format(longVal) + 
									" <!-- LONG: " + name + " -->";
							}
							catch (Exception exception) {
								rifLogger.error(this.getClass(), "Unable to parseLong(" + 
									columnType + "): " + value,
									exception);
								throw exception;
							}
						}
						else if (value != null && (
								 columnType.equals("float") || 
							     columnType.equals("float8") || 
							     columnType.equals("double precision") ||
							     columnType.equals("numeric"))) {
							try {
								Double doubleVal=Double.parseDouble(value);
								value=NumberFormat.getNumberInstance(locale).format(doubleVal) + 
									" <!-- Double: " + name + " -->";
							}
							catch (Exception exception) {
								rifLogger.error(this.getClass(), "Unable to parseDouble(" + 
									columnType + "): " + value,
									exception);
								throw exception;
							}
						}						
						else if (value == null) {
							value="&nbsp;";
						}
						
						if (rowCount == 1) {
							
							String columnComment=manager.getColumnComment(connection,
								schemaName, tableName, name /* Column name */);
							if (rotate) {
								rotatedRowsArray[i-1]="          <td title=\"" + columnComment + "\">" + 
									name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ") + 
									"<!-- " + columnType + " -->" +
									"          </td>" +
									lineSeparator; //Initialise
							}
							else {
								headerText.append("          <th title=\"" + columnComment + "\">" + 
									name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ") + 
									"<!-- " + columnType + " -->" + "</th>" + lineSeparator);
							}
							commentArray[i-1]="        <li class=\"dictionary\"><em>" + name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ") +
								"</em>: " + columnComment + "</li>" + lineSeparator;
						}
						
						if (rotate) {
							rotatedRowsArray[i-1]+="          <td>" + value + "</td><!-- Column: " + i +
								"; row: " + rowCount +
								" -->" + lineSeparator;
						}
						else {
							bodyText.append("          <td>" + value + 
								"       </td><!-- Column: " + i +
								"; row: " + rowCount +
								" -->" + lineSeparator);
						}
					}
					
					if (rowCount == 1) {
						headerText.append("        </tr>" + lineSeparator);
						htmlFileText.append(headerText.toString());
					}
					
					if (!rotate) {
						bodyText.append("        </tr>" + lineSeparator);
					}
					else {
						for (int j = 0; j < rotatedRowsArray.length; j++) {
							bodyText.append("        <tr>" + lineSeparator);
							bodyText.append(rotatedRowsArray[j]);
							bodyText.append("        </tr>" + lineSeparator);
						}
					}
					htmlFileText.append(bodyText.toString());
				} while (resultSet.next());
				
				htmlFileText.append("      </table>" + lineSeparator);
				
				htmlFileText.append("    </p>" + lineSeparator);
				htmlFileText.append("    <p>" + lineSeparator);
					htmlFileText.append("      <ul class=\"dictionary\">" + lineSeparator);
				for (int j = 0; j < commentArray.length; j++) {
					htmlFileText.append(commentArray[j]);
				}
				htmlFileText.append("      </ul>" + lineSeparator);
				htmlFileText.append("    </p>" + lineSeparator);
					
			}
			else {
				htmlFileText.append("    <p>No data found</p>" + lineSeparator);
			}	
			
			if (expectedRows.equals("1") && rowCount == 1) { // OK
			}		
			else if (expectedRows.equals("1+") && rowCount > 0) { // OK
			}			
			else if (expectedRows.equals("0+")) { // No need to check
			}
			else {
				throw new Exception("Expecting: " + expectedRows + "; got: " + rowCount);
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + htmlReportQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}					
	}
	
	private void addJsonFile(
			final File temporaryDirectory,
			final ZipOutputStream submissionZipOutputStream,
			final Connection connection,
			final User user,
			final String studyID,
			final Locale locale,
			final String url)
			throws Exception {
				
		JSONObject json=new JSONObject();
		GetStudyJSON getStudyJSON = new GetStudyJSON(manager);
		JSONObject rif_job_submission=getStudyJSON.addRifStudiesJson(connection, 
			studyID, locale, url, null);
		rif_job_submission.put("created_by", user.getUserID());
		json.put("rif_job_submission", rif_job_submission);
		
		String jsonFileText=readFile("RIFStudyHeader.json") + json.toString(2);
		String JSONFileName="RIFstudy_" + studyID + ".json";
		
		rifLogger.info(this.getClass(), "Adding JSONfile: " + temporaryDirectory.getAbsolutePath() + File.separator + 
			JSONFileName + " to ZIP file");
		
		File file=new File(temporaryDirectory.getAbsolutePath() + File.separator + JSONFileName);
		ZipEntry zipEntry = new ZipEntry(JSONFileName);

		submissionZipOutputStream.putNextEntry(zipEntry);
		byte[] b=jsonFileText.getBytes();
		submissionZipOutputStream.write(b, 0, b.length);

		submissionZipOutputStream.closeEntry();		
	}

	private String readFile(String file) throws IOException {

		AppFile tcFile = AppFile.getServicesInstance(file);
				
		BufferedReader reader = tcFile.reader();
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(lineSeparator);
		}

		reader.close();
		return stringBuilder.toString();
	}
	
	private String addDirToTemporaryDirectoryPath(
		final User user,
		final String studyID,
		final String dirName) throws Exception {
		
		Path temporaryDirectoryPath = createTemporaryDirectoryPath(studyID);
		File newDirectory;

		if (temporaryDirectoryPath.toFile().exists()) {
			newDirectory = temporaryDirectoryPath.resolve(dirName).toFile();
			if (newDirectory.exists()) {
				rifLogger.info(this.getClass(), 
					"Found directory: " + newDirectory.getAbsolutePath());
			}
			else {
				newDirectory.mkdirs();
				rifLogger.info(this.getClass(), 
					"Created directory: " + newDirectory.getAbsolutePath());
			}
		}
		else {
			throw new Exception("R temporary directory: " +
			                    temporaryDirectoryPath.toFile().getAbsolutePath() + " was not created by Statistics_JRI.R");
		}
		
		return newDirectory.getAbsolutePath();
	}
			
	private File createSubmissionZipFile(
		final User user,
		final String baseStudyName) {

		StringBuilder fileName = new StringBuilder();
		fileName.append(EXTRACT_DIRECTORY);
		fileName.append(File.separator);
		fileName.append(user.getUserID());		
		fileName.append("_");
		fileName.append(baseStudyName);
		fileName.append(".zip");
		
		return new File(fileName.toString());		
	}
	/*
	 * Produces the base name for result files.
	 */
	private String createBaseStudyFileName(
		final RIFStudySubmission rifStudySubmission,
		final String studyID) {
		
		AbstractStudy study = rifStudySubmission.getStudy();
//		String name = study.getName().toLowerCase();
		String name = "s" + studyID + "_" + study.getName().toLowerCase();
		//concatenate study name length.  We need to be mindful about
		//the length of file names we produce so that they are not too
		//long for some operating systems to handle.
		
		if (name.length() > BASE_FILE_STUDY_NAME_LENGTH) {
			name = name.substring(0, BASE_FILE_STUDY_NAME_LENGTH);
		}
		
		
		//replace any spaces with underscores
		name = name.replaceAll(" ", "_");
		
		return name;
	}
	
	private Path createTemporaryDirectoryPath(final String studyID) {

		Path path = FileSystems.getDefault().getPath(EXTRACT_DIRECTORY, "scratchspace");

		// Numbered directory support (1-100 etc) to reduce the number of files/directories per directory to 100. This is to improve filesystem 
		// performance on Windows Tomcat servers 	
		Integer centile=Integer.parseInt(studyID) / 100; // 1273 = 12

		// Number directory: d1201-1300
		String numberDir = "d" + ((centile*100)+1) + "-" + (centile+1)*100;

		return path.resolve(numberDir).resolve("s" + studyID);
	}
	
	private void writeQueryFile(
		final ZipOutputStream submissionZipOutputStream,
		final User user,
		final String baseStudyName,
		final RIFStudySubmission rifStudySubmission)
		throws Exception {
		
		XMLCommentInjector commentInjector = new XMLCommentInjector();
		RIFStudySubmissionContentHandler rifStudySubmissionContentHandler
			= new RIFStudySubmissionContentHandler();
		rifStudySubmissionContentHandler.initialise(
			submissionZipOutputStream, 
			commentInjector);
	
		//KLG @TODO.  Right now we have only 
		
		//write the query file to a special directory.
		//this folder should only contain one file
		StringBuilder queryFileName = new StringBuilder();
		queryFileName.append(STUDY_QUERY_SUBDIRECTORY);
		queryFileName.append(File.separator);
		queryFileName.append(baseStudyName);
		queryFileName.append("_query.xml");
		
		ZipEntry rifQueryFileNameZipEntry = new ZipEntry(queryFileName.toString());
		submissionZipOutputStream.putNextEntry(rifQueryFileNameZipEntry);
		rifStudySubmissionContentHandler.writeXML(
			user, 
			rifStudySubmission);
		submissionZipOutputStream.closeEntry();

		rifLogger.info(this.getClass(), "Add to ZIP file: " + queryFileName);		
	}
	
	private void addAllFilesToZip(
			final File startDirectory,
			final ZipOutputStream submissionZipOutputStream,
			final String relativePath)
					throws Exception {
						
		rifLogger.debug(this.getClass(), "Adding R files start directory: " + startDirectory.getAbsolutePath() + lineSeparator + 
			"; relativePath: " + relativePath);
		File[] listOfFiles = startDirectory.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {	
		
			if (listOfFiles[i].isFile()) {
				rifLogger.debug(this.getClass(), "Adding R file: " + startDirectory.getAbsolutePath() + File.separator + 
					listOfFiles[i].getName() + " to ZIP file" + lineSeparator + "; relativePath: " + relativePath);
				
				File file=new File(startDirectory.getAbsolutePath() + File.separator + listOfFiles[i].getName());
				ZipEntry zipEntry = null;
				if (relativePath != null) {
					zipEntry = new ZipEntry(relativePath + File.separator + listOfFiles[i].getName());
				}
				else {
					zipEntry = new ZipEntry(listOfFiles[i].getName());
				}
				submissionZipOutputStream.putNextEntry(zipEntry);

				FileInputStream fileInputStream  = new FileInputStream(file);
				byte[] buffer = new byte[4092];
				int byteCount = 0;
				while ((byteCount = fileInputStream.read(buffer)) != -1) {
					submissionZipOutputStream.write(buffer, 0, byteCount);
				}

				fileInputStream.close();
				submissionZipOutputStream.closeEntry();
			}
			else if (listOfFiles[i].isDirectory()) {
				rifLogger.debug(this.getClass(), "Adding R directory: " + startDirectory.getAbsolutePath() + File.separator + 
					listOfFiles[i].getName() + File.separator + " to ZIP file" + 
					lineSeparator + "; relativePath: " + relativePath);
					/*
				if (relativePath != null) {
					submissionZipOutputStream.putNextEntry(
						new ZipEntry(relativePath + File.separator + listOfFiles[i].getName() + File.separator));
				}
				else {
					submissionZipOutputStream.putNextEntry(
						new ZipEntry(listOfFiles[i].getName() + File.separator));
				} */
				
				if (relativePath == null) {
					addAllFilesToZip(listOfFiles[i], submissionZipOutputStream, 
						listOfFiles[i].getName()); // Recurse!!
				}
				else {
					addAllFilesToZip(listOfFiles[i], submissionZipOutputStream, 
						relativePath + File.separator + listOfFiles[i].getName()); // Recurse!!
				}
			}
			else {
				rifLogger.debug(this.getClass(), "Ignoring R file: " + startDirectory.getAbsolutePath() + File.separator + 
					listOfFiles[i].getName());
			}
    	}
	}
	
	public String getRif40StudyState(
			final Connection connection,
			final String studyID)
					throws Exception {
						
		//get study_state
		SQLGeneralQueryFormatter studyStatusQueryFormatter = new SQLGeneralQueryFormatter();	
		studyStatusQueryFormatter.addQueryLine(0, "SELECT a.study_state");
		studyStatusQueryFormatter.addQueryLine(0, "FROM rif40.rif40_studies a");
		studyStatusQueryFormatter.addQueryLine(0, "WHERE a.study_id = ?");
					
		ResultSet studyStatusResultSet = null;
		String studyStatus = null;
		
		try {
			manager.logSQLQuery("getRif40StudyState", studyStatusQueryFormatter, studyID);
			PreparedStatement studyStatusStatement = manager.createPreparedStatement(connection,
					studyStatusQueryFormatter);
			studyStatusStatement.setInt(1, Integer.parseInt(studyID));	
			studyStatusResultSet = studyStatusStatement.executeQuery();
			studyStatusResultSet.next();
			studyStatus = studyStatusResultSet.getString(1);
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + studyStatusQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		return studyStatus;

	}
	private void closeStatement(PreparedStatement statement) {

		if (statement == null) {
			return;
		}

		try {
			statement.close();
		}
		catch(SQLException ignore) {}
	}
}
