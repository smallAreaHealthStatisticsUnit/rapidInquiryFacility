package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;

import java.sql.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Date;
import java.util.Calendar;


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
 * @author phamly
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
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================
	public GetStudyJSON(
			final RIFServiceStartupOptions rifServiceStartupOptions) {
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
	}
	
	public JSONObject addRifStudiesJson(Connection connection, String studyID) 
					throws Exception {
		this.connection=connection;
		this.studyID=studyID;
		
		SQLGeneralQueryFormatter rifStudiesQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		ResultSetMetaData rsmd = null;
		int columnCount = 0;
		JSONObject rif_job_submission = new JSONObject();
		
		rifStudiesQueryFormatter.addQueryLine(0, "SELECT *");
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
			String geographyName=null;
			Calendar calendar = Calendar.getInstance();
			rif_output_options.put("rif_output_option", new String[] { "Data", "Maps", "Ratios and Rates" });

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				String value = resultSet.getString(i);
				Date dateValue=null;
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
				else if (name.equals("project_description") ) {
					rif_project.put("description", value);	
				}
				else if (name.equals("username") ) {
					rif_project.put("extracted_by", value);	
				}
				else if (name.equals("study_date") ) {
					dateValue=resultSet.getDate(i, calendar);
					rif_job_submission.put("job_submission_date", dateValue);	
				}
				else if (name.equals("authorised_on") ) {
					dateValue=resultSet.getDate(i, calendar);
					rif_job_submission.put(name, dateValue);	// DD/MM/YY HH24:MI:SS
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
						calculation_method.put("name", value + "_r_procedure");
						calculation_method.put("code_routine_name", value + "_r_procedure");
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
					rif_job_submission.put(name, value);	
				}
			}
			rif_job_submission.put("project", rif_project);	
			addInvestigations(investigation, geographyName);
			investigations.put("investigation", investigation);
			study_type.put("investigations", investigations);
			rif_job_submission.put("disease_mapping_study", study_type);
			rif_job_submission.put("rif_output_options", rif_output_options);

			if (resultSet.next()) {
				throw new Exception("addRifStudiesJson(): expected 1 row, got >1");
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
	
	private void addInvestigations(JSONArray investigation, String geographyName)
					throws Exception {
		SQLGeneralQueryFormatter rifInvestigationsQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		
		rifInvestigationsQueryFormatter.addQueryLine(0, "SELECT * FROM rif40.rif40_investigations WHERE study_id = ?");
		PreparedStatement statement = createPreparedStatement(connection, rifInvestigationsQueryFormatter);

		try {		
			statement.setInt(1, Integer.parseInt(studyID));	
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();

				do {
					JSONObject investigationObject = new JSONObject();
					JSONObject age_band = new JSONObject();
					JSONObject health_codes = new JSONObject();
					JSONObject year_range = new JSONObject();
					JSONObject year_intervals = new JSONObject();
					JSONArray year_interval = new JSONArray();
					JSONArray covariates = new JSONArray();
					int yearStart=0;
					int yearStop=0;
					int minAgeGroup=0;
					int maxAgeGroup=0;
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
							investigationObject.put("extracted_by", value);	
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
							investigationObject.put(name, value);
						}
					}
					JSONObject lower_age_group=addAgeSexGroup(minAgeGroup /* Offset */, numeratorTable);
					age_band.put("lower_age_group", lower_age_group);
					JSONObject upper_age_group=addAgeSexGroup(maxAgeGroup /* Offset */, numeratorTable);
					age_band.put("upper_age_group", upper_age_group);
					investigationObject.put("age_band", age_band);
					investigationObject.put("health_codes", health_codes);
					investigationObject.put("year_range", year_range);
					for (int j=yearStart;j<=yearStop;j++) {
						JSONObject yearInterval = new JSONObject();
						yearInterval.put("start_year", j);
						yearInterval.put("stop_year", j);
						year_interval.put(yearInterval);
					}
					year_intervals.put("year_interval", year_interval);
					investigationObject.put("year_intervals", year_intervals);
					investigationObject.put("years_per_interval", 1);
					investigationObject.put("covariates", covariates); // Got to here
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
	
	private void addNumeratorDenominatorPair(String numeratorTable, 
						JSONObject numerator_denominator_pair, JSONObject health_theme, String geographyName)
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
						health_theme.put("theme_description", value);
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