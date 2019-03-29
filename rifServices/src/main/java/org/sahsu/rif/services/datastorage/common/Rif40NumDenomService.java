package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.system.RIFServiceMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Creates Rif40NumDenomService object JSON used by rifc-dsub-main.js;
 *
 * - CREATE/re CREATEs the users rif40_num_denom VIEW; preferably by diffing the VIEW text
 *
 * Todo:
 *
 * - The performance of the rif40_num_denom query needs improving
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class Rif40NumDenomService extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final String lineSeparator = System.getProperty("line.separator");

	private static DatabaseType databaseType;
	
	/**
	 * Constructor
	 *
	 * @param options RIFServiceStartupOptions for the extract directory
	 */
	public Rif40NumDenomService(final RIFServiceStartupOptions options) {

		super(options);
		if (rifDatabaseProperties == null) {
			rifDatabaseProperties = options.getRIFDatabaseProperties();
		}
		if (rifDatabaseProperties != null) {
			databaseType = rifDatabaseProperties.getDatabaseType();
		}
	}    
    
	/**
	 * Gets Rif40NumDenomService object JSON
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return Rif40NumDenomService object JSON
	 * @throws RIFServiceException the RIF service exception
	 */    
    public JSONObject get(
			final Connection connection,
			final User user) 
			throws RIFServiceException { 
        JSONArray rif40NumDenomPairs=getRif40NumDenomData(connection, user);
        createRif40numeratorOutcomeColumns(connection, user);
		
        return createRif40NumDenomServiceJson(rif40NumDenomPairs);
    }
    
 	/**
	 * Gets create Rif40NumDenomService Json
	 *
	 * @param rif40NumDenomPairs JSONArray from RIF40_NUM_DENOM query
	 * @return Rif40NumDenomService JSONObject
	 */
    private JSONObject createRif40NumDenomServiceJson(final JSONArray rif40NumDenomPairs) {
        
/* Convert results to required JSON format
rif40NumDenomServiceJson = {
themes: [{
        "name": "cancers",
        "description": "covering various types of cancers"
    },{
        "name": "HEATHROW",
        "description": "HEATHROW Study"
    },{
        "name": "SYDVAST_TEST",
        "description": "South west Sweden test theme"
    }
],
geographies: {
    "South west Sweden test theme": {
        geographyList: ["STOCKHOLM", "SWEDEN_COUNTY", "SYDVAST"],
        geographyDescriptions: {
            "STOCKHOLM": "Stockholm, Sweden",
            "SWEDEN_COUNTY": "Sverige to county level",
            "SYDVAST": "South West Sweden"
        },
        "STOCKHOLM": [{
                numerator_table_name: "STOCKHOLM_CANCER",
                numerator_table_description: "Stockholm Cancer data 19??-20??",
                denominator_table_name: "STOCKHOLM_POPULATION",
                denominator_table_description: "Stockholm Population 19??-20??.",
                automatic: 1
            }
        ],
        "SWEDEN_COUNTY": [{
                numerator_table_name: "SWEDEN_CANCER",
                numerator_table_description: "Sweden Cancer data 19??-20??",
                denominator_table_name: "SWEDEN_POPULATION",
                denominator_table_description: "Sweden Population 19??-20??.",
                automatic: 1
            }
        ],
        "SYDVAST": [{
                numerator_table_name: "SYDVAST_TEST_1",
                numerator_table_description: "Sydvast test 1 data 2008-2016",
                denominator_table_name: "SYDVAST_TEST_1_POPULATION",
                denominator_table_description: "Sydvast test 1 population 2008-2016.",
                automatic: 0
            }, {
                numerator_table_name: "SYDVAST_TEST_2",
                numerator_table_description: "Sydvast test 2 data 2008-2016",
                denominator_table_name: "SYDVAST_TEST_2_POPULATION",
                denominator_table_description: "Sydvast test 2 population 2008-2016.",
                automatic: 0
            }, {
                numerator_table_name: "SYDVAST_TEST_3",
                numerator_table_description: "Sydvast test 3 data 2008-2016",
                denominator_table_name: "SYDVAST_TEST_3_POPULATION",
                denominator_table_description: "Sydvast test 3 population 2008-2016.",
                automatic: 0
            }, {
                numerator_table_name: "SYDVAST_TEST_4",
                numerator_table_description: "Sydvast test 4 data 2008-2016",
                denominator_table_name: "SYDVAST_TEST_4_POPULATION",
                denominator_table_description: "Sydvast test 4 population 2008-2016.",
                automatic: 0
            }
        ],
    }
    "covering various types of cancers": {
        "geographyList": ["SAHSULAND"],
        geographyDescriptions: {
            "SAHSULAND": "SAHSU Example geography"
        },
        "SAHSULAND": [{
            numerator_table_name: " NUM_SAHSULAND_CANCER ",
            numerator_table_description: " cancer numerator "
            denominator_table_name: " POP_SAHSULAND_POP ",
            denominator_table_description: " population health file ",
            automatic: 1
            }
        ]
    }
}
};
*/
        JSONObject row;
        // Build a hash of themes using description as the key
        HashMap<String, String> themeList = new HashMap<>();
        for (int i=0; i<rif40NumDenomPairs.length(); i++) {
            row=rif40NumDenomPairs.getJSONObject(i);
            String themeDescription=row.optString("themeDescription");
            String themeName=row.optString("themeName");
            if (themeName != null && themeDescription != null) {
                themeList.put(themeDescription, themeName);
            }
        }
        
        JSONArray themeListJson=new JSONArray();
        JSONObject geographiesJson=new JSONObject();
        
        // Build themes object and themeList array
        for (HashMap.Entry<String, String> entry : themeList.entrySet()) {
            String themeName=entry.getValue();
            String themeDescription=entry.getKey();
            JSONObject themesJson=new JSONObject();
            themesJson.put("name", themeName);
            themesJson.put("description", themeDescription);
            themeListJson.put(themesJson);
        }
        
        // Foreach theme description build a list of RIF40_NUM_DENOM pairs by geography
        for (HashMap.Entry<String, String> entry : themeList.entrySet()) {
//            String themeName=entry.getValue();
            String themeDescription=entry.getKey();
            JSONArray geographyListJson=new JSONArray();
            JSONObject geographyDescriptionsJson=new JSONObject();
            JSONArray themeGeographyArray;
            JSONObject themeDescriptionJson=new JSONObject();
            for (int i=0; i<rif40NumDenomPairs.length(); i++) {
                row=rif40NumDenomPairs.getJSONObject(i);
                JSONObject themeNumDenomJson=new JSONObject();
                for(int j=0; j<row.names().length(); j++) {
                    themeNumDenomJson.put(row.names().getString(j), 
                        row.optString(row.names().getString(j)));
                }

                String themeDescriptionStr=row.optString("themeDescription");
                String geographyDescription=row.optString("geographyDescription");
                String geographyName=row.optString("geographyName");
                
                if (themeDescriptionStr != null && themeDescriptionStr.equals(themeDescription) && 
                    geographyName != null && geographyDescription != null) {
                    if (!geographyDescriptionsJson.has(geographyName)) {
                        geographyListJson.put(geographyName);
                        geographyDescriptionsJson.put(geographyName, geographyDescription);
                    }
                    
                    if (!themeDescriptionJson.has(geographyName)) { 
                        themeGeographyArray=new JSONArray();
                        themeDescriptionJson.put(geographyName, themeGeographyArray);
                    }
                    else {
                        themeGeographyArray=themeDescriptionJson.getJSONArray(geographyName);
                    }
                    themeGeographyArray.put(themeNumDenomJson);
                }
            }
            
            // Create theme driven view of geography/numerator/denominator data
            themeDescriptionJson.put("geographyList", geographyListJson);
            themeDescriptionJson.put("geographyDescriptions", geographyDescriptionsJson);
            
            geographiesJson.put(themeDescription, themeDescriptionJson);
        }
        
        JSONObject rif40NumDenomServiceJson = new JSONObject();
        rif40NumDenomServiceJson.put("themes", themeListJson);
        rif40NumDenomServiceJson.put("geographies", geographiesJson);
        
        return rif40NumDenomServiceJson;
    }
    
	/**
	 * Gets RIF40_NUM_DENOM data as a JSONArray
     *
     *    geography   |           geography_desciption           |   numerator_table    |             numerator_description             |  theme_name  |         theme_description         |     denominator_table     |                                 denominator_description                                  | automatic
     * ---------------+------------------------------------------+----------------------+-----------------------------------------------+--------------+-----------------------------------+---------------------------+------------------------------------------------------------------------------------------+-----------
     *  HALLAND       | Halland County, Sweden                   | HALLAND_CANCER       | Halland Cancer data 19??-20??                 | cancers      | covering various types of cancers | HALLAND_POPULATION        | Halland Population 19??-20??.                                                            |         1
     *  SAHSULAND     | SAHSU Example geography                  | NUM_SAHSULAND_CANCER | cancer numerator                              | cancers      | covering various types of cancers | POP_SAHSULAND_POP         | population health file                                                                   |         1
     *  STOCKHOLM     | Stockholm, Sweden                        | STOCKHOLM_CANCER     | Stockholm Cancer data 19??-20??               | cancers      | covering various types of cancers | STOCKHOLM_POPULATION      | Stockholm Population 19??-20??.                                                          |         1
     *  SWEDEN_COUNTY | Sverige to county level                  | SWEDEN_CANCER        | Sweden Cancer data 19??-20??                  | cancers      | covering various types of cancers | SWEDEN_POPULATION         | Sweden Population 19??-20??.                                                             |         1
     *  SYDVAST       | South West Sweden                        | SYDVAST_TEST_1       | Sydvast test 1 data 2008-2016                 | SYDVAST_TEST | South west Sweden test theme      | SYDVAST_TEST_1_POPULATION | Sydvast test 1 population 2008-2016.                                                     |         0
     *  SYDVAST       | South West Sweden                        | SYDVAST_TEST_2       | Sydvast test 2 data 2008-2016                 | SYDVAST_TEST | South west Sweden test theme      | SYDVAST_TEST_2_POPULATION | Sydvast test 2 population 2008-2016.                                                     |         0
     *  SYDVAST       | South West Sweden                        | SYDVAST_TEST_3       | Sydvast test 3 data 2008-2016                 | SYDVAST_TEST | South west Sweden test theme      | SYDVAST_TEST_3_POPULATION | Sydvast test 3 population 2008-2016.                                                     |         0
     *  SYDVAST       | South West Sweden                        | SYDVAST_TEST_4       | Sydvast test 4 data 2008-2016                 | SYDVAST_TEST | South west Sweden test theme      | SYDVAST_TEST_4_POPULATION | Sydvast test 4 population 2008-2016.                                                     |         0
     *  USA_2014      | US 2014 Census geography to county level | SEER_CANCER          | SEER Cancer data 1973-2013. 9 States in total | cancers      | covering various types of cancers | SEER_POPULATION           | SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total |         1
     * (9 rows)
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return RIF40_NUM_DENOM as a JSONArray
	 * @throws RIFServiceException the RIF service exception
	 */
	private JSONArray getRif40NumDenomData(
			final Connection connection,
			final User user) 
			throws RIFServiceException {             
        JSONArray result=new JSONArray();
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		String sqlQueryText = null;  
        
 		try {
            SQLGeneralQueryFormatter queryFormatter = formatRif40NumDenomQuery(connection, user);
			String databaseViewDefinition = getViewDefinition(connection, user.getUserID(), "rif40_num_denom");
			String localViewDefinition = queryFormatter.generateQuery();
			
			HashMap<String, String> columnCommentHash = new HashMap<>();	

			columnCommentHash.put("geography", "Geography");
			columnCommentHash.put("geography_description", "Geography description");
			columnCommentHash.put("numerator_table", "Numerator table");
			columnCommentHash.put("numerator_description", "Numerator table description");
			columnCommentHash.put("denominator_table", "Denominator table");
			columnCommentHash.put("denominator_description", "Denominator table description");
			columnCommentHash.put("theme_name", "Numerator table health study theme name");
			columnCommentHash.put("theme_description", "Numerator table health study theme description");
			columnCommentHash.put("automatic", "Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.");
 
            if (viewsAreDifferent(user, localViewDefinition, databaseViewDefinition, "rif40_num_denom")) {
				createRifView(connection, user.getUserID().toLowerCase(), localViewDefinition,
					"rif40_num_denom", columnCommentHash);
				commentObject(connection, "VIEW", user.getUserID().toLowerCase(), "rif40_num_denom", 
					"Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.");	
            }	
            sqlQueryText = logSQLQuery(
					"getNumeratorDenominatorPairs",
					queryFormatter);

			//Parameterise and execute query
			statement
					= createPreparedStatement(
					connection,
					queryFormatter);

			dbResultSet = statement.executeQuery();
			connection.commit();

			while (dbResultSet.next()) {
                JSONObject row=new JSONObject();
                
                ResultSetMetaData rsmd = dbResultSet.getMetaData();
                int columnCount = rsmd.getColumnCount();

                // The column count starts from 1
                for (int i = 1; i <= columnCount; i++ ) {
                    String name = rsmd.getColumnName(i);
                    if (name.equals("numerator_table")) {
                        name = "numerator_table_name";
                    }
                    else if (name.equals("denominator_table")) {
                        name = "denominator_table_name";
                    }
                    else if (name.equals("numerator_description")) {
                        name = "numerator_table_description";
                    }
                    else if (name.equals("denominator_description")) {
                        name = "denominator_table_description";
                    }
                    else if (name.equals("geography")) {
                        name = "geography_name";
                    }
                    String value = dbResultSet.getString(i);
                    row.put(jsonCapitalise(name), value);
                }
                result.put(row);
			}

			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			RIFSQLException rifSQLException = new RIFSQLException(
                this.getClass(), sqlException, statement, sqlQueryText);
			SQLQueryUtility.rollback(connection);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");

			throw new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage,
                    rifSQLException);
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
       
        return result;        
    }

	private void createRif40numeratorOutcomeColumns(
			final Connection connection,
			final User user) 
			throws RIFServiceException {     

		SQLGeneralQueryFormatter queryFormatter = formatRif40numeratorOutcomeColumnsQuery(connection, user);
		String databaseViewDefinition = getViewDefinition(connection, user.getUserID(), 
			"rif40_numerator_outcome_columns");
		String localViewDefinition = queryFormatter.generateQuery();
		
		HashMap<String, String> columnCommentHash = new HashMap<>();	

		columnCommentHash.put("geography", "Geography");
		columnCommentHash.put("table_name", "Numerator table");
		columnCommentHash.put("table_description", "Numerator table description");
		columnCommentHash.put("outcome_group_name", "Outcome Group Name. E.g SINGLE_VARIABLE_ICD");
		columnCommentHash.put("outcome_type", "Outcome type: ICD, ICD-0 or OPCS");
		columnCommentHash.put("outcome_group_description", "Outcome Group Description. E.g. Single variable ICD");
		columnCommentHash.put("field_name", "Numerator field name (will only the actual field name if the multiple field count is zero.");
		columnCommentHash.put("multiple_field_count", "Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to be left padded to 2 characters with \"0\" and preceded by an underscore");
		columnCommentHash.put("columnn_exists", "Does the column exist true/false");
		columnCommentHash.put("column_comment", "Numerator field comment");

		if (viewsAreDifferent(user, localViewDefinition, databaseViewDefinition,
			"rif40_numerator_outcome_columns")) {
			createRifView(connection, user.getUserID().toLowerCase(), localViewDefinition,
				"rif40_numerator_outcome_columns", columnCommentHash);
			commentObject(connection, "VIEW", user.getUserID().toLowerCase(), "rif40_numerator_outcome_columns", 
				"All numerator outcome fields (columns)");
		}
	}
			
   /** Check if views are different. This will not work for Postgres as the original VIEW text is NOT stored 
     * and it will always be re-created
	 *
     * @param user User
	 * @param localViewDefinition local view definition
	 * @param databaseViewDefinition database view definition
	 * @param ViewName View name
     * @return boolean true if views are different 
	 */     
    private boolean viewsAreDifferent(
        final User user,
        final String localViewDefinition,
        final String databaseViewDefinition,
		final String viewName) {
        int diffCount=0;
        if (databaseViewDefinition == null) { // Does NOT exist!
            rifLogger.info(this.getClass(), user.getUserID() + 
                "." + viewName + " needs to be created in the database");
            return true;
        }
        else if (localViewDefinition != null && localViewDefinition.equals(databaseViewDefinition)) {
            rifLogger.info(this.getClass(), user.getUserID() + 
                "." + viewName + " is the same as the database");
            return false;
        }
		else if (localViewDefinition != null) {
			String[] localViewDefinitionLines = localViewDefinition.split(lineSeparator);
			String[] databaseViewDefinitionLines = databaseViewDefinition.split(lineSeparator);
            StringBuilder diffReport = new StringBuilder();
            diffReport.append("View difference report" + lineSeparator);
            if (databaseViewDefinitionLines.length > localViewDefinitionLines.length) {
                diffReport.append("* Database view is longer by " +
                    (databaseViewDefinitionLines.length - localViewDefinitionLines.length) + " lines" + lineSeparator);
                diffCount+=(databaseViewDefinitionLines.length - localViewDefinitionLines.length);
            }
            else if (databaseViewDefinitionLines.length < localViewDefinitionLines.length) {
                diffReport.append("* New view is longer by " +
                    (localViewDefinitionLines.length - databaseViewDefinitionLines.length) + " lines" + lineSeparator);
                diffCount+=(localViewDefinitionLines.length - databaseViewDefinitionLines.length);
            }
            for (int i=0; i<localViewDefinitionLines.length; i++) {
                if (localViewDefinitionLines[i] != null) {
                    if (databaseViewDefinitionLines.length > i &&
                        databaseViewDefinitionLines[i] != null) {
                        if (!localViewDefinitionLines[i].trim().equals(databaseViewDefinitionLines[i].trim())) {
                            diffReport.append("[" + (i+1) + "] diff" + lineSeparator +
                                "db  >>>" + databaseViewDefinitionLines[i].trim() + "<<<" + lineSeparator +
                                "gen <<<" + localViewDefinitionLines[i].trim() + ">>>" + lineSeparator);
                            diffCount++;
                        }
                    }
                    else {
                        diffReport.append("[" + (i+1) + "] no database line >>> " + 
                            localViewDefinitionLines[i] + lineSeparator);
                        diffCount++;
                    }
                }
            }
			
            if (diffCount > 0) {
                rifLogger.info(this.getClass(), user.getUserID() + 
                    "." + viewName + " needs updating; " + diffCount + " differences " + lineSeparator + "Database >>>" + lineSeparator +
                    databaseViewDefinition + "<<<" + lineSeparator + "New >>>" + lineSeparator +
                    localViewDefinition + "<<<" + lineSeparator +
                    diffReport.toString());	
                return true;
            }
            else {      
                rifLogger.info(this.getClass(), user.getUserID() + 
                    "." + viewName + " is the same as the database");
                return false;
            }
        }
		else {
			return false;
		}
    }
    
   /** create RIF40_NUM_DENOM/RIF40_NUMERATOR_OUTCOME_COLUMNS View
	 *
	 * @param connection the connection
	 * @param schemaName schema name
	 * @param newViewDefinition new view definition SQL
	 * @param viewName view name
	 * @param columnCommentHash column comments hash
	 * @throws RIFServiceException the RIF service exception
	 */  
	private void createRifView(
			final Connection connection,
			final String schemaName,
			final String newViewDefinition,
			final String viewName,
			final HashMap<String, String> columnCommentHash)
			throws RIFServiceException {
		Statement statement = null;
		String sqlQueryText = null;  
        
 		try {
            SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
            SQLGeneralQueryFormatter queryFormatter2 = new SQLGeneralQueryFormatter();
			if (databaseType == DatabaseType.POSTGRESQL) {
				queryFormatter.addQueryLine(0, "DROP VIEW IF EXISTS " + 
					schemaName + "." + viewName + " CASCADE;");
				queryFormatter2.addQueryLine(0, "CREATE VIEW " + 
					schemaName + "." + viewName + " AS ");
			}
			else if (databaseType == DatabaseType.SQL_SERVER) {
				queryFormatter.addQueryLine(0, "IF EXISTS (SELECT * FROM sys.objects");
				queryFormatter.addQueryLine(0, "           WHERE object_id = OBJECT_ID(N'[" + 
					schemaName + "].[" + viewName + "]') AND type in (N'V'))");
				queryFormatter.addQueryLine(0, "BEGIN");
				queryFormatter.addQueryLine(0, "	DROP VIEW [" + schemaName + "].[" + viewName + "];");
				queryFormatter.addQueryLine(0, "END;");
				queryFormatter2.addQueryLine(0, "CREATE VIEW [" + 
					schemaName + "].[" + viewName + "] AS ");
			}
			else {
				throw new SQLException("createRifView(): invalid databaseType: " +
					databaseType);
			}
			queryFormatter2.addQueryLine(0, newViewDefinition);
            
            sqlQueryText = logSQLQuery(
					"dropView",
					queryFormatter);
            statement = connection.createStatement();
			statement.execute(queryFormatter.generateQuery());
			statement.close();
			
            sqlQueryText = logSQLQuery(
					"createView",
					queryFormatter2);

            statement = connection.createStatement();
			statement.execute(queryFormatter2.generateQuery());
			
			for (String columnName : columnCommentHash.keySet()) {
				String comment = columnCommentHash.get(columnName);
				commentColumn(connection, "VIEW", schemaName, viewName, 
					columnName, comment);
			}		

			SQLQueryUtility.commit(connection);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			RIFSQLException rifSQLException = new RIFSQLException(
                this.getClass(), sqlException, statement, sqlQueryText);
			SQLQueryUtility.rollback(connection);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");

			throw new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage,
                    rifSQLException);
		}
		finally {
			//Cleanup database resources
            SQLQueryUtility.close(statement);
		}
	}
	
	/**
	 * Format SQL query (replacement for rif40_numerator_outcome_columns)
     *
 	 * WITH a AS (
 	 * 	SELECT z.geography,
 	 * 	  	   a_1.table_name,
 	 * 		   z.numerator_description AS table_description,
 	 * 		   c.outcome_group_name,
 	 * 		   c.outcome_type,
 	 * 		   c.outcome_group_description,
 	 * 		   c.field_name,
 	 * 		   c.multiple_field_count
 	 * 	  FROM rif40_num_denom z, -* RIF40's version of the code *-
 	 * 		   rif40_tables a_1,
 	 * 		   rif40_table_outcomes b,
 	 * 		   rif40_outcome_groups c
 	 * 	 WHERE a_1.table_name       = z.numerator_table
 	 * 	   AND a_1.table_name       = b.numer_tab
 	 * 	   AND c.outcome_group_name = b.outcome_group_name
 	 * )
 	 * SELECT a.geography,
 	 *        a.table_name,
 	 *        a.table_description,
 	 *        a.outcome_group_name,
 	 *        a.outcome_type,
 	 *        a.outcome_group_description,
 	 *        a.field_name,
 	 *        a.multiple_field_count,
 	 *        CASE
 	 *             WHEN d.attrelid IS NOT NULL THEN true
 	 *             ELSE false
  	 *       END AS columnn_exists,
 	 *        CASE
 	 *             WHEN d.attrelid IS NOT NULL THEN col_description(LOWER(a.table_name)::regclass::oid, d.attnum::integer)
 	 *             ELSE NULL::text
 	 *        END AS column_comment
 	 *   FROM a
 	 *        LEFT JOIN pg_attribute d ON (LOWER(a.table_name)::regclass::oid = d.attrelid AND d.attname = lower(a.field_name));
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return SQLGeneralQueryFormatter object
	 * @throws RIFServiceException the RIF service exception
	 */  
    private SQLGeneralQueryFormatter formatRif40numeratorOutcomeColumnsQuery(
			final Connection connection,
			final User user)
			throws RIFServiceException {
        SQLGeneralQueryFormatter queryFormatter =  new SQLGeneralQueryFormatter();
        
        try {
            queryFormatter.addQueryLine(0, "WITH a AS (");
            queryFormatter.addQueryLine(0, "     SELECT z.geography,");
            queryFormatter.addQueryLine(0, "		a_1.table_name,");
            queryFormatter.addQueryLine(0, "		z.numerator_description AS table_description,");
            queryFormatter.addQueryLine(0, "		c.outcome_group_name,");
            queryFormatter.addQueryLine(0, "		c.outcome_type,");
            queryFormatter.addQueryLine(0, "		c.outcome_group_description,");
            queryFormatter.addQueryLine(0, "		c.field_name,");
            queryFormatter.addQueryLine(0, "		c.multiple_field_count");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
				queryFormatter.addQueryLine(0, "	   FROM rif40_num_denom z,"); /* User's version of the code */
				queryFormatter.addQueryLine(0, "		[rif40].[rif40_tables] a_1,");
				queryFormatter.addQueryLine(0, "		[rif40].[rif40_table_outcomes] b,");
				queryFormatter.addQueryLine(0, "		[rif40].[rif40_outcome_groups] c");
			}
			else {
				queryFormatter.addQueryLine(0, "	   FROM rif40_num_denom z,"); /* User's version of the code */
				queryFormatter.addQueryLine(0, "		rif40_tables a_1,");
				queryFormatter.addQueryLine(0, "		rif40_table_outcomes b,");
				queryFormatter.addQueryLine(0, "		rif40_outcome_groups c");
			}				
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
				queryFormatter.addQueryLine(0, "	  WHERE a_1.table_name       = z.numerator_table");
				queryFormatter.addQueryLine(0, "		AND a_1.table_name       = b.numer_tab");
				queryFormatter.addQueryLine(0, "		AND c.outcome_group_name = b.outcome_group_name");
			}
			else {
			    queryFormatter.addQueryLine(0, "	  WHERE (((a_1.table_name)::text = (z.numerator_table)::text) AND ((a_1.table_name)::text = (b.numer_tab)::text) AND ((c.outcome_group_name)::text = (b.outcome_group_name)::text))");
			}
            queryFormatter.addQueryLine(0, ")");
            queryFormatter.addQueryLine(0, "SELECT a.geography,");
            queryFormatter.addQueryLine(0, "       a.table_name,");
            queryFormatter.addQueryLine(0, "       a.table_description,");
            queryFormatter.addQueryLine(0, "       a.outcome_group_name,");
            queryFormatter.addQueryLine(0, "       a.outcome_type,");
            queryFormatter.addQueryLine(0, "       a.outcome_group_description,");
            queryFormatter.addQueryLine(0, "       a.field_name,");
            queryFormatter.addQueryLine(0, "       a.multiple_field_count,");
            queryFormatter.addQueryLine(0, "       CASE");
			
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {	
				queryFormatter.addQueryLine(0, "           WHEN d.name IS NOT NULL THEN 'true'");
				queryFormatter.addQueryLine(0, "           ELSE 'false'");
			}
			else {
				queryFormatter.addQueryLine(0, "           WHEN (d.attrelid IS NOT NULL) THEN true");
				queryFormatter.addQueryLine(0, "           ELSE false");
			}
            queryFormatter.addQueryLine(0, "       END AS columnn_exists,");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {	
				queryFormatter.addQueryLine(0, "       e.value AS column_comment");
			}
			else {
				queryFormatter.addQueryLine(0, "       CASE");
				queryFormatter.addQueryLine(0, "            WHEN (d.attrelid IS NOT NULL) THEN col_description(((lower((a.table_name)::text))::regclass)::oid, (d.attnum)::integer)");
				queryFormatter.addQueryLine(0, "            ELSE NULL::text");
				queryFormatter.addQueryLine(0, "       END AS column_comment");
			}
	
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {		
				queryFormatter.addQueryLine(0, "  FROM a");
				queryFormatter.addQueryLine(0, "       LEFT JOIN sys.columns d");
				queryFormatter.addQueryLine(0, "       ON lower(d.name) collate database_default = lower(a.field_name) collate database_default and Object_ID = object_id(a.table_name)");
				queryFormatter.addQueryLine(0, "       LEFT JOIN  sys.extended_properties e");
				queryFormatter.addQueryLine(0, "       ON e.name='MS_Description' and major_id=object_id(a.table_name)");
				queryFormatter.addQueryLine(0, "       and e.minor_id=0");
			}
			else {
				queryFormatter.addQueryLine(0, "  FROM (a");
				queryFormatter.addQueryLine(0, "       LEFT JOIN pg_attribute d ON (((((lower((a.table_name)::text))::regclass)::oid = d.attrelid) AND ((d.attname)::text = lower((a.field_name)::text)))));");
			}
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			SQLQueryUtility.rollback(connection);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");

			throw new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage,
                    exception);
		}
        
        return queryFormatter;
    }
			
	/**
	 * Format SQL query (replacement for rif40_num_denom)
     *
 	 * WITH n AS (
	 *     SELECT n1.geography,
	 *            n1.geography_description,
	 *            n1.numerator_table,
	 *            n1.numerator_description,
	 *            n1.automatic,
	 *            n1.theme_name,
	 *            n1.theme_description
	 *       FROM ( SELECT g.geography,
	 *                     g.description AS geography_description,
	 *                     n.table_name AS numerator_table,
	 *                     n.description AS numerator_description,
	 *                     n.automatic,
	 *                     t.theme AS theme_name,
	 *                     t.description AS theme_description
	 *                FROM rif40_geographies g,
	 *                     rif40_tables n,
	 *                     rif40_health_study_themes t
	 *               WHERE n.isnumerator = 1 AND
	 *                     n.automatic   = 1 AND
	 *                     n.theme       = t.theme AND
	 *                     rif40_is_object_resolvable(n.table_name) = 1) n1
	 *      WHERE rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1
	 * ), d AS (
	 *     SELECT d1.geography,
	 *            d1.denominator_table,
	 *            d1.denominator_description
	 *       FROM ( SELECT g.geography,
	 *                     d.table_name AS denominator_table,
	 *                     d.description AS denominator_description
	 *                FROM rif40_geographies g,
	 *                     rif40_tables d
	 *               WHERE d.isindirectdenominator = 1 AND
	 *                     d.automatic             = 1 AND
	 *                     rif40_is_object_resolvable(d.table_name) = 1) d1
	 *      WHERE rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1 AND
	 *            rif40_auto_indirect_checks(d1.denominator_table) IS NULL
	 * )
	 * SELECT n.geography,
	 *        n.geography_description,
	 *        n.numerator_table,
	 *        n.numerator_description,
	 *        n.theme_name,
	 *        n.theme_description,
	 *        d.denominator_table,
 	 *        d.denominator_description,
 	 *        n.automatic
	 *   FROM n, d
	 *  WHERE n.geography = d.geography
	 * UNION
	 * SELECT ta.geography AS geography,
	 *        ta.geography_description,
	 *        ta.numerator_table,
	 *        ta.numerator_description,
	 *        h.theme AS theme_name,
	 *        h.description AS theme_description,
	 *        ta.denominator_table,
	 *        d.description AS denominator_description,
	 *        0 AS automatic
	 *   FROM ( SELECT nd.geography,
	 *                 g.description AS geography_description,
	 *                 nd.numerator_table,
	 *                 nd.denominator_table,
	 *                 n.description AS numerator_description,
	 *                 n.theme
	 *            FROM peter.t_rif40_num_denom nd
	 *                 LEFT OUTER JOIN rif40_geographies g ON (g.geography = nd.geography)
	 *                 LEFT OUTER JOIN rif40_tables n ON (n.table_name = nd.numerator_table)
	 *           WHERE rif40_is_object_resolvable(nd.numerator_table) = 1 AND
	 *                 rif40_is_object_resolvable(nd.denominator_table) = 1
	 *        ) ta
	 *        LEFT JOIN rif40_tables d              ON d.table_name = ta.denominator_table
	 *        LEFT JOIN rif40_health_study_themes h ON h.theme      = ta.theme
	 *  ORDER BY 1, 2, 4;
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return SQLGeneralQueryFormatter object
	 * @throws RIFServiceException the RIF service exception
	 */  
    private SQLGeneralQueryFormatter formatRif40NumDenomQuery(
			final Connection connection,
			final User user)
			throws RIFServiceException {
        SQLGeneralQueryFormatter queryFormatter =  new SQLGeneralQueryFormatter();
        
        try {
            queryFormatter.addQueryLine(0, "WITH n AS (");
            queryFormatter.addQueryLine(0, "    SELECT n1.geography,");
            queryFormatter.addQueryLine(0, "           n1.geography_description,");
            queryFormatter.addQueryLine(0, "           n1.numerator_table,");
            queryFormatter.addQueryLine(0, "           n1.numerator_description,");
            queryFormatter.addQueryLine(0, "           n1.automatic,");
            queryFormatter.addQueryLine(0, "           n1.theme_name,");
            queryFormatter.addQueryLine(0, "           n1.theme_description");
            queryFormatter.addQueryLine(0, "      FROM ( SELECT g.geography,");
            queryFormatter.addQueryLine(0, "                    g.description AS geography_description,");
            queryFormatter.addQueryLine(0, "                    n.table_name AS numerator_table,");
            queryFormatter.addQueryLine(0, "                    n.description AS numerator_description,");
            queryFormatter.addQueryLine(0, "                    n.automatic,");
            queryFormatter.addQueryLine(0, "                    t.theme AS theme_name,");
            queryFormatter.addQueryLine(0, "                    t.description AS theme_description");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "               FROM rif40.rif40_geographies g,");
                queryFormatter.addQueryLine(0, "                    rif40.rif40_tables n,");
                queryFormatter.addQueryLine(0, "                    rif40.rif40_health_study_themes t");
            }
            else {
                queryFormatter.addQueryLine(0, "               FROM rif40_geographies g,");
                queryFormatter.addQueryLine(0, "                    rif40_tables n,");
                queryFormatter.addQueryLine(0, "                    rif40_health_study_themes t");
            }
            
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "              WHERE n.isnumerator = 1 ");
                queryFormatter.addQueryLine(0, "                AND n.automatic   = 1 ");
                queryFormatter.addQueryLine(0, "                AND n.theme       = t.theme");
                queryFormatter.addQueryLine(0, "                AND [rif40].[rif40_is_object_resolvable](n.table_name) = 1) n1");            
                queryFormatter.addQueryLine(0, "     WHERE [rif40].[rif40_num_denom_validate](n1.geography, n1.numerator_table) = 1");
            }
            else {
                queryFormatter.addQueryLine(0, "              WHERE ((n.isnumerator = 1) AND (n.automatic = 1) AND ((n.theme)::text = (t.theme)::text) AND (rif40_is_object_resolvable(n.table_name) = 1))) n1");
                queryFormatter.addQueryLine(0, "     WHERE (rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1)");
            }
            queryFormatter.addQueryLine(0, "), d AS (");
            queryFormatter.addQueryLine(0, "    SELECT d1.geography,");
            queryFormatter.addQueryLine(0, "           d1.denominator_table,");
            queryFormatter.addQueryLine(0, "           d1.denominator_description");
            queryFormatter.addQueryLine(0, "      FROM ( SELECT g.geography,");
            queryFormatter.addQueryLine(0, "                    d.table_name AS denominator_table,");
            queryFormatter.addQueryLine(0, "                    d.description AS denominator_description");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "               FROM rif40.rif40_geographies g,");
                queryFormatter.addQueryLine(0, "                    rif40.rif40_tables d");
            }
            else {
                queryFormatter.addQueryLine(0, "               FROM rif40_geographies g,");
                queryFormatter.addQueryLine(0, "                    rif40_tables d");
            }
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "              WHERE d.isindirectdenominator = 1");
                queryFormatter.addQueryLine(0, "                AND d.automatic             = 1");
                queryFormatter.addQueryLine(0, "                AND [rif40].[rif40_is_object_resolvable](d.table_name) = 1) d1");
                queryFormatter.addQueryLine(0, "     WHERE [rif40].[rif40_num_denom_validate](d1.geography, d1.denominator_table) = 1");
                queryFormatter.addQueryLine(0, "       AND [rif40].[rif40_auto_indirect_checks](d1.denominator_table) IS NULL");
            }
            else {
                queryFormatter.addQueryLine(0, "                  WHERE ((d.isindirectdenominator = 1) AND (d.automatic = 1) AND (rif40_is_object_resolvable(d.table_name) = 1))) d1");
                queryFormatter.addQueryLine(0, "     WHERE ((rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1) AND (rif40_auto_indirect_checks(d1.denominator_table) IS NULL))");
            }
            queryFormatter.addQueryLine(0, ")");
            queryFormatter.addQueryLine(0, "SELECT n.geography,");
            queryFormatter.addQueryLine(0, "       n.geography_description,");
            queryFormatter.addQueryLine(0, "       n.numerator_table,");
            queryFormatter.addQueryLine(0, "       n.numerator_description,");
            queryFormatter.addQueryLine(0, "       n.theme_name,");
            queryFormatter.addQueryLine(0, "       n.theme_description,");
            queryFormatter.addQueryLine(0, "       d.denominator_table,");
            queryFormatter.addQueryLine(0, "       d.denominator_description,");
            queryFormatter.addQueryLine(0, "       n.automatic");
            queryFormatter.addQueryLine(0, "  FROM n,");
            queryFormatter.addQueryLine(0, "       d");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
				queryFormatter.addQueryLine(0, " WHERE n.geography = d.geography");
			}
			else {
				queryFormatter.addQueryLine(0, " WHERE ((n.geography)::text = (d.geography)::text)");
            }
            addTRif40NumDenom(queryFormatter, connection, user.getUserID());
            addTRif40NumDenom(queryFormatter, connection, "rif40");
    
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) {        
				queryFormatter.addQueryLine(0, " ORDER BY 1, 2, 4;");
			}
        }
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			SQLQueryUtility.rollback(connection);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");

			throw new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage,
                    exception);
		}
        
        return queryFormatter;
    }
    
    /** Add T_RIF40_NUM_DENOM UNION to query for USER and RIF40 schemas if they exists
     *
	 * @param queryFormatter SQLGeneralQueryFormatter
	 * @param connection the connection
	 * @throws RIFServiceException the RIF service exception
	 */ 
    private void addTRif40NumDenom(
        final SQLGeneralQueryFormatter queryFormatter, 
        final Connection connection, 
        final String schemaName) throws Exception {
        
        if (doesTableExist(connection, schemaName, "t_rif40_num_denom")) { 
            queryFormatter.addQueryLine(0, "UNION");
            queryFormatter.addQueryLine(0, "SELECT ta.geography,");
            queryFormatter.addQueryLine(0, "       ta.geography_description,");
            queryFormatter.addQueryLine(0, "       ta.numerator_table,");
            queryFormatter.addQueryLine(0, "       ta.numerator_description,");
            queryFormatter.addQueryLine(0, "       h.theme AS theme_name,");
            queryFormatter.addQueryLine(0, "       h.description AS theme_description,");
            queryFormatter.addQueryLine(0, "       ta.denominator_table,");
            queryFormatter.addQueryLine(0, "       d.description AS denominator_description,");
            queryFormatter.addQueryLine(0, "       0 AS automatic");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "  FROM ( SELECT nd.geography,");
            }
            else {
                queryFormatter.addQueryLine(0, "  FROM ((( SELECT nd.geography,");
            }
            queryFormatter.addQueryLine(0, "                g.description AS geography_description,");
            queryFormatter.addQueryLine(0, "                nd.numerator_table,");
            queryFormatter.addQueryLine(0, "                nd.denominator_table,");
            queryFormatter.addQueryLine(0, "                n.description AS numerator_description,");
            queryFormatter.addQueryLine(0, "                n.theme");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "           FROM " + schemaName + ".t_rif40_num_denom nd");
                queryFormatter.addQueryLine(0, "                LEFT JOIN rif40.rif40_geographies g ON (g.geography = nd.geography)");
                queryFormatter.addQueryLine(0, "                LEFT JOIN rif40.rif40_tables n ON (n.table_name = nd.numerator_table)");
                queryFormatter.addQueryLine(0, "          WHERE [rif40].[rif40_is_object_resolvable](nd.numerator_table)    = 1");
                queryFormatter.addQueryLine(0, "            AND [rif40].[rif40_is_object_resolvable](nd.denominator_table) = 1");
                queryFormatter.addQueryLine(0, "       ) ta");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40.rif40_tables d ON (d.table_name = ta.denominator_table)");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40.rif40_health_study_themes h ON (h.theme = ta.theme)");
            }
            else {
                if (schemaName.equals("rif40")) {
                    queryFormatter.addQueryLine(0, "           FROM " + schemaName + ".t_rif40_num_denom nd");
                }
                else {
                    queryFormatter.addQueryLine(0, "           FROM ((t_rif40_num_denom nd");                  
                }
                queryFormatter.addQueryLine(0, "                LEFT JOIN rif40_geographies g ON (((g.geography)::text = (nd.geography)::text)))");
                queryFormatter.addQueryLine(0, "                LEFT JOIN rif40_tables n ON (((n.table_name)::text = (nd.numerator_table)::text)))");
                queryFormatter.addQueryLine(0, "          WHERE ((rif40_is_object_resolvable(nd.numerator_table) = 1) AND (rif40_is_object_resolvable(nd.denominator_table) = 1))) ta");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40_tables d ON (((d.table_name)::text = (ta.denominator_table)::text)))");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40_health_study_themes h ON (((h.theme)::text = (ta.theme)::text)))"); 
            }
        }            
    }
}
            