package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;

import java.net.URI;
import javax.net.ssl.*;

// Requires v2 Jersey/Javax-ws
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.UriBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties; 
import java.security.cert.X509Certificate; 
import java.security.SecureRandom; 

import java.sql.*;
import org.json.*;
import java.lang.*;

import java.util.Date;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Peter Hambly
 * @author phambly
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public class GetStudyJSON extends SQLAbstractSQLManager {
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private Connection connection;
	private String studyID;
	private String tomcatServer;
	private boolean taxonomyInitialiseError=false;
	private Exception otherTaxonomyError=null;
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================
	/**
     * Constructor.
     * 
     * @param RIFServiceStartupOptions rifServiceStartupOptions (required)
     */
	public GetStudyJSON(
			final RIFServiceStartupOptions rifServiceStartupOptions) {
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
	}
	
	/** 
     * get JSON description for RIF study. In same format as front in save; but with more information
	 *
	 * View for data: RIF40_STUDIES
	 *
     * @param Connection connection (required)
     * @param String studyID (required)
     * @param Locale locale (required)
     * @param String tomcatServer [deduced from calling URL] (required)
     * @param String taxonomyServicesServer [from RIFServiceStartupProperties.java parameter] (required; may be NULL)
     * @return JSONObject [front end saves as JSON5 file]
     */
	public JSONObject addRifStudiesJson(
			final Connection connection, 
			final String studyID, 
			final Locale locale,
			final String tomcatServer,
			final String taxonomyServicesServer) 
					throws Exception {
		this.connection=connection;
		this.studyID=studyID;
		if (taxonomyServicesServer != null && !taxonomyServicesServer.equals("")) {
			this.tomcatServer=taxonomyServicesServer;
		}
		else if (tomcatServer != null && !tomcatServer.equals("")) {
			this.tomcatServer=tomcatServer;
		}
		else {
			throw new Exception("addRifStudiesJson(): cannot deduce tomcat server from RIF services request or RIFServiceStartup.properties");
		}
		
		SQLGeneralQueryFormatter rifStudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		ResultSetMetaData rsmd = null;
		int columnCount = 0;
		JSONObject rif_job_submission = new JSONObject();
		JSONObject additionalData = new JSONObject();
		
		rifStudiesQueryFormatter.addQueryLine(0, "SELECT username,study_id,extract_table,study_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       summary,description,other_notes,study_date,");
		rifStudiesQueryFormatter.addQueryLine(0, "       geography,study_type,study_state,comparison_geolevel_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       denom_tab,direct_stand_tab,year_start,year_stop,");
		rifStudiesQueryFormatter.addQueryLine(0, "       max_age_group,min_age_group,study_geolevel_name,");
		rifStudiesQueryFormatter.addQueryLine(0, "       map_table,suppression_value,extract_permitted,");
		rifStudiesQueryFormatter.addQueryLine(0, "       transfer_permitted,authorised_by,authorised_on,authorised_notes,");
		rifStudiesQueryFormatter.addQueryLine(0, "       covariate_table,project,project_description,stats_method");
		rifStudiesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_studies");	
		rifStudiesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");	
		PreparedStatement statement = createPreparedStatement(connection, rifStudiesQueryFormatter);
		
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
			JSONObject disease_mapping_study_area = new JSONObject();
			JSONObject comparison_area = new JSONObject();
			
			String geographyName=null;
			String comparisonGeolevelName = null;
			String studyGeolevelName = null;
			Calendar calendar = null;
			DateFormat df = null;
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
				Timestamp dateTimeValue=null;
				if (value == null) {
					value="";
				}

				/* 
				"project": {
				  "name": "",
				  "description": ""
				}, */
				
				if (name.equals("project") ) {
					rif_project.put("name", value);	
				}
				else if (name.equals("comparison_geolevel_name") ) {
					additionalData.put(name, value);	
					comparisonGeolevelName=value;		
				}
				else if (name.equals("study_geolevel_name") ) {
					additionalData.put(name, value);
					studyGeolevelName=value;					
				}
				else if (name.equals("project_description") ) {
					rif_project.put("description", value);	
				}
				else if (name.equals("username") ) {
					additionalData.put("extracted_by", value);	
				}
				else if (name.equals("study_date") ) {
					dateTimeValue=resultSet.getTimestamp(i, calendar);
					additionalData.put("job_submission_date", df.format(dateTimeValue));	
				}
				else if (name.equals("authorised_on") ) {
					dateTimeValue=resultSet.getTimestamp(i, calendar);
					additionalData.put(name, df.format(dateTimeValue));	// DD/MM/YY HH24:MI:SS
				}

				else if (name.equals("study_name") ) {
					study_type.put("name", value);	
				}
				else if (name.equals("description") ) {
					study_type.put(name, value);	
				}
				else if (name.equals("geography") ) {
					JSONObject geography = new JSONObject();
					geographyName = value;
					geography.put("name", geographyName);	
					geography.put("description", getGeographyDescription(geographyName));	// Need to get from rif40_geographies
					study_type.put(name, geography);	
				}
				else if (name.equals("viewer_mapping") || name.equals("diseasemap1_mapping") || name.equals("diseasemap2_mapping") ) {
					rif_output_options.put(name, new JSONObject(value)); // Parse value
				}
				else if (name.equals("study_type") ) {
					switch(Integer.parseInt(value)) {
						case 1: // disease mapping
							study_type.put("study_type", "Disease Mapping");	
							break;
						case 11: 
							study_type.put("study_type", "Risk Analysis (many areas, one band)");
							break;
						case 12: 
							study_type.put("study_type", "Risk Analysis (point sources)");
							break;
						case 13: 
							study_type.put("study_type", "Risk Analysis (exposure covariates)");
							break;
						case 14: 
							study_type.put("study_type", "Risk Analysis (coverage shapefile)");
							break;
						case 15: 
							study_type.put("study_type", "Risk Analysis (exposure shapefile)");
							break;
					}
				}
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
				else if (name.equals("stats_method") ) {
					if (value == null) {
						value="NONE";
					}
					JSONObject calculation_method = new JSONObject();
					JSONObject calculation_methods = new JSONObject();
					JSONObject parameters = new JSONObject();
					JSONArray parameter = new JSONArray();
					if (resultSet.getString(i).equals("NONE")) {
						calculation_method.put("name", value);
						calculation_method.put("code_routine_name", value);
						calculation_method.put("description", value);
					}
					else {
						calculation_method.put("name", value.toLowerCase());
						calculation_method.put("code_routine_name", value.toLowerCase() + "_r_procedure");
						if (value.equals("BYM")) {
							calculation_method.put("description", "Besag, York and Mollie (BYM) model type");
						}
						else if (value.equals("HET")) {
							calculation_method.put("description", "Heterogenous (HET) model type");
						}
						else if (value.equals("CAR")) {
							calculation_method.put("description", "Conditional Auto Regression (CAR) model type");
						}
					}
					parameters.put("parameter", parameter);
					calculation_method.put("parameters", parameters);
					calculation_methods.put("calculation_method", calculation_method);	
					rif_job_submission.put("calculation_methods", calculation_methods);
				}
				else { 
					additionalData.put(name, value);	
				}
			}
			rif_job_submission.put("project", rif_project);	
			addStudyAreas(disease_mapping_study_area, studyGeolevelName, comparisonGeolevelName, geographyName);
			study_type.put("disease_mapping_study_area", disease_mapping_study_area);
			addComparisonAreas(comparison_area, studyGeolevelName, comparisonGeolevelName, geographyName);
			study_type.put("comparison_area", comparison_area);
			addInvestigations(investigation, geographyName);
			investigations.put("investigation", investigation);
			study_type.put("investigations", investigations);
			rif_job_submission.put("disease_mapping_study", study_type);
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
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + rifStudiesQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}

		return rif_job_submission;
	}

	/**
	 * Get geography description
	 *
     * @param String geographyName (required)
	 * @return geography description string
     */	
	private String getGeographyDescription(String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifGeographyQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifGeographyQueryFormatter.addQueryLine(0, "SELECT description FROM rif40.rif40_geographies WHERE geography = ?");
		PreparedStatement statement = createPreparedStatement(connection, rifGeographyQueryFormatter);
		String geographyDescription=null;
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
			PGSQLQueryUtility.close(statement);
		}

		return geographyDescription;
	}

	/**
	 * Add SQL statement log
	 *
     * @param JSONObject additionalData (required)
     */	
	private void addSqlLog(JSONObject additionalData) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addSqlLogQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		addSqlLogQueryFormatter.addQueryLine(0, "SELECT statement_number, COUNT(line_number) AS lines"); 
		addSqlLogQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_study_sql"); 
		addSqlLogQueryFormatter.addQueryLine(0, " WHERE study_id = ?"); 
		addSqlLogQueryFormatter.addQueryLine(0, " GROUP BY statement_number"); 
		addSqlLogQueryFormatter.addQueryLine(0, " ORDER BY statement_number");
		PreparedStatement statement = createPreparedStatement(connection, addSqlLogQueryFormatter);
		String geographyDescription=null;
		try {			
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				int rowCount = 0;

				do {	
					JSONObject tableRow = new JSONObject();
					String statementNumber=null;
					rowCount++;
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
			PGSQLQueryUtility.close(statement);
		}
		
		additionalData.put("rif40_study_sql", tableData);	
	}

	/**
	 * Add SQL Log lines
	 *
     * @param JSONObject additionalData (required)
     * @param String statementNumber (required)
     */	
	private void addSQLLogLines(JSONObject additionalData, String statementNumber) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40.rif40_study_sql_log"); 
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, "   AND statement_number = ?");
		PreparedStatement statement = createPreparedStatement(connection, addAdditionalTablesQueryFormatter);
		String geographyDescription=null;
		try {			
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(statementNumber));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				int rowCount = 0;

				do {	
					JSONObject tableRow = new JSONObject();
					rowCount++;
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}		
						
						if (name.equals("study_id") || name.equals("username") || 
						    name.equals("statement_number") || name.equals("audsid") || 
							name.equals("statement_type")) {
						}
						else {
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
			PGSQLQueryUtility.close(statement);
		}
		
		additionalData.put("rif40_study_sql_log", tableData);	
	}
	
	/**
	 * Add SQL Log SQL
	 *
     * @param JSONObject additionalData (required)
     * @param String statementNumber (required)
     */	
	private void addSQLLogSql(JSONObject additionalData, String statementNumber) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40.rif40_study_sql"); 
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, "   AND statement_number = ?");
		addAdditionalTablesQueryFormatter.addQueryLine(0, " ORDER BY line_number");
		PreparedStatement statement = createPreparedStatement(connection, addAdditionalTablesQueryFormatter);
		String geographyDescription=null;
		try {			
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(statementNumber));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				int rowCount = 0;

				do {	
					JSONObject tableRow = new JSONObject();
					rowCount++;
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}		
						
						if (name.equals("study_id") || name.equals("username") || name.equals("status") || 
						    name.equals("statement_number") || name.equals("statement_type")) {
						}
						else {
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
			PGSQLQueryUtility.close(statement);
		}
		
		additionalData.put("sql", tableData);	
	}
		
	/**
	 * Add additional tables
	 *
     * @param JSONObject additionalData (required)
     * @param String tableName (required)
     */	
	private void addAdditionalTables(JSONObject additionalData, String tableName) 
					throws Exception {
		JSONArray tableData = new JSONArray();
		SQLGeneralQueryFormatter addAdditionalTablesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		addAdditionalTablesQueryFormatter.addQueryLine(0, "SELECT * FROM rif40." + tableName.toLowerCase()); 
		addAdditionalTablesQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		PreparedStatement statement = createPreparedStatement(connection, addAdditionalTablesQueryFormatter);
		String geographyDescription=null;
		try {			
			statement.setInt(1, Integer.parseInt(studyID));		
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				int rowCount = 0;

				do {	
					JSONObject tableRow = new JSONObject();
					rowCount++;
					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
						String name = rsmd.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
							value="";
						}		
						
						if (name.equals("study_id") || name.equals("username")) {
						}
						else {
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
			PGSQLQueryUtility.close(statement);
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
	private Client hostIgnoringClient() 
			throws Exception { 
			
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
					new HostnameVerifier()
					{
						@Override
						public boolean verify( String s, SSLSession sslSession )
						{
							return true;
						}
					}, sslcontext
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
	 * Get health code description from taxonomy service
	 *
     * @param String code (required)
	 * @return health code description string
     */	
	private JSONObject getHealthCodeDesription(String code) 
					throws Exception { // Will get from taxonomy service
		
		// Set up return value;
		JSONObject rval=new JSONObject();
		rval.put("description", "Not available");
		rval.put("identifier", "Unknown");
		rval.put("label", code);
		rval.put("isTopLevelTerm", "no");
		boolean rvalFound=false;
		
		if (otherTaxonomyError != null) { // These is an error in the taxonomyServices link
										  // This will require a tomcat restart to fix
			return rval;
		}
	
/* Call to taxonomy service: 

http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=c33&is_case_sensitive=false

[{
		"identifier": "C33-icd10",
		"label": "C33",
		"description": "Malignant neoplasm of trachea",
		"isTopLevelTerm": null
	}
]
 */
 
/* This code requires javax.ws.rs 2.1 which in turn forces a Jersey 2.1/2.2 upgrade

A mismatch causes a tomcat failure

10:24:53.622 [http-nio-8080-exec-2] ERROR org.apache.juli.logging.DirectJDKLog org.apache.catalina.core.ContainerBase.[Catalina].[lo
calhost].[/rifServices].[rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceApplication]: Servlet.service() for serv
let [rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceApplication] in context with path [/rifServices] threw excep
tion [Servlet execution threw an exception] with root cause
java.lang.AbstractMethodError: javax.ws.rs.core.UriBuilder.uri(Ljava/lang/String;)Ljavax/ws/rs/core/UriBuilder;
        at javax.ws.rs.core.UriBuilder.fromUri(UriBuilder.java:120) ~[javax.ws.rs-api-2.1.jar:2.1]
        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:669) ~[jersey-servlet-1.19.jar:1.19]
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:742) ~[servlet-api.jar:?]
		
		Client client=null;
		WebTarget target=null;
		try {			

			client = ClientBuilder.newClient();

			String URI="http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms";
			target = client.target(URI).
				queryParam("taxonomy_id", "icd10").
				queryParam("search_text", code).
				queryParam("is_case_sensitive", "false");
			
			String response = target.path("rest").
	 //                           path("hello").
								request().
								accept(MediaType.TEXT_PLAIN).
								get(Response.class)
								.toString();

			String plainAnswer =
					target.path("rest").
	//				path("hello").
					request().accept(MediaType.TEXT_PLAIN).get(String.class);
			rifLogger.info(this.getClass(), code + ": " + plainAnswer);	
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in rest get " + target.toString(),
				exception);
			throw exception;
		}	
*/		

		Client client = null;
		
		WebResource webResource=null;
		try {		
			if (tomcatServer.equals("https://localhost:8080")) {
				client=hostIgnoringClient();
			}
			else {	
				client=Client.create();
			}
			String URI=tomcatServer + "/taxonomyServices/taxonomyServices/getMatchingTerms";
			webResource = client.resource(URI);
			if (webResource == null) {
				throw new Exception("Null WebResource returned by rest client, URI: " + URI);
			}
			webResource = webResource.queryParam("taxonomy_id", "icd10");
			webResource = webResource.queryParam("search_text", code);
			webResource = webResource.queryParam("is_case_sensitive", "false");
			ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

			if (response.getStatus() != 200) {
			   throw new Exception("Failed : HTTP error code : "
					+ response.getStatus());
			}
/*
  Usual response - an array of ICD10 identifiers:

  [{
		"identifier": "C33-icd10",
		"label": "C33",
		"description": "Malignant neoplasm of trachea",
		"isTopLevelTerm": null
   }
  ]
  
  Cope with:

  [{
		"errorMessages": ["The system for supporting taxonomy services has not yet been initialised."]
   }
  ]
 */
			String output = response.getEntity(String.class);
			JSONTokener tokener = new JSONTokener(output);
			JSONArray array = new JSONArray(tokener);
			int arrayLen=array.length();
			for (int i = 0; i < arrayLen; i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				if (jsonObject == null) {
					throw new Exception("Expected JSONObject, got null for code: " + code);
				}
				else {	
					if (jsonObject.has("errorMessages")) {	
						JSONArray errorArray = jsonObject.getJSONArray("errorMessages");
						int errorArrayLen=errorArray.length();
						StringBuilder sb = new StringBuilder();
						if (errorArray.getString(0). // Handle init (i.e. suppress error)
							equals("The system for supporting taxonomy services has not yet been initialised.")) {
							taxonomyInitialiseError=true;
							rval.put("description", "Not yet available; please run again in 5 minutes");
						}
						for (int k = 0; k < errorArrayLen; k++) {
							sb.append(k + ": " + errorArray.getString(k) + lineSeparator);
						}
						throw new Exception("taxonomyServices error: " + sb.toString() + 
							"; for code: " + code);						
					}
					else {
						if (jsonObject.has("label") &&
							jsonObject.getString("label").toUpperCase().equals(code.toUpperCase())) {
							if (rvalFound) { // >1 match
								throw new Exception(">1 match for code: " + code);
							}
							else {
								rvalFound=true;
								rval=jsonObject;
								if (rval.isNull("is_top_level_term")) {
									rval.put("is_top_level_term", "no");
								}
							}
						}
						else { // No match
						}						
					}
				}
			}
			rifLogger.info(this.getClass(), code + ": " + output + "; rval: " + rval.toString());	

		}
		catch (Exception exception) {
			if (webResource == null) {
				rifLogger.error(this.getClass(), "Error in rest get for code: " + code,
					exception);
			}
			else if (taxonomyInitialiseError) {
				rifLogger.warning(this.getClass(), "taxonomyInitialiseError in rest get: " + webResource.toString() 
					+ "; for code: " + code + "; please run again in 5 minutes");
			}
			else {
				rifLogger.error(this.getClass(), "Error in rest get: " + webResource.toString() 
					+ "; for code: " + code, exception);
				otherTaxonomyError=exception;
			}
		}	
			
		return rval;
	}
	
	/**
	 * Get outcome type. Will return the current ontology version e.g. icd10 even if icd9 codes 
	 * are actually being used
	 *
     * @param String outcome_group_name (required)
	 * @return outcome type string
     */	
	private String getOutcomeType(String outcome_group_name) 
					throws Exception {
		SQLGeneralQueryFormatter rifOutcomeGroupsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifOutcomeGroupsQueryFormatter.addQueryLine(0, 
			"SELECT a.outcome_type, b.current_version FROM rif40.rif40_outcome_groups a, rif40.rif40_outcomes b WHERE a.outcome_group_name = ? AND a.outcome_type = b.outcome_type");
		PreparedStatement statement = createPreparedStatement(connection, rifOutcomeGroupsQueryFormatter);
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
			PGSQLQueryUtility.close(statement);
		}

		return outcomeGroup.toLowerCase();
	}							

	/**
	 * Get geolevelLookup table name
     *
     * @param Connection connection (required)
     * @param String geolevelName (required)
     * @param String geographyName (required)
	 * @return JSONObject
     */	
	public JSONObject getLookupTableName(Connection connection, String geolevelName, String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifGeographyQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifGeographyQueryFormatter.addQueryLine(0, "SELECT description,lookup_table,lookup_desc_column");
		rifGeographyQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_geolevels");
		rifGeographyQueryFormatter.addQueryLine(0, " WHERE geography = ? AND geolevel_name = ?");
		PreparedStatement statement = createPreparedStatement(connection, rifGeographyQueryFormatter);
		String lookupTableName=null;
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
			PGSQLQueryUtility.close(statement);
		}

		return geolevelData;
	}

	/**
	 * Add study areas to a study
	 *	
	 * View for data: RIF40_STUDY_AREAS
	 *
     * @param JSONObject disease_mapping_study_areas (required)
     * @param JSONObject String studyGeolevelName (required)
     * @param JSONObject String comparisonGeolevelName (required)
     * @param JSONObject String geographyName (required)
     */		
	private void addStudyAreas(JSONObject disease_mapping_study_areas, 
		String studyGeolevelName, String comparisonGeolevelName, String geographyName)
					throws Exception {
		JSONObject studyGeolevel = getLookupTableName(connection, studyGeolevelName, geographyName);
		
		SQLGeneralQueryFormatter rifStudyAreasQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		rifStudyAreasQueryFormatter.addQueryLine(0, "SELECT a.area_id, a.band_id, b." + 
			studyGeolevel.getString("lookup_desc_column").toLowerCase() + " AS label, b.gid");		
		rifStudyAreasQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_study_areas a ");
		rifStudyAreasQueryFormatter.addQueryLine(0, " 			LEFT OUTER JOIN rif_data." +			
			studyGeolevel.getString("lookup_table").toLowerCase() + 
			" b ON (a.area_id = b." + studyGeolevelName.toLowerCase() + ")");	
		rifStudyAreasQueryFormatter.addQueryLine(0, " WHERE a.study_id = ? ORDER BY band_id");
		PreparedStatement statement = createPreparedStatement(connection, rifStudyAreasQueryFormatter);
		
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
				geo_levels.put("geolevel_select", studyName);
				mapArea2.put("map_area", mapAreaArray);
				disease_mapping_study_areas.put("geo_levels", geo_levels);
				disease_mapping_study_areas.put("study_geolevel", studyGeolevel);
				disease_mapping_study_areas.put("map_areas", mapArea2);
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
			PGSQLQueryUtility.close(statement);
		}									
	}

	/**
	 * Add comparison areas to a study
	 *	
	 * View for data: RIF40_COMPARISON_AREAS
	 *
     * @param JSONObject comparison_areas (required)
     * @param JSONObject String studyGeolevelName (required)
     * @param JSONObject String comparisonGeolevelName (required)
     * @param JSONObject String geographyName (required)
     */		
	private void addComparisonAreas(JSONObject comparison_areas, 
		String studyGeolevelName, String comparisonGeolevelName, String geographyName)
					throws Exception {
		JSONObject comparisonGeolevel = getLookupTableName(connection, comparisonGeolevelName, geographyName);
		
		SQLGeneralQueryFormatter rifComparisonAreasQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		rifComparisonAreasQueryFormatter.addQueryLine(0, "SELECT a.area_id, b." + 
			comparisonGeolevel.getString("lookup_desc_column").toLowerCase() + " AS label, b.gid");			
		rifComparisonAreasQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_comparison_areas a");
		rifComparisonAreasQueryFormatter.addQueryLine(0, " 			LEFT OUTER JOIN rif_data." +
			comparisonGeolevel.getString("lookup_table").toLowerCase() + " b ON (a.area_id = b." + 
			comparisonGeolevelName.toLowerCase() + ")");		
		rifComparisonAreasQueryFormatter.addQueryLine(0, " WHERE a.study_id = ? ORDER BY area_id");
		PreparedStatement statement = createPreparedStatement(connection, rifComparisonAreasQueryFormatter);
		
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
				geo_levels.put("geolevel_select", compName);
				mapArea2.put("map_area", mapAreaArray);
				comparison_areas.put("geo_levels", geo_levels);
				comparison_areas.put("comparison_geolevel", comparisonGeolevel);
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
			PGSQLQueryUtility.close(statement);
		}								
	}
	
	/**
	 * Add health codes to an investigation
	 *	
	 * View for data: RIF40_INV_CONDITIONS
	 *
     * @param JSONObject healthCodes (required)
     * @param String studyID (required)
     * @param int invID (required)
     */						
	private void addHealthCodes(JSONObject healthCodes, String studyID, int invID)
					throws Exception {
		SQLGeneralQueryFormatter rifInvConditionsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		rifInvConditionsQueryFormatter.addQueryLine(0, "SELECT min_condition,max_condition,predefined_group_name,");
		rifInvConditionsQueryFormatter.addQueryLine(0, "       outcome_group_name,numer_tab,");
		rifInvConditionsQueryFormatter.addQueryLine(0, "       field_name,condition,CAST(column_comment AS VARCHAR(2000)) AS column_comment");		
		rifInvConditionsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_inv_conditions");	
		rifInvConditionsQueryFormatter.addQueryLine(0, " WHERE study_id = ? AND inv_id = ? ORDER BY line_number");
		PreparedStatement statement = createPreparedStatement(connection, rifInvConditionsQueryFormatter);
		
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

						if (name.equals("min_condition") ) {
							minCondition = value;
						}
						else if (name.equals("max_condition") ) {
							maxCondition = value;
						}
						else if (name.equals("outcome_group_name") ) {
							healthCode.put("name_space", getOutcomeType(value));
						}
						else {
							healthCode.put(name, value);
						}
					}
					
					if (minCondition.length() > 0 && maxCondition.length() > 0) { // BETWEEN
						JSONObject code = new JSONObject();
						JSONObject taxonomyObject = getHealthCodeDesription(minCondition);
						code.put("min_condition", minCondition);
						code.put("min_description", taxonomyObject.getString("description"));
						code.put("max_condition", maxCondition);
						code.put("max_description", getHealthCodeDesription(maxCondition).getString("description"));
						String is_top_level_term=null;
						if (!taxonomyObject.isNull("is_top_level_term")) {
							is_top_level_term=taxonomyObject.getString("is_top_level_term");
						}
						healthCode.put("is_top_level_term", is_top_level_term);
						healthCode.put("code", code);
					}
					else if (minCondition.length() > 0 && maxCondition.length() == 0) { // LIKE
						healthCode.put("code", minCondition);
						JSONObject taxonomyObject = getHealthCodeDesription(minCondition);
						healthCode.put("description", taxonomyObject.getString("description"));
						String is_top_level_term=null;
						if (!taxonomyObject.isNull("is_top_level_term")) {
							is_top_level_term=taxonomyObject.getString("is_top_level_term");
						}
						healthCode.put("is_top_level_term", is_top_level_term);
					}
					else {
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
			PGSQLQueryUtility.close(statement);
		}					
	}

	/**
	 * Add covariates to an investigation
	 *	
	 * View for data: RIF40_INV_COVARIATES
	 *
     * @param JSONArray covariateArray (required)
     * @param String studyID (required)
     * @param int invID (required)
     */			
	private void addCovariates(JSONArray covariateArray, String studyID, int invID)
					throws Exception {
		SQLGeneralQueryFormatter rifInvCovariatesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		rifInvCovariatesQueryFormatter.addQueryLine(0, "SELECT covariate_name,min,max,geography,study_geolevel_name");
		rifInvCovariatesQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_inv_covariates");
		rifInvCovariatesQueryFormatter.addQueryLine(0, " WHERE study_id = ? AND inv_id = ?");
		PreparedStatement statement = createPreparedStatement(connection, rifInvCovariatesQueryFormatter);
		
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
						
						if (name.equals("covariate_name") ) {
							covariate.put("name", value);	
						}
						else if (name.equals("min") ) {
							covariate.put("minimum_value", value);
						}
						else if (name.equals("max") ) {
							covariate.put("maximum_value", value);
						}
						else {
							covariate.put(name, value);	
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
			PGSQLQueryUtility.close(statement);
		}				
	}
	
	/**
	 * Add investigation to a study
	 *	
	 * View for data: RIF40_INVESTIGATIONS
	 *
     * @param JSONArray investigation (required)
     * @param String geographyName (required)
     */		
	private void addInvestigations(JSONArray investigation, String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifInvestigationsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifInvestigationsQueryFormatter.addQueryLine(0, "SELECT inv_id,inv_name,year_start,year_stop,");
		rifInvestigationsQueryFormatter.addQueryLine(0, "       max_age_group,min_age_group,genders,numer_tab,");
		rifInvestigationsQueryFormatter.addQueryLine(0, "       mh_test_type,inv_description,classifier,classifier_bands,investigation_state");
		rifInvestigationsQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_investigations");
		rifInvestigationsQueryFormatter.addQueryLine(0, " WHERE study_id = ? ORDER BY inv_id");
		PreparedStatement statement = createPreparedStatement(connection, rifInvestigationsQueryFormatter);

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

						if (name.equals("inv_name") ) {
							investigationObject.put("title", value);
						}
						else if (name.equals("username") ) {
							additionalData.put("extracted_by", value);	
						}
						else if (name.equals("numer_tab") ) {
							numeratorTable=value;
							JSONObject numerator_denominator_pair = new JSONObject();
							JSONObject health_theme = new JSONObject();
							addNumeratorDenominatorPair(numeratorTable, 
								numerator_denominator_pair, health_theme, geographyName);

							investigationObject.put("health_theme", health_theme);
							investigationObject.put("numerator_denominator_pair", numerator_denominator_pair);
						}
						else if (name.equals("min_age_group") ) {
							minAgeGroup=Integer.parseInt(value);
						}
						else if (name.equals("max_age_group") ) {
							maxAgeGroup=Integer.parseInt(value);
						}
						else if (name.equals("year_start") ) {
							yearStart=Integer.parseInt(value);
							year_range.put("lower_bound", yearStart);
						}
						else if (name.equals("year_stop") ) {
							yearStop=Integer.parseInt(value);
							year_range.put("upper_bound", yearStop);
						}
						else if (name.equals("inv_id") ) {
							invId=Integer.parseInt(value);
							additionalData.put(name, invId);
						}
						else if (name.equals("genders") ) {
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
						}
						else {
							additionalData.put(name, value);
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
			PGSQLQueryUtility.close(statement);
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
     * @param int offset (required)
     * @param String tableName (required)
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
		
		ResultSet resultSet = null;
		PreparedStatement statement = createPreparedStatement(connection, ageSexGroupQueryFormatter);
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
			PGSQLQueryUtility.close(statement);
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
     * @param String numeratorTable (required)
     * @param JSONObject numerator_denominator_pair (required)
     * @param JSONObject health_theme (required)
     * @param String geographyName (required)
     */	
	private void addNumeratorDenominatorPair(String numeratorTable, 
						JSONObject numerator_denominator_pair, JSONObject health_theme, 
						String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifNumDenomQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifNumDenomQueryFormatter.addQueryLine(0, "SELECT a.geography, a.numerator_table, a.numerator_description,");
		rifNumDenomQueryFormatter.addQueryLine(0, "       a.theme_description, a.denominator_table, a.denominator_description, b.theme");
		rifNumDenomQueryFormatter.addQueryLine(0, "  FROM rif40_num_denom a");
		rifNumDenomQueryFormatter.addQueryLine(0, "       LEFT OUTER JOIN rif40.rif40_health_study_themes b ON (a.theme_description = b.description)");
		rifNumDenomQueryFormatter.addQueryLine(0, " WHERE a.geography = ? AND a.numerator_table = ?");
		PreparedStatement statement = createPreparedStatement(connection, rifNumDenomQueryFormatter);

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

					if (name.equals("numerator_table") ) {
						numerator_denominator_pair.put("numerator_table_name", value);
					}
					else if (name.equals("numerator_description") ) {
						numerator_denominator_pair.put("numerator_table_description", value);
					}
					else if (name.equals("denominator_table") ) {
						numerator_denominator_pair.put("denominator_table_name", value);
					}
					else if (name.equals("denominator_description") ) {
						numerator_denominator_pair.put("denominator_description", value);
					}
					else if (name.equals("theme_description") ) {
						health_theme.put("description", value);
					}
					else if (name.equals("theme") ) {
						health_theme.put("name", value);
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
			PGSQLQueryUtility.close(statement);
		}
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