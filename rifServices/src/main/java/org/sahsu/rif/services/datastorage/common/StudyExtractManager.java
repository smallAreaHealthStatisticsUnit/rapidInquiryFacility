package org.sahsu.rif.services.datastorage.common;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Locale;

import org.json.JSONObject;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class StudyExtractManager extends BaseSQLManager {

	private static String TAXONOMY_SERVICES_SERVER;

	private RIFServiceStartupOptions rifServiceStartupOptions;

	public StudyExtractManager(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		TAXONOMY_SERVICES_SERVER = this.rifServiceStartupOptions.getTaxonomyServicesServer();
	}

	String getStudyExtractFIleName(final User user, final String studyID) {

		return user.getUserID()
		       + "_"
		       + "s" + studyID
		       + ".zip";
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
	FileInputStream getStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID) throws RIFServiceException {

		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		return rifZipFile.getStudyExtract(
				connection,
				user,
				rifStudySubmission,
				zoomLevel,
				studyID);
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
	String getExtractStatus(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID) throws RIFServiceException {

		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		return rifZipFile.getExtractStatus(connection, user, rifStudySubmission, studyID);
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
	 * @param  locale 		locale
	 * @param  url e.g. http://localhost:8080.
	 * @return 				Textual extract status as exscaped JSON, e.g. {status: STUDY_NOT_FOUND}
	 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
	 */
	String getJsonFile(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID,
			final Locale locale,
			final String url) throws RIFServiceException {

		String result;

		try {
			JSONObject json = new JSONObject();
			GetStudyJSON getStudyJSON = new GetStudyJSON(this);
			JSONObject rifJobSubmission = getStudyJSON.addRifStudiesJson(connection,
			                                                             studyID, locale, url,
			                                                             TAXONOMY_SERVICES_SERVER);
			rifJobSubmission.put("created_by", user.getUserID());
			json.put("rif_job_submission", rifJobSubmission);
			result=json.toString();
		} catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName()
			                                 + "ERROR", exception);

			String errorMessage = RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.getJsonFile",
					user.getUserID(),
					studyID);
			throw new RIFServiceException(RIFServiceError.JSONFILE_CREATE_FAILED, errorMessage);
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
	 * @param url [deduced from calling URL] (required)
	 */
	void createStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID,
			final Locale locale,
			final String url)
			throws RIFServiceException {

		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		rifZipFile.createStudyExtract(connection,
		                              user,
		                              rifStudySubmission,
		                              zoomLevel,
		                              studyID,
		                              locale,
		                              url,
		                              TAXONOMY_SERVICES_SERVER);
	}
}
