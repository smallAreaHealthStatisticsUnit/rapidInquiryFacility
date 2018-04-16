package rifServices.dataStorageLayer.pg;

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

//import rifGenericLibrary.dataStorageLayer.common.SQLFunctionCallerQueryFormatter;

public class PGSQLStudyExtractManager extends PGSQLAbstractSQLManager
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
	
	public PGSQLStudyExtractManager(
			final RIFServiceStartupOptions rifServiceStartupOptions) {


		super(rifServiceStartupOptions);
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		EXTRACT_DIRECTORY = this.rifServiceStartupOptions.getExtractDirectory();
		TAXONOMY_SERVICES_SERVER = this.rifServiceStartupOptions.getTaxonomyServicesServer();
		DatabaseType databaseType = this.rifServiceStartupOptions.getRifDatabaseType();
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
		
	@Override
	public FileInputStream getStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel,
			final String studyID)
					throws RIFServiceException {
						
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		return rifZipFile.getStudyExtract(
			connection,
			user,
			rifStudySubmission,
			zoomLevel,
			studyID);
	}
	
	@Override
	public String getExtractStatus(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID
	                              )
					throws RIFServiceException {
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions, this);
		return rifZipFile.getExtractStatus(
			connection,
			user,
			rifStudySubmission,
			studyID);
	}

	@Override
	public String getJsonFile(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID,
			final Locale locale,
			final String tomcatServer)
					throws RIFServiceException {
		String result="{}";

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
			rifLogger.error(this.getClass(), "PGSQLStudyExtractManager ERROR", exception);
				
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.getJsonFile",
					user.getUserID(),
					studyID);
			throw new RIFServiceException(
				RIFServiceError.JSONFILE_CREATE_FAILED,
				errorMessage);
		}
		return result;
	}
	
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

	@Override
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
}
