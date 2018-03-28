package rifServices.dataStorageLayer.pg;


import rifServices.dataStorageLayer.common.StudyExtractManager;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.fileFormats.RIFStudySubmissionContentHandler;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
//import rifGenericLibrary.dataStorageLayer.common.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.dataStorageLayer.common.GetStudyJSON;
import rifServices.dataStorageLayer.common.RifZipFile;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifGenericLibrary.util.RIFDateFormat;

import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Locale;

public class PGSQLStudyExtractManager extends PGSQLAbstractSQLManager implements StudyExtractManager {

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

	public PGSQLStudyExtractManager(
			final RIFServiceStartupOptions rifServiceStartupOptions) {


		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		EXTRACT_DIRECTORY = this.rifServiceStartupOptions.getExtractDirectory();
		TAXONOMY_SERVICES_SERVER = this.rifServiceStartupOptions.getTaxonomyServicesServer();
		databaseType=this.rifServiceStartupOptions.getRifDatabaseType();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================	
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
						
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions);
		FileInputStream zipStream=rifZipFile.getStudyExtract(
			connection,
			user,
			rifStudySubmission,
			zoomLevel,
			studyID);
		return zipStream;
	}
	
	@Override
	public String getExtractStatus(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String studyID
	                              )
					throws RIFServiceException {
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions);
		String extractStatus=rifZipFile.getExtractStatus(
			connection,
			user,
			rifStudySubmission,
			studyID);
		return extractStatus;
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
			GetStudyJSON getStudyJSON = new GetStudyJSON(rifServiceStartupOptions);
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
			RIFServiceException rifServiceExeption
				= new RIFServiceException(
					RIFServiceError.JSONFILE_CREATE_FAILED, 
					errorMessage);
			throw rifServiceExeption;
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
						
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions);
		rifZipFile.createStudyExtract(connection,
			user,
			rifStudySubmission,
			zoomLevel,
			studyID,
			locale,
			tomcatServer,
			TAXONOMY_SERVICES_SERVER);

	}

	/*
	private void writeStatisticalPostProcessingFiles(
		final Connection connection,
		final String temporaryDirectoryPath,		
		final ZipOutputStream submissionZipOutputStream,
		final String baseStudyName,
		final RIFStudySubmission rifStudySubmission)
		throws Exception {


		ArrayList<CalculationMethod> calculationMethods
			= rifStudySubmission.getCalculationMethods();
		for (CalculationMethod calculationMethod : calculationMethods) {

			StringBuilder postProcessedTableName = new StringBuilder();			
			postProcessedTableName.append("s");
			postProcessedTableName.append(rifStudySubmission.getStudyID());
			postProcessedTableName.append("_");
			postProcessedTableName.append(calculationMethod.getName());

			StringBuilder postProcessedFileName = new StringBuilder();
			postProcessedFileName.append(temporaryDirectoryPath);
			postProcessedFileName.append(File.separator);
			postProcessedFileName.append(baseStudyName);
			postProcessedFileName.append("_");
			postProcessedFileName.append(calculationMethod.getName());
			postProcessedFileName.append(".csv");

			File postProcessedFile = new File(postProcessedFileName.toString());
			addFileToZipFile(
				submissionZipOutputStream, 
				STATISTICAL_POSTPROCESSING_SUBDIRECTORY, 
				postProcessedFile);
		}
	}	
	 */

	/*
	private void writeTermsAndConditionsFiles(
		final ZipOutputStream submissionZipOutputStream) 
		throws Exception {

		File[] files = termsAndConditionsDirectory.listFiles();
		for (File file : files) {
			addFileToZipFile(
				submissionZipOutputStream, 
				TERMS_CONDITIONS_SUBDIRECTORY,
				file);			
		}		
	}
	 */


	/*
	 * General methods for writing to zip files
	 */
/*
	public void addFileToZipFile(
			final ZipOutputStream submissionZipOutputStream,
			final String zipEntryName,
			final File inputFile)
					throws Exception {

		ZipEntry rifQueryFileNameZipEntry = new ZipEntry(zipEntryName);
		submissionZipOutputStream.putNextEntry(rifQueryFileNameZipEntry);

		byte[] BUFFER = new byte[4096 * 1024];
		FileInputStream fileInputStream = new FileInputStream(inputFile);		
		int bytesRead = fileInputStream.read(BUFFER);		
		while (bytesRead != -1) {
			submissionZipOutputStream.write(BUFFER, 0, bytesRead);			
			bytesRead = fileInputStream.read(BUFFER);
		}
		submissionZipOutputStream.flush();
		fileInputStream.close();
		submissionZipOutputStream.closeEntry();
		
		rifLogger.info(this.getClass(), "Add to ZIP file: " + inputFile);
	} */
		
	@Override
	public String getRif40StudyState(
			final Connection connection,
			final String studyID)
					throws Exception {
		String studyState;
		RifZipFile rifZipFile = new RifZipFile(rifServiceStartupOptions);
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
