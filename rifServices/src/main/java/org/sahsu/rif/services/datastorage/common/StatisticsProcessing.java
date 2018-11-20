package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.LogManager;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.Parameters;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class StatisticsProcessing extends CommonRService {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	private RIFServiceStartupOptions rifStartupOptions;

	StatisticsProcessing() {

		String logManagerName=System.getProperty("java.util.logging.manager");
		if (logManagerName == null || !logManagerName.equals("java.util.logging.manager")) {
			System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			rifLogger.info(this.getClass(), "Set java.util.logging.manager=" +
				System.getProperty("java.util.logging.manager"));
		}

		final LogManager logManager = LogManager.getLogManager();
		Enumeration<String> loggerNames = logManager.getLoggerNames();

		if (!loggerNames.hasMoreElements()) {
			rifLogger.warning(this.getClass(), "java.util.logging.manager has no loggers");
		}

		while (loggerNames.hasMoreElements()) {

			String name = loggerNames.nextElement();
			if (name.equals("rifGenericLibrary.util.RIFLogger")) {
				rifLogger.info(this.getClass(), "Found java.util.logging.manager logger: " + name);
			} else {
				rifLogger.debug(this.getClass(), "Other java.util.logging.manager logger: " + name);
			}
		}
	}

	public void initialise(final String userID, final String password,
			final RIFServiceStartupOptions rifStartupOptions) throws RIFServiceException {

		this.rifStartupOptions = rifStartupOptions;

		setUser(userID, password);

		setODBCDataSourceName(rifStartupOptions.getODBCDataSourceName());

		List<Parameter> rifStartupOptionParameters = rifStartupOptions.getDbParametersForRScripts();
		addParameters(rifStartupOptionParameters);
	}
	
	void performStep(final Connection connection, final RIFStudySubmission studySubmission,
			final String studyID, final String url) throws RIFServiceException {

		//KLG: For now it only works with the first study.  For some reason, newer extract
		//tables cause the R program we use to generate an error.
		addParameter("studyID", studyID);

		//add a parameter for investigation name.  This will appear as a column in the extract
		//table that the R program will need to know about.  Note that specifying a single
		//investigation name assumes that eventually we will make a study have one investigation
		//rather than multiple investigations.
		Investigation firstInvestigation = studySubmission.getStudy().getInvestigations().get(0);

		addParameter("investigationName",
		             createDatabaseFriendlyInvestigationName(firstInvestigation.getTitle()));

		String covariateName = getCovariateName(studySubmission);
		addParameter("covariate_name", covariateName);

		Integer investigationID;
		try {

			investigationID = getInvestigationID(connection, studyID, firstInvestigation);
		} catch (SQLException e) {

			throw new RIFServiceException("", e);
		}

		String studyName=studySubmission.getStudy().getName();
		addParameter("studyName", studyName);

		String studyDescription=studySubmission.getStudy().getDescription();
		addParameter("studyDescription", studyDescription);

		rifLogger.info(this.getClass(), "Study id: " + studyID +
			"; Study name: " + studyName +
			"; Study description: " + studyDescription +
			"; Investigation name: " + firstInvestigation.getTitle() +
			"; ID: "+ investigationID);

		addParameter("investigationId", String.valueOf(investigationID));

		if (studySubmission.getStudy().isRiskAnalysis()) {
			addParameter("studyType", "riskAnalysis");
		} else {
			addParameter("studyType", "diseaseMapping");
		}

		setCalculationMethod(studySubmission.getCalculationMethods().get(0));
		addParameter("working_dir", rifStartupOptions.getExtractDirectory());
		addParameter("databaseType", rifStartupOptions.getRifDatabaseType().getShortName());

		Parameters parameters = new Parameters(getParameters());

		String statsServiceUrl = url + "/statistics/service/script";

		rifLogger.info(getClass(), "About to call statistics service on " + statsServiceUrl);

		// Make sure Jersey converts our native bean-style object to JSON.
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		Client client = Client.create(clientConfig);
		WebResource resource = client.resource(statsServiceUrl);
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
				                          .type(MediaType.APPLICATION_JSON)
				                          .post(ClientResponse.class, parameters);

		rifLogger.info(getClass(), "Statistics service called: " + response.toString());
		if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {

			String baseMsg = "Received a failure response from the Statistics Service. "
			                 + "Response as a string is \n\n\t%s\n\n"
			                 + "The Entity from the Response is \n\n\t%s\n\n\n"
			                 + "----------------- End of Statistics failure details "
			                 + "-------------------\n\n";
			String reason = String.format(baseMsg, response.toString(),
			                              response.getEntity(String.class));
			rifLogger.error(getClass(), reason);
			throw new RIFServiceException(reason);
		}
	}

	/*
	 * @TODO: KLG - Currently the study submission data model allows for it to have a study with multiple investigations,
	 * each of which can have multiple different covariates.  In practice, we are finding that we are using one covariate.
	 * This is a method that should be migrated into the RIFStudySubmission class.
	 */
	private String getCovariateName(final RIFStudySubmission studySubmission) {
		AbstractStudy study
		= studySubmission.getStudy();
		ArrayList<Investigation> investigations = study.getInvestigations();
		//Get the covariates from the first investigation
		ArrayList<AbstractCovariate> covariates = investigations.get(0).getCovariates();


		//This just takes the first covariate of the first investigation and returns its name.  That's the one we will
		//assume will appear in the extract table.  Note though that this needs to be changed in future because at the 
		//moment our model accommodates multiple covariates in multiple investigations.
		if (covariates.isEmpty()) {
			return "NONE";
		}

		return covariates.get(0).getName();
	}


	private String createDatabaseFriendlyInvestigationName(final String investigationName) {
		return investigationName.trim().toUpperCase().replaceAll(" ", "_");		
	}


	private Integer getInvestigationID(
			final Connection connection,
			final String studyID,
			final Investigation investigation) 
					throws SQLException,
					RIFServiceException {


		rifLogger.info(this.getClass(), "SQLSmoothedResultsSubmissionStep getInvestigationID studyID=="+studyID+"==investigation_name=="+investigation.getTitle()+"==inv_description=="+investigation.getDescription()+"==");

		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
     				rifStartupOptions.getRifDatabaseType());
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addFromTable("rif40_investigations");
		queryFormatter.addSelectField("inv_id");
		queryFormatter.addWhereParameter("study_id");
		queryFormatter.addWhereParameter("inv_name");

		rifLogger.info(this.getClass(), "=======getInvestigationID========1===" + lineSeparator +
			"StudyID=="+studyID+"=="  + lineSeparator +
			"Inv_name=="+investigation.getTitle().toUpperCase()+"==" + lineSeparator +
			queryFormatter.generateQuery()  + lineSeparator +
			"=======getInvestigationID========2===");

		String databaseFriendlyInvestigationName
		= createDatabaseFriendlyInvestigationName(investigation.getTitle());

		Integer investigationID;
		PreparedStatement statement = null;		
		ResultSet resultSet = null;
		try {
			statement 
			= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			statement.setString(2, databaseFriendlyInvestigationName);
			resultSet = statement.executeQuery();
			rifLogger.info(this.getClass(), "About to call next");
			resultSet.next();
			rifLogger.info(this.getClass(), "called next");
			investigationID = resultSet.getInt(1);
		}
		finally {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);
		}

		return investigationID;
	}
}
