package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudySubmission;

/**
 * Creates a study extract.
 *
 * <p>This code was extracted from
 * {@link StudySubmissionService}.
 * </p>
 */
public class StudyExtract {
	
	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();
	
	private User user;
	private String studyID;
	private String zoomLevel;
	private Locale locale;
	private String url;
	private ServiceResources rifServiceResources;
	private Connection connection;
	
	public StudyExtract(User user, String studyID, String zoomLevel, Locale locale,
			String url, ServiceResources rifServiceResources) {
		
		this.user = user;
		this.studyID = studyID;
		this.zoomLevel = zoomLevel;
		this.locale = locale;
		this.url = url;
		this.rifServiceResources = rifServiceResources;
	}
	
	public void create() throws RIFServiceException {
		
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return;
		}
		
		RIFLogger rifLogger = RIFLogger.getLogger();
		try {

			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter("createStudyExtract",
					"user", user);
			fieldValidationUtility.checkNullMethodParameter("createStudyExtract",
					"studyID", studyID);
			fieldValidationUtility.checkNullMethodParameter("createStudyExtract",
					"zoomLevel", zoomLevel);

			//Check for security violations
			new ValidateUser(user, sqlConnectionManager).validate();
			fieldValidationUtility.checkMaliciousMethodParameter("createStudyExtract",
					"studyID", studyID);

			//Audit attempt to do operation
			String auditTrailMessage =
					SERVICE_MESSAGES.getMessage("logging.createStudyExtract",
							user.getUserID(), user.getIPAddress(), studyID, zoomLevel);
			rifLogger.info(getClass(), auditTrailMessage);

			connection = sqlConnectionManager.assignPooledWriteConnection(user);

			SubmissionManager sqlRIFSubmissionManager =
					rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission =
					sqlRIFSubmissionManager.getRIFStudySubmission(connection, user, studyID);

			StudyExtractManager studyExtractManager =
					rifServiceResources.getSQLStudyExtractManager();
			studyExtractManager.createStudyExtract(connection, user, rifStudySubmission, zoomLevel,
			                                       studyID, locale, url);
			rifLogger.info(getClass(), "Create ZIP file completed OK");

		} catch(RIFServiceException rifServiceException) {

			new ExceptionLog(user, "createStudyExtract", rifServiceException,
					rifServiceResources, rifLogger).log();
		} finally {
			
			sqlConnectionManager.reclaimPooledWriteConnection(user, connection);
		}
	}
}
