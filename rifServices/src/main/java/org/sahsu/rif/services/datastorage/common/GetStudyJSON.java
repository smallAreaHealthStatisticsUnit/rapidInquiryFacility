package org.sahsu.rif.services.datastorage.common;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.util.RIFLogger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class GetStudyJSON {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final String HEALTH_CODE = "healthCode";
	private static final String lineSeparator = System.getProperty("line.separator");
	private static final String LABEL = "label";
	private static final String IS_TOP_LEVEL_TERM = "is_top_level_term";

	private final SQLManager manager;
	private Connection connection;
	private String studyID;
	private String url=null;
	private boolean taxonomyInitialiseError=false;
	private Exception otherTaxonomyError=null;
	
	/**
     * Constructor.
     * 
     * @param manager an SQLManager
     */
	public GetStudyJSON(final SQLManager manager) {
		
		this.manager = manager;
	}
	
	/** 
     * get JSON description for RIF study. In same format as front in save; but with more information
	 *
	 * View for data: RIF40_STUDIES
	 *
     * @param connection (required)
     * @param studyID (required)
     * @param locale (required)
     * @param url [deduced from calling URL] (required)
     * @param taxonomyServicesServer [from RIFServiceStartupProperties.java parameter] (required; may be NULL)
     * @return JSONObject [front end saves as JSON5 file]
     */
	JSONObject addRifStudiesJson(
			final Connection connection,
			final String studyID,
			final Locale locale,
			final String url,
			final String taxonomyServicesServer) 
					throws Exception {

		this.connection=connection;
		this.studyID=studyID;
		if (this.url == null) {
			if (taxonomyServicesServer != null && !taxonomyServicesServer.equals("")) {
				rifLogger.info(this.getClass(), "Using taxonomyServicesServer parameter for base URL: " + 
					taxonomyServicesServer);
				this.url =taxonomyServicesServer;
			}
			else if (url != null && !url.equals("")) {
				rifLogger.info(this.getClass(), "Using url parameter for base URL: " + 
					url);
				this.url =url;
			}
			else {
				throw new Exception("addRifStudiesJson(): cannot deduce tomcat server from RIF services request or RIFServiceStartup.properties");
			}
		}
		
		SQLGeneralQueryFormatter rifStudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		ResultSetMetaData rsmd;
		int columnCount;
		JSONObject rif_job_submission = new JSONObject();
		JSONObject additionalData = new JSONObject();
		boolean isDiseaseMappingStudy = false;
		
		rifStudiesQueryFormatter.addQueryLine(0, "SELECT username,study_id,extract_table,study_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       summary,description,other_notes,study_date,");
		rifStudiesQueryFormatter.addQueryLine(0, "       geography,study_type,study_state,comparison_geolevel_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       denom_tab,direct_stand_tab,year_start,year_stop,");
		rifStudiesQueryFormatter.addQueryLine(0, "       max_age_group,min_age_group,study_geolevel_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       map_table,suppression_value,extract_permitted,");
		rifStudiesQueryFormatter.addQueryLine(0, "       transfer_permitted,authorised_by,authorised_on,authorised_notes,");
		rifStudiesQueryFormatter.addQueryLine(0, "       covariate_table,project,project_description,stats_method,print_state,select_state");
		rifStudiesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_studies");	
		rifStudiesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");	
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifStudiesQueryFormatter);
		
		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				rsmd = resultSet.getMetaData();
				columnCount = rsmd.getColumnCount();
			}
			else {
				throw new Exception("addRifStudiesJson(): expected 1 row, got none");
			}
			JSONObject rif_project = new JSONObject();
			JSONObject study_type = new JSONObject();
			JSONObject rif_output_options = new JSONObject();
			JSONObject investigations = new JSONObject();
			JSONArray investigation = new JSONArray();
			JSONObject study_area = new JSONObject();
			JSONObject comparison_area = new JSONObject();
			JSONObject selectState = null;
			
			String geographyName=null;
			String comparisonGeolevelName = null;
			String studyGeolevelName = null;
			Calendar calendar;
			DateFormat df;
			if (locale != null) {
				df=DateFormat.getDateTimeInstance(
					DateFormat.DEFAULT /* Date style */, 
					DateFormat.DEFAULT /* Time style */, 
					locale);
				calendar = df.getCalendar();
				rif_job_submission.put("locale", locale);
			}
			else { // assume US
				df=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); // MM/DD/YY HH24:MI:SS
				rif_job_submission.put("date_format", "MM/dd/yyyy HH:mm:ss");
				calendar = Calendar.getInstance();
			}
			
			additionalData.put("calendar", calendar.toString());
			rif_output_options.put("rif_output_option", new String[] { "Data", "Maps", "Ratios and Rates" });

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				String value = resultSet.getString(i);
				Timestamp dateTimeValue;
				if (value == null) {
					value = "";
				}

				/* 
				"project": {
				  "name": "",
				  "description": ""
				}, */

				switch (name) {
					case "project":
						rif_project.put("name", value);
						break;
					case "comparison_geolevel_name":
						additionalData.put(name, value);
						comparisonGeolevelName = value;
						break;
					case "study_geolevel_name":
						additionalData.put(name, value);
						studyGeolevelName = value;
						break;
					case "project_description":
						rif_project.put("description", value);
						break;
					case "username":
						additionalData.put("extracted_by", value);
						rif_job_submission.put("submitted_by", value);
						break;
					case "study_date":
						if (value != null && value.length() > 0) {
							dateTimeValue = resultSet.getTimestamp(i, calendar);
							rif_job_submission.put("job_submission_date", df.format
									                                                 (dateTimeValue));
						} else {
							rif_job_submission.put("job_submission_date", "ONKNOWN");
						}
						break;
					case "authorised_on":
						dateTimeValue = resultSet.getTimestamp(i, calendar);
						additionalData
								.put(name, df.format(dateTimeValue));    // DD/MM/YY HH24:MI:SS

						break;
					case "study_name":
						study_type.put("name", value);
						break;
					case "description":
						study_type.put(name, value);
						break;
					case "geography":
						JSONObject geography = new JSONObject();
						geographyName = value;
						geography.put("name", geographyName);
						geography.put("description", getGeographyDescription(
								geographyName));    // Need to get from rif40_geographies

						study_type.put(name, geography);
						break;
					case "viewer_mapping":
					case "diseasemap1_mapping":
					case "diseasemap2_mapping":
						rif_output_options.put(name, new JSONObject(value)); // Parse value

						break;
					case "study_type":
						switch (Integer.parseInt(value)) {
							case 1: // disease mapping
								study_type.put("study_type", "Disease Mapping");
								isDiseaseMappingStudy = true;
								break;
							case 11:
								study_type
										.put("study_type", "Risk Analysis (many areas, one band)");
								break;
							case 12:
								study_type.put("study_type", "Risk Analysis (point sources)");
								break;
							case 13:
								study_type.put("study_type", "Risk Analysis (exposure "
								                             + "covariates)");
								break;
							case 14:
								study_type.put("study_type", "Risk Analysis (coverage shapefile)");
								break;
							case 15:
								study_type.put("study_type", "Risk Analysis (exposure shapefile)");
								break;
						}
						break;
				/* NONE/HET/BYM/CAR
				"calculation_methods": {
				  "calculation_method": {
					"name": "bym_r_procedure",
					"code_routine_name": "bym_r_procedure",
					"description": "Besag, York and Mollie (BYM) model type",
					"parameters": {
					  "parameter": []
					}
				  }
				}, */
					case "stats_method":
						if (value == null) {
							value = "NONE";
						}
						JSONObject calculation_method = new JSONObject();
						JSONObject calculation_methods = new JSONObject();
						JSONObject parameters = new JSONObject();
						JSONArray parameter = new JSONArray();
						if (resultSet.getString(i).equals("NONE")) {
							calculation_method.put("name", value);
							calculation_method.put("code_routine_name", value);
							calculation_method.put("description", value);
						} else {
							calculation_method.put("name", value.toLowerCase());
							calculation_method
									.put("code_routine_name", value.toLowerCase() +
									                          "_r_procedure");

							switch (value) {
								case "BYM":
									calculation_method.put("description",
									                       "Besag, York and Mollie (BYM) model "
									                       + "type");
									break;
								case "HET":
									calculation_method
											.put("description", "Heterogenous (HET) model type");
									break;
								case "CAR":
									calculation_method.put("description",
									                       "Conditional Auto Regression (CAR) "
									                       + "model type");
									break;
							}
						}
						parameters.put("parameter", parameter);
						calculation_method.put("parameters", parameters);
						calculation_methods.put("calculation_method", calculation_method);
						rif_job_submission.put("calculation_methods", calculation_methods);
						break;
					case "print_state":
						if (value != null && value.length() > 0) {
							try {
								JSONObject printState = new JSONObject(value);
								rif_job_submission.put("print_selection", printState);
							} catch (JSONException jsonException) {
								throw new JSONException(
										jsonException.getMessage() + "; in: " + name + "=" +
										value);
							}
						}
						break;
					case "select_state":
						if (value != null && value.length() > 0) {
							try {
								selectState = new JSONObject(value);
								rif_job_submission.put("study_selection", selectState);
							} catch (JSONException jsonException) {
								throw new JSONException(
										jsonException.getMessage() + "; in: " + name + "=" + value);
							}
						}
						break;
					default:
						additionalData.put(name, value);
						break;
				}
			}
			rif_job_submission.put("project", rif_project);	
			addStudyAreas(study_area, studyGeolevelName, comparisonGeolevelName, geographyName, selectState);
			if (isDiseaseMappingStudy) {
				study_type.put("disease_mapping_study_area", study_area);
			}
			else {
				study_type.put("risk_analysis_study_area", study_area);
			}
			addComparisonAreas(comparison_area, studyGeolevelName, comparisonGeolevelName, geographyName, selectState);
			study_type.put("comparison_area", comparison_area);
			addInvestigations(investigation, geographyName);
			investigations.put("investigation", investigation);
			study_type.put("investigations", investigations);

			if (isDiseaseMappingStudy) {
				rif_job_submission.put("disease_mapping_study", study_type);
			}
			else {
				rif_job_submission.put("risk_analysis_study", study_type);
			}

			rif_job_submission.put("rif_output_options", rif_output_options);
			addAdditionalTables(additionalData, "rif40_study_status");
			addSqlLog(additionalData);
			rif_job_submission.put("additional_data", additionalData);

			if (resultSet.next()) {
				throw new Exception("addRifStudiesJson(): expected 1 row, got >1");
			}
			if (taxonomyInitialiseError) { // Add flag for Taxonomy initialise error for front end if set
				rif_job_submission.put("taxonomy_initialise_error", true);
			}
			if (otherTaxonomyError != null) {
				JSONObject otherTaxonomyErrorObject = new JSONObject();
				StackTraceElement[] stackTrace = otherTaxonomyError.getStackTrace();
				int index = 0;
				JSONArray stackTraceArray = new JSONArray();
				for (StackTraceElement element : stackTrace) {
					index++;
					JSONObject stackTraceItem = new JSONObject();
					stackTraceItem.put("index", index);
					stackTraceItem.put("method_name", element.getMethodName());
					stackTraceItem.put("class", element.getClassName());
					stackTraceItem.put("line_number", element.getLineNumber());
					stackTraceItem.put("file_name", element.getFileName());
					stackTraceArray.put(stackTraceItem);
				}
				otherTaxonomyErrorObject.put("stack_trace", stackTraceArray);
				otherTaxonomyErrorObject.put("message", otherTaxonomyError.getMessage());
				otherTaxonomyErrorObject.put("stack_trace_text", otherTaxonomyError.getStackTrace());
				rif_job_submission.put("other_taxonomy_error", otherTaxonomyErrorObject);
			}
		}
		catch (SQLException sqlException) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifStudiesQueryFormatter.generateQuery(),
				sqlException);
			throw sqlException;
		}
		catch (JSONException jsonException) {
			rifLogger.error(this.getClass(), "Error in JSON parse: ",
				jsonException);
			throw jsonException;
		}
		finally {
			closeStatement(statement);
		}

		return rif_job_submission;
	}

	/**
	 * Get geography description
	 *
     * @param geographyName (required)
	 * @return geography description string
     */	
	private String getGeographyDescription(String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifGeographyQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifGeographyQueryFormatter.addQueryLine(0, "SELECT description FROM rif40.rif40_geographies WHERE geography = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifGeographyQueryFormatter);
		String geographyDescription;
		try {			
			statement.setString(1, geographyName);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				geographyDescription=resultSet.getString(1);
				if (resultSet.next()) {
					throw new Exception("getGeographyDescription(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("getGeographyDescription(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifGeographyQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}

		return geographyDescription;
	}

	/**
	 * Add SQL statement log
	 *
     * @param additionalData (required)
     */	
	private void addSqlLog(JSONObject additionalData) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addSqlLogQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		addSqlLogQueryFormatter.addQueryLine(0, "SELECT statement_number, COUNT(line_number) AS lines"); 
		addSqlLogQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_study_sql"); 
		addSqlLogQueryFormatter.addQueryLine(0, " WHERE study_id = ?"); 
		addSqlLogQueryFormatter.addQueryLine(0, " GROUP BY statement_number"); 
		addSqlLogQueryFormatter.addQueryLine(0, " ORDER BY statement_number");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				addSqlLogQueryFormatter);
		try {
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {	
					JSONObject tableRow = new JSONObject();
					String statementNumber=null;
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}		
						
						if (name.equals("statement_number")) {
							statementNumber=value;
							tableRow.put(name, value);
						}
						else {
							tableRow.put(name, value);
						}
					}
					addSQLLogLines(tableRow, statementNumber);
					addSQLLogSql(tableRow, statementNumber);
					tableData.put(tableRow);
					
				} while (resultSet.next());
			}	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + addSqlLogQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		additionalData.put("rif40_study_sql", tableData);	
	}

	/**
	 * Add SQL Log lines
	 *
     * @param additionalData (required)
     * @param statementNumber (required)
     */	
	private void addSQLLogLines(JSONObject additionalData, String statementNumber) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40.rif40_study_sql_log"); 
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, "   AND statement_number = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				addAdditionalTablesQueryFormatter);
		try {
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(statementNumber));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {	
					JSONObject tableRow = new JSONObject();
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						if (!name.equals("study_id") && !name.equals("username") &&
						    !name.equals("statement_number") && !name.equals("audsid") &&
						    !name.equals("statement_type")) {

							    tableRow.put(name, value);
						    }
					}
					tableData.put(tableRow);
					
				} while (resultSet.next());
			}	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + addAdditionalTablesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		additionalData.put("rif40_study_sql_log", tableData);	
	}
	
	/**
	 * Add SQL Log SQL
	 *
     * @param additionalData (required)
     * @param statementNumber (required)
     */	
	private void addSQLLogSql(JSONObject additionalData, String statementNumber) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40.rif40_study_sql"); 
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, "   AND statement_number = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, " ORDER BY line_number");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				addAdditionalTablesQueryFormatter);
		try {
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(statementNumber));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {	
					JSONObject tableRow = new JSONObject();
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						if (!name.equals("study_id") && !name.equals("username") &&
						    !name.equals("status") && !name.equals("statement_number") &&
						    !name.equals("statement_type")) {

							tableRow.put(name, value);
						}
					}
					tableData.put(tableRow);
					
				} while (resultSet.next());
			}	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + addAdditionalTablesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		additionalData.put("sql", tableData);	
	}
		
	/**
	 * Add additional tables
	 *
     * @param additionalData (required)
     * @param tableName (required)
     */
	private void addAdditionalTables(JSONObject additionalData, String tableName)
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40." + tableName.toLowerCase());
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				addAdditionalTablesQueryFormatter);
		try {
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {	
					JSONObject tableRow = new JSONObject();
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						if (!name.equals("study_id") && !name.equals("username")) {

							tableRow.put(name, value);
						}
					}
					tableData.put(tableRow);
					
				} while (resultSet.next());
			}	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + addAdditionalTablesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		additionalData.put(tableName.toLowerCase(), tableData);
	}

	/**
	 * TLS client for localhost ONLY. https://dzone.com/articles/jersey-ignoring-ssl
	 *
	 * DO NOT USE IT FOR ANYTHING ELSE!!!!!!
	 *
	 * To cope with:
	 * 
	 * Error in rest get https://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=C33&is_case_sensitive=false; for code: C33
	 * getMessage:          ClientHandlerException: javax.net.ssl.SSLHandshakeException: java.security.cert.CertificateException: No name matching localhost found
	 * getRootCauseMessage: CertificateException: No name matching localhost found
	 * getThrowableCount:   3
	 * getRootCauseStackTrace >>>
	 * java.security.cert.CertificateException: No name matching localhost found
	 * 	at sun.security.util.HostnameChecker.matchDNS(HostnameChecker.java:221)
	 * 	at sun.security.util.HostnameChecker.match(HostnameChecker.java:95)
	 * 	at sun.security.ssl.X509TrustManagerImpl.checkIdentity(X509TrustManagerImpl.java:455)
	 * 	at sun.security.ssl.X509TrustManagerImpl.checkIdentity(X509TrustManagerImpl.java:436)
	 * 	at sun.security.ssl.X509TrustManagerImpl.checkTrusted(X509TrustManagerImpl.java:200)
	 * 	at sun.security.ssl.X509TrustManagerImpl.checkServerTrusted(X509TrustManagerImpl.java:124)
	 * 	at sun.security.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:1496)
	 *
	 * If you are running networked you will have to set up Java correctly
	 *
	 * @return Jersey Client
     */
	private Client hostIgnoringClient() {
			
		Client client = null;
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
				public X509Certificate[] getAcceptedIssuers(){return null;}
				public void checkClientTrusted(X509Certificate[] certs, String authType){}
				public void checkServerTrusted(X509Certificate[] certs, String authType){}
			}};

			rifLogger.info(this.getClass(), "Using TLS localhost ignoring client");	
			SSLContext sslcontext = SSLContext.getInstance( "TLS" );
			sslcontext.init( null, trustAllCerts, new SecureRandom() );
			DefaultClientConfig config = new DefaultClientConfig();
			Map<String, Object> properties = config.getProperties();
			HTTPSProperties httpsProperties = new HTTPSProperties(
					(s, sslSession) -> true, sslcontext
			);
			properties.put( HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties );
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
			client = Client.create( config );
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error https://localhost workaround",
				exception);
		}
		
		return client;
	}

	/**
	 * Get health code description from taxonomy service [public version]
	 *
     * @param url (required)
     * @param taxonomyServicesServer (required)
     * @param code (required)
	 * @return health code description string
     */
	JSONObject getHealthCodeDescription(
			final String url,
			final String taxonomyServicesServer,
			final String code) 
					throws Exception { // Will get from taxonomy service
		if (taxonomyServicesServer != null && !taxonomyServicesServer.equals("")) {
			this.url =taxonomyServicesServer;
		}
		else if (url != null && !url.equals("")) {
			this.url =url;
		}
		else {
			throw new Exception("getHealthCodeDescription(): cannot deduce tomcat server from RIF"
			                    + " services request or RIFServiceStartup.properties");
		}
		return getHealthCodeDescription(code);
	}
	
	/**
	 * Get health code description from taxonomy service
	 *
     * @param code (required)
	 * @return health code description string
     */	
	private JSONObject getHealthCodeDescription(String code) {
		
		// Set up return value;
		JSONObject rval = new JSONObject();
		rval.put("description", "Not available");
		rval.put("identifier", "Unknown");
		rval.put(LABEL, code);
		rval.put("isTopLevelTerm", "no");

		if (otherTaxonomyError != null) { // These is an error in the taxonomyservices link
										  // This will require a tomcat restart to fix
			return rval;
		}

		ClientResponse response = null;
		try {

			response = getClientResponse(code);
			String output = response.getEntity(String.class);
			JSONObject taxonomyTerms = new JSONObject(output);
			rifLogger.debug(getClass(), "JSNObject taxonomyTerms: " + taxonomyTerms.toString());
			JSONObject terms = taxonomyTerms.getJSONObject("terms");

			// With different taxonomies, the healthCode element can sometimes be an array,
			// sometimes an object. This is all a bit hacky for my taste, but I'm not sure there's
			// another way.
			JSONObject healthCode = extractHealthCodeObject(code, terms);

			if (healthCode.has(LABEL) &&
			    healthCode.getString(LABEL).toUpperCase().equals(code.toUpperCase())) {
				rval = healthCode;

				if (rval.isNull(IS_TOP_LEVEL_TERM)) {

					rval.put(IS_TOP_LEVEL_TERM, "no");
				}
			} else if (healthCode.has("errorMessages")) {

				JSONArray errorArray = healthCode.getJSONArray("errorMessages");
				int errorArrayLen = errorArray.length();
				StringBuilder sb = new StringBuilder();
				if (errorArray.getString(0).equals(
						"The system for supporting taxonomy services has not yet been initialised.")) {
					taxonomyInitialiseError = true;
					rval.put("description", "Not yet available; please run again in 5 minutes");
				}

				for (int k = 0; k < errorArrayLen; k++) {
					sb.append(k).append(": ").append(errorArray.getString(k))
							.append(lineSeparator);
				}

				throw new Exception("taxonomyServices error: " + sb.toString() +
				                    "; for code: " + code);
			}

			rifLogger.info(this.getClass(), code + ": " + output + "; rval: " + rval.toString());
		} catch (Exception exception) {

			if (response == null) {
				rifLogger.error(this.getClass(), "Error in rest get for code: " + code,
					exception);
			} else if (taxonomyInitialiseError) {
				rifLogger.warning(getClass(), "taxonomyInitialiseError in rest get: "
				                              + response.getLocation() + "; for code: " + code
				                              + "; please run again in 5 minutes");
			} else {
				rifLogger.error(this.getClass(), "Error in rest get: "
				                                 + response.getLocation() + "; for code: "
				                                 + code, exception);
				otherTaxonomyError = exception;
			}
		}	
			
		return rval;
	}

	private JSONObject extractHealthCodeObject(final String code, final JSONObject terms) throws Exception {

		JSONObject codeObject = new JSONObject();
		if (terms.has(HEALTH_CODE) && terms.get(HEALTH_CODE) instanceof JSONArray) {

			JSONArray healthCode = terms.getJSONArray(HEALTH_CODE);
			int arrayLen = healthCode.length();
			for (int i = 0; i < arrayLen; i++) {

				codeObject = healthCode.getJSONObject(i);

				if (codeObject == null) {
					throw new Exception("Expected JSONObject, got null for code: " + code);
				}
			}
		} else {

			codeObject = terms.getJSONObject("healthCode");
		}
		return codeObject;
	}

	private ClientResponse getClientResponse(final String code) throws Exception {

		WebResource webResource;
		String uri = url + "/taxonomies/service/findTermInAnyTaxonomy";
		webResource = getClient().resource(uri);
		if (webResource == null) {
			throw new Exception("Null WebResource returned by rest client, URI: " + uri);
		}

		webResource = webResource.queryParam("search_text", code);
		webResource = webResource.queryParam("is_case_sensitive", "false");
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
				                          .get(ClientResponse.class);

		if (response.getStatus() != 200) {
		   throw new Exception(uri + " failed: HTTP error code : "
				+ response.getStatus());
		}
		return response;
	}

	private Client getClient() {
		final Client client;
		if (url.equals("https://localhost:8080")) {
			client=hostIgnoringClient();
		}
		else {
			client=Client.create();
		}
		return client;
	}

	/**
	 * Return taxonomyInitialiseError
	 *
	 * @return Boolean
     */
	boolean getTaxonomyInitialiseError() {
		return taxonomyInitialiseError;
	}
	
	/**
	 * Get outcome type. Will return the current ontology version e.g. icd10 even if icd9 codes 
	 * are actually being used
	 *
     * @param outcome_group_name (required)
	 * @return outcome type string
     */	
	private String getOutcomeType(String outcome_group_name) 
					throws Exception {
		SQLGeneralQueryFormatter rifOutcomeGroupsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifOutcomeGroupsQueryFormatter.addQueryLine(0, 
			"SELECT a.outcome_type, b.current_version FROM rif40.rif40_outcome_groups a, rif40.rif40_outcomes b WHERE a.outcome_group_name = ? AND a.outcome_type = b.outcome_type");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifOutcomeGroupsQueryFormatter);
		String outcomeGroup;
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

		return outcomeGroup.toLowerCase();
	}							

	/**
	 * Get rif40_studies data table name
     *
     * @param connection (required)
     * @param studyID (required)
	 * @return JSONObject
     */
	JSONObject getStudyData(Connection connection, String studyID)
					throws Exception {
		SQLGeneralQueryFormatter rifStudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifStudiesQueryFormatter.addQueryLine(0, "SELECT username,extract_table,study_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       summary,description,other_notes,");
		rifStudiesQueryFormatter.addQueryLine(0, "       study_date,geography,study_type,");
		rifStudiesQueryFormatter.addQueryLine(0, "       comparison_geolevel_name,denom_tab,");
		rifStudiesQueryFormatter.addQueryLine(0, "       direct_stand_tab,year_start,year_stop,");
		rifStudiesQueryFormatter.addQueryLine(0, "       max_age_group,min_age_group,study_geolevel_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       map_table,covariate_table,project,");
		rifStudiesQueryFormatter.addQueryLine(0, "       project_description,stats_method");
		rifStudiesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_studies");
		rifStudiesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifStudiesQueryFormatter);
		JSONObject studiesData = new JSONObject();
		try {			
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {	
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value="";
					}		
					
					studiesData.put(name, value);
				}
				if (resultSet.next()) {
					throw new Exception("getStudyData(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("getStudyData(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifStudiesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}

		return studiesData;
	}
	
	/**
	 * Get geolevelLookup table name
     *
     * @param connection (required)
     * @param geolevelName (required)
     * @param geographyName (required)
	 * @return JSONObject
     */
	JSONObject getLookupTableName(Connection connection, String geolevelName, String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifGeographyQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifGeographyQueryFormatter.addQueryLine(0, "SELECT description,lookup_table,lookup_desc_column");
		rifGeographyQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_geolevels");
		rifGeographyQueryFormatter.addQueryLine(0, " WHERE geography = ? AND geolevel_name = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifGeographyQueryFormatter);
		JSONObject geolevelData = new JSONObject();
		try {			
			statement.setString(1, geographyName);	
			statement.setString(2, geolevelName);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {	
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value="";
					}		
					
					geolevelData.put(name, value);
				}
				if (resultSet.next()) {
					throw new Exception("getLookupTableName(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("getLookupTableName(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifGeographyQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}

		return geolevelData;
	}

	/**
	 * Add study areas to a study
	 *	
	 * View for data: RIF40_STUDY_AREAS
	 *
     * @param study_areas (required)
     * @param studyGeolevelName (required)
     * @param comparisonGeolevelName (required)
     * @param geographyName (required)
     * @param selectState (required)
     */		
	private void addStudyAreas(JSONObject study_areas, 
		String studyGeolevelName, String comparisonGeolevelName, String geographyName, JSONObject selectState)
					throws Exception {
		JSONObject studyGeolevel = getLookupTableName(connection, studyGeolevelName, geographyName);
		
		SQLGeneralQueryFormatter rifStudyAreasQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		rifStudyAreasQueryFormatter.addQueryLine(0, "SELECT a.area_id, a.band_id, b." + 
			studyGeolevel.getString("lookup_desc_column").toLowerCase() + " AS label, b.gid");		
		rifStudyAreasQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_study_areas a ");
		rifStudyAreasQueryFormatter.addQueryLine(0, " 			LEFT OUTER JOIN rif_data." +			
			studyGeolevel.getString("lookup_table").toLowerCase() + 
			" b ON (a.area_id = b." + studyGeolevelName.toLowerCase() + ")");	
		rifStudyAreasQueryFormatter.addQueryLine(0, " WHERE a.study_id = ? ORDER BY band_id");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifStudyAreasQueryFormatter);
		
		JSONArray mapAreaArray=new JSONArray();
		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {			
					JSONObject mapArea=new JSONObject();
					
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}		
						
						if (name.equals("area_id") ) {
							mapArea.put("id", value);
						}
						else {
							mapArea.put(name, value);
						}
					}
					mapAreaArray.put(mapArea);
				} while (resultSet.next());
				
				JSONObject mapArea2=new JSONObject();
				JSONObject compName=new JSONObject();
				compName.put("name", comparisonGeolevelName);
				JSONObject studyName=new JSONObject();
				studyName.put("name", studyGeolevelName);
				JSONObject geo_levels=new JSONObject();
				geo_levels.put("geolevel_to_view", studyName);
				geo_levels.put("geolevel_to_map", studyName);
				geo_levels.put("geolevel_area", "");
				
				String studySelectAt=studyGeolevelName;
				if (selectState != null) {
					studySelectAt=selectState.optString("studySelectAt");
					if (studySelectAt == null) {
						studySelectAt=studyGeolevelName;
					}
				}		
				JSONObject geolevelSelect=new JSONObject();			
				geolevelSelect.put("name", studySelectAt);
				geo_levels.put("geolevel_select", geolevelSelect);
				
				mapArea2.put("map_area", mapAreaArray);
				study_areas.put("geo_levels", geo_levels);
				study_areas.put("study_geolevel", studyGeolevel);
				
				if (selectState != null) {
					JSONArray studySelectedAreas=selectState.optJSONArray("studySelectedAreas");
					if (studySelectedAreas == null) {
						mapArea2.put("map_area", mapAreaArray);
					}
					else {	
						mapArea2.put("map_area", studySelectedAreas);
					}
				}
				else {
					mapArea2.put("map_area", mapAreaArray);
				}
				study_areas.put("map_areas", mapArea2);
			}
			else {
				throw new Exception("addStudyAreas(): expected 1+ rows, got none");
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifStudyAreasQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}									
	}

	/**
	 * Add comparison areas to a study
	 *	
	 * View for data: RIF40_COMPARISON_AREAS
	 *
     * @param comparison_areas (required)
     * @param studyGeolevelName (required)
     * @param comparisonGeolevelName (required)
     * @param geographyName (required)
     * @param selectState (required)
     */		
	private void addComparisonAreas(JSONObject comparison_areas, 
		String studyGeolevelName, String comparisonGeolevelName, String geographyName, JSONObject selectState)
					throws Exception {
		JSONObject comparisonGeolevel = getLookupTableName(connection, comparisonGeolevelName, geographyName);
		
		SQLGeneralQueryFormatter rifComparisonAreasQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		rifComparisonAreasQueryFormatter.addQueryLine(0, "SELECT a.area_id, b." + 
			comparisonGeolevel.getString("lookup_desc_column").toLowerCase() + " AS label, b.gid");			
		rifComparisonAreasQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_comparison_areas a");
		rifComparisonAreasQueryFormatter.addQueryLine(0, " 			LEFT OUTER JOIN rif_data." +
			comparisonGeolevel.getString("lookup_table").toLowerCase() + " b ON (a.area_id = b." + 
			comparisonGeolevelName.toLowerCase() + ")");		
		rifComparisonAreasQueryFormatter.addQueryLine(0, " WHERE a.study_id = ? ORDER BY area_id");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifComparisonAreasQueryFormatter);
		
		JSONArray mapAreaArray=new JSONArray();
		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {			
					JSONObject mapArea=new JSONObject();
					
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}					
						
						if (name.equals("area_id") ) {
							mapArea.put("id", value);
						}
						else {
							mapArea.put(name, value);
						}
					}
					mapAreaArray.put(mapArea);
				} while (resultSet.next());
				
				JSONObject mapArea2=new JSONObject();
				JSONObject compName=new JSONObject();
				compName.put("name", comparisonGeolevelName);
				JSONObject studyName=new JSONObject();
				studyName.put("name", studyGeolevelName);
				JSONObject geo_levels=new JSONObject();
				geo_levels.put("geolevel_to_view", compName);
				geo_levels.put("geolevel_to_map", compName);
				geo_levels.put("geolevel_area", "");
				
				String comparisonSelectAt=comparisonGeolevelName;
				if (selectState != null) {
					comparisonSelectAt=selectState.optString("comparisonSelectAt");
					if (comparisonSelectAt == null) {
						comparisonSelectAt=comparisonGeolevelName;
					}
				}
				JSONObject geolevelSelect=new JSONObject();			
				geolevelSelect.put("name", comparisonSelectAt);
				geo_levels.put("geolevel_select", geolevelSelect);
					
				comparison_areas.put("geo_levels", geo_levels);
				comparison_areas.put("comparison_geolevel", comparisonGeolevel);
				
				if (selectState != null) {
					JSONArray comparisonSelectedAreas=selectState.optJSONArray("comparisonSelectedAreas");
					if (comparisonSelectedAreas == null) {
						mapArea2.put("map_area", mapAreaArray);
					}
					else {	
						mapArea2.put("map_area", comparisonSelectedAreas);
					}	
				}
				else {
					mapArea2.put("map_area", mapAreaArray);
				}				
				comparison_areas.put("map_areas", mapArea2);
			}
			else {
				throw new Exception("addComparisonAreas(): expected 1+ rows, got none");
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifComparisonAreasQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}								
	}
	
	/**
	 * Add health codes to an investigation
	 *	
	 * View for data: RIF40_INV_CONDITIONS
	 *
     * @param healthCodes (required)
     * @param studyID (required)
     * @param invID (required)
     */						
	private void addHealthCodes(JSONObject healthCodes, String studyID, int invID)
					throws Exception {
		SQLGeneralQueryFormatter rifInvConditionsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		rifInvConditionsQueryFormatter.addQueryLine(0, "SELECT min_condition,max_condition,predefined_group_name,");
		rifInvConditionsQueryFormatter.addQueryLine(0, "       outcome_group_name,numer_tab,");
		rifInvConditionsQueryFormatter.addQueryLine(0, "       field_name,condition,CAST(column_comment AS VARCHAR(2000)) AS column_comment");		
		rifInvConditionsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_inv_conditions");	
		rifInvConditionsQueryFormatter.addQueryLine(0, " WHERE study_id = ? AND inv_id = ? ORDER BY line_number");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifInvConditionsQueryFormatter);
		
		JSONArray healthCodeArray=new JSONArray();
		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, invID);	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {			
					JSONObject healthCode=new JSONObject();
					String minCondition = null;
					String maxCondition = null;
					
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						switch (name) {
							case "min_condition":
								minCondition = value;
								break;
							case "max_condition":
								maxCondition = value;
								break;
							case "outcome_group_name":
								healthCode.put("name_space", getOutcomeType(value));
								break;
							default:
								healthCode.put(name, value);
								break;
						}
					}
					
					if (!StringUtils.isEmpty(minCondition) && !StringUtils.isEmpty(maxCondition)) {
						// BETWEEN
						JSONObject code = new JSONObject();
						JSONObject taxonomyObject = getHealthCodeDescription(minCondition);
						code.put("min_condition", minCondition);
						code.put("min_description", taxonomyObject.getString("description"));
						code.put("max_condition", maxCondition);
						code.put("max_description", getHealthCodeDescription(maxCondition).getString("description"));
						String is_top_level_term = null;
						if (!taxonomyObject.isNull(IS_TOP_LEVEL_TERM)) {
							is_top_level_term=taxonomyObject.getString(IS_TOP_LEVEL_TERM);
						}
						healthCode.put(IS_TOP_LEVEL_TERM, is_top_level_term);
						healthCode.put("code", code);
					} else if (!StringUtils.isEmpty(minCondition)
					         && StringUtils.isEmpty(maxCondition)) {
						// LIKE
						healthCode.put("code", minCondition);
						JSONObject taxonomyObject = getHealthCodeDescription(minCondition);
						healthCode.put("description", taxonomyObject.getString("description"));
						String is_top_level_term = null;
						if (!taxonomyObject.isNull(IS_TOP_LEVEL_TERM)) {
							is_top_level_term=taxonomyObject.getString(IS_TOP_LEVEL_TERM);
						}
						healthCode.put(IS_TOP_LEVEL_TERM, is_top_level_term);
					} else {
						throw new Exception("addHealthCodes(): minCondition: " + minCondition +
							"; maxCondition: " + maxCondition);
					}
					
					healthCodeArray.put(healthCode);
				} while (resultSet.next());
				
				healthCodes.put("health_code", healthCodeArray);
			}
			else {
				throw new Exception("addHealthCodes(): expected 1+ rows, got none");
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifInvConditionsQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}					
	}

	/**
	 * Add covariates to an investigation
	 *	
	 * View for data: RIF40_INV_COVARIATES
	 *
     * @param covariateArray (required)
     * @param studyID (required)
     * @param invID (required)
     */			
	private void addCovariates(JSONArray covariateArray, String studyID, int invID)
					throws Exception {
		SQLGeneralQueryFormatter rifInvCovariatesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		rifInvCovariatesQueryFormatter.addQueryLine(0, "SELECT covariate_name,min,max,geography,study_geolevel_name");
		rifInvCovariatesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_inv_covariates");
		rifInvCovariatesQueryFormatter.addQueryLine(0, " WHERE study_id = ? AND inv_id = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifInvCovariatesQueryFormatter);
		
		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, invID);	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {			
					JSONObject covariate=new JSONObject();
					JSONObject adjustableCovariate=new JSONObject();
					
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						switch (name) {
							case "covariate_name":
								covariate.put("name", value);
								break;
							case "min":
								covariate.put("minimum_value", value);
								break;
							case "max":
								covariate.put("maximum_value", value);
								break;
							default:
								covariate.put(name, value);
								break;
						}
					}		
					covariate.put("covariate_type", "adjustable");
					adjustableCovariate.put("adjustable_covariate", covariate);					
					covariateArray.put(adjustableCovariate);
				} while (resultSet.next());
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifInvCovariatesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}				
	}
	
	/**
	 * Add investigation to a study
	 *	
	 * View for data: RIF40_INVESTIGATIONS
	 *
     * @param investigation (required)
     * @param geographyName (required)
     */		
	private void addInvestigations(JSONArray investigation, String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifInvestigationsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifInvestigationsQueryFormatter.addQueryLine(0, "SELECT inv_id,inv_name,year_start,year_stop,");
		rifInvestigationsQueryFormatter.addQueryLine(0, "       max_age_group,min_age_group,genders,numer_tab,");
		rifInvestigationsQueryFormatter.addQueryLine(0, "       mh_test_type,inv_description,classifier,classifier_bands,investigation_state");
		rifInvestigationsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_investigations");
		rifInvestigationsQueryFormatter.addQueryLine(0, " WHERE study_id = ? ORDER BY inv_id");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifInvestigationsQueryFormatter);

		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {
					JSONObject investigationObject = new JSONObject();
					JSONObject additionalData = new JSONObject();
					JSONObject age_band = new JSONObject();
					JSONObject health_codes = new JSONObject();
					JSONObject year_range = new JSONObject();
					JSONObject year_intervals = new JSONObject();
					JSONArray year_interval = new JSONArray();
					JSONArray covariateArray = new JSONArray();
					int yearStart=0;
					int yearStop=0;
					int minAgeGroup=0;
					int maxAgeGroup=0;
					int invId=0;
					String numeratorTable=null;

					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}

						switch (name) {
							case "inv_name":
								investigationObject.put("title", value);
								break;
							case "username":
								additionalData.put("extracted_by", value);
								break;
							case "numer_tab":
								numeratorTable = value;
								JSONObject numerator_denominator_pair = new JSONObject();
								JSONObject health_theme = new JSONObject();
								addNumeratorDenominatorPair(numeratorTable,
								                            numerator_denominator_pair,
								                            health_theme, geographyName);

								investigationObject.put("health_theme", health_theme);
								investigationObject.put("numerator_denominator_pair",
								                        numerator_denominator_pair);
								break;
							case "min_age_group":
								minAgeGroup = Integer.parseInt(value);
								break;
							case "max_age_group":
								maxAgeGroup = Integer.parseInt(value);
								break;
							case "year_start":
								yearStart = Integer.parseInt(value);
								year_range.put("lower_bound", yearStart);
								break;
							case "year_stop":
								yearStop = Integer.parseInt(value);
								year_range.put("upper_bound", yearStop);
								break;
							case "inv_id":
								invId = Integer.parseInt(value);
								additionalData.put(name, invId);
								break;
							case "genders":
								switch (Integer.parseInt(value)) {
									case 1:
										investigationObject.put("sex", "Males");
										break;
									case 2:
										investigationObject.put("sex", "Females");
										break;
									case 3:
										investigationObject.put("sex", "Both");
										break;
								}
								break;
							default:
								additionalData.put(name, value);
								break;
						}
					}
					JSONObject lower_age_group=addAgeSexGroup(minAgeGroup /* Offset */, numeratorTable);
					age_band.put("lower_age_group", lower_age_group);
					JSONObject upper_age_group=addAgeSexGroup(maxAgeGroup /* Offset */, numeratorTable);
					age_band.put("upper_age_group", upper_age_group);
					investigationObject.put("age_band", age_band);
					addHealthCodes(health_codes, studyID, invId);
					investigationObject.put("health_codes", health_codes);
					investigationObject.put("year_range", year_range);
					for (int j=yearStart;j<=yearStop;j++) {
						JSONObject yearInterval = new JSONObject();
						yearInterval.put("start_year", j);
						yearInterval.put("end_year", j);
						year_interval.put(yearInterval);
					}
					year_intervals.put("year_interval", year_interval);
					investigationObject.put("year_intervals", year_intervals);
					investigationObject.put("years_per_interval", 1);
					
					addCovariates(covariateArray, studyID, invId);
					investigationObject.put("covariates", covariateArray); 
					investigationObject.put("additional_data", additionalData);
					investigation.put(investigationObject);	
				} while (resultSet.next());
			}
			else {
				throw new Exception("addInvestigations(): expected 1+ rows, got none");
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifInvestigationsQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
	}

	/** 
     * Add upper or lower age group to an investigation as part of an age band:
	 *	"age_band": {
     *         "upper_age_group": {
     *           "lower_limit": "85",
     *           "name": "85PLUS",
     *           "upper_limit": "255",
     *           "id": "21"
     *         },
     *         "lower_age_group": {
     *           "lower_limit": "0",
     *           "name": "0",
     *           "upper_limit": "0",
     *           "id": "0"
     *         }
     *       },
	 *
     * @param offset (required)
     * @param tableName (required)
	 * @return JSONObject
     */	
	private JSONObject addAgeSexGroup(int offset, String tableName) 
					throws Exception {
		SQLGeneralQueryFormatter ageSexGroupQueryFormatter = new SQLGeneralQueryFormatter();		
		ageSexGroupQueryFormatter.addQueryLine(0, "SELECT a.offset AS id, a.low_age AS lower_limit, a.high_age AS upper_limit, a.fieldname AS name");
		ageSexGroupQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_age_groups a, rif40.rif40_tables b");
		ageSexGroupQueryFormatter.addQueryLine(0, " WHERE a.offset       = ?");
		ageSexGroupQueryFormatter.addQueryLine(0, "   AND a.age_group_id = b.age_group_id");
		ageSexGroupQueryFormatter.addQueryLine(0, "   AND b.table_name   = ?");
		
		ResultSet resultSet;
		PreparedStatement statement = manager.createPreparedStatement(connection,
				ageSexGroupQueryFormatter);
		JSONObject age_group = new JSONObject();

		try {		
			statement.setInt(1, offset);
			statement.setString(2, tableName);	
			resultSet = statement.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if (resultSet.next()) {
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value="";
					}
					age_group.put(name, value);	
				}
				
				if (resultSet.next()) {
					throw new Exception("addAgeSexGroup(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("addAgeSexGroup(): expected 1 row, got none");
			}		
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + ageSexGroupQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		return age_group;
	}
	
	/** 
     * Add numerator and denominator pair and health theme to an investigation:
	 * 
	 *	"numerator_denominator_pair": {
     *         "denominator_table_name": "POP_SAHSULAND_POP",
     *         "numerator_table_description": "cancer numerator",
     *         "denominator_description": "population health file",
     *         "numerator_table_name": "NUM_SAHSULAND_CANCER"
     *       },
	 *	"health_theme": {
     *         "name": "cancers",
     *         "description": "covering various types of cancers"
     *       }
	 *	
	 * View for data: RIF40_NUM_DENOM
	 *
     * @param numeratorTable (required)
     * @param numerator_denominator_pair (required)
     * @param health_theme (required)
     * @param geographyName (required)
     */	
	private void addNumeratorDenominatorPair(String numeratorTable, 
						JSONObject numerator_denominator_pair, JSONObject health_theme, 
						String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifNumDenomQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet;
		
		rifNumDenomQueryFormatter.addQueryLine(0, "SELECT a.geography, a.numerator_table, a.numerator_description,");
		rifNumDenomQueryFormatter.addQueryLine(0, "       a.theme_description, a.denominator_table, a.denominator_description, b.theme");
		rifNumDenomQueryFormatter.addQueryLine(0, "  FROM rif40_num_denom a");
		rifNumDenomQueryFormatter.addQueryLine(0, "       LEFT OUTER JOIN rif40.rif40_health_study_themes b ON (a.theme_description = b.description)");
		rifNumDenomQueryFormatter.addQueryLine(0, " WHERE a.geography = ? AND a.numerator_table = ?");
		PreparedStatement statement = manager.createPreparedStatement(connection,
				rifNumDenomQueryFormatter);

		try {		
			statement.setString(1, geographyName);	
			statement.setString(2, numeratorTable);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value="";
					}

					switch (name) {
						case "numerator_table":
							numerator_denominator_pair.put("numerator_table_name", value);
							break;
						case "numerator_description":
							numerator_denominator_pair.put("numerator_table_description", value);
							break;
						case "denominator_table":
							numerator_denominator_pair.put("denominator_table_name", value);
							break;
						case "denominator_description":
							numerator_denominator_pair.put("denominator_description", value);
							break;
						case "theme_description":
							health_theme.put("description", value);
							break;
						case "theme":
							health_theme.put("name", value);
							break;
					}
				}
				
				if (resultSet.next()) {
					throw new Exception("addNumeratorDenominatorPair(): expected 1 row, got >1");
				}
			}
			else {
				throw new Exception("addNumeratorDenominatorPair(): expected 1 row, got none");
			}	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifNumDenomQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
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
