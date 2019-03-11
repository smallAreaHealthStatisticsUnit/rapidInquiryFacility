package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.system.RIFServiceMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.ResultSetMetaData;

public class Rif40NumDenomService extends BaseSQLManager {
	/**
	 * Instantiates a new SQLRIF context manager.
	 */
	public Rif40NumDenomService(final RIFServiceStartupOptions options) {

		super(options);
		if (rifDatabaseProperties == null) {
			rifDatabaseProperties = options.getRIFDatabaseProperties();
		}
	}    
    
	/**
	 * Gets RIF40_NUM_DENOM as a JSONArray
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return RIF40_NUM_DENOM as a JSONArray
	 * @throws RIFServiceException the RIF service exception
	 */    
    public JSONArray get(
			final Connection connection,
			final User user) 
			throws RIFServiceException {             
        JSONArray result=new JSONArray();
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		String sqlQueryText = null;  
        
 		try {
            SQLGeneralQueryFormatter queryFormatter = formatRif40NumDenomQuery(connection, user);
 
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
 
	/**
	 * Format SQL query (replacement for rif40_num_denom)
     *
 	 * WITH n AS (
	 *     SELECT n1.geography,
	 *            n1.geography_desciption,
	 *            n1.numerator_table,
	 *            n1.numerator_description,
	 *            n1.automatic,
	 *            n1.theme_name,
	 *            n1.theme_description
	 *       FROM ( SELECT g.geography,
	 *                     g.description AS geography_desciption,
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
	 * SELECT n.geography AS geography_name,
	 *        n.geography_desciption,
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
	 * SELECT ta.geography AS geography_name,
	 *        ta.geography_desciption,
	 *        ta.numerator_table,
	 *        ta.numerator_description,
	 *        h.theme AS theme_name,
	 *        h.description AS theme_description,
	 *        ta.denominator_table,
	 *        d.description AS denominator_description,
	 *        0 AS automatic
	 *   FROM ( SELECT nd.geography,
	 *                 g.description AS geography_desciption,
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
            queryFormatter.addQueryLine(0, "           n1.geography_desciption,");
            queryFormatter.addQueryLine(0, "           n1.numerator_table,");
            queryFormatter.addQueryLine(0, "           n1.numerator_description,");
            queryFormatter.addQueryLine(0, "           n1.automatic,");
            queryFormatter.addQueryLine(0, "           n1.theme_name,");
            queryFormatter.addQueryLine(0, "           n1.theme_description");
            queryFormatter.addQueryLine(0, "      FROM ( SELECT g.geography,");
            queryFormatter.addQueryLine(0, "                    g.description AS geography_desciption,");
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
            queryFormatter.addQueryLine(0, "              WHERE n.isnumerator = 1 AND");
            queryFormatter.addQueryLine(0, "                    n.automatic   = 1 AND");
            queryFormatter.addQueryLine(0, "                    n.theme       = t.theme AND");
            
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "                    rif40.rif40_is_object_resolvable(n.table_name) = 1) n1");            queryFormatter.addQueryLine(0, "     WHERE rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1");
                queryFormatter.addQueryLine(0, "     WHERE rif40.rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1");
            }
            else {
                queryFormatter.addQueryLine(0, "                    rif40_is_object_resolvable(n.table_name) = 1) n1");
                queryFormatter.addQueryLine(0, "     WHERE rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1");

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
            queryFormatter.addQueryLine(0, "              WHERE d.isindirectdenominator = 1 AND");
            queryFormatter.addQueryLine(0, "                    d.automatic             = 1 AND");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "                    rif40.rif40_is_object_resolvable(d.table_name) = 1) d1");
                queryFormatter.addQueryLine(0, "     WHERE rif40.rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1 AND");
                queryFormatter.addQueryLine(0, "           rif40.rif40_auto_indirect_checks(d1.denominator_table) IS NULL");
            }
            else {
                queryFormatter.addQueryLine(0, "                    rif40_is_object_resolvable(d.table_name) = 1) d1");
                queryFormatter.addQueryLine(0, "     WHERE rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1 AND");
                queryFormatter.addQueryLine(0, "           rif40_auto_indirect_checks(d1.denominator_table) IS NULL");
            }
            queryFormatter.addQueryLine(0, ")");
            queryFormatter.addQueryLine(0, "SELECT n.geography AS geography_name,");
            queryFormatter.addQueryLine(0, "       n.geography_desciption,");
            queryFormatter.addQueryLine(0, "       n.numerator_table,");
            queryFormatter.addQueryLine(0, "       n.numerator_description,");
            queryFormatter.addQueryLine(0, "       n.theme_name,");
            queryFormatter.addQueryLine(0, "       n.theme_description,");
            queryFormatter.addQueryLine(0, "       d.denominator_table,");
            queryFormatter.addQueryLine(0, "       d.denominator_description,");
            queryFormatter.addQueryLine(0, "       n.automatic");
            queryFormatter.addQueryLine(0, "  FROM n, d");
            queryFormatter.addQueryLine(0, " WHERE n.geography = d.geography");
            
            addTRif40NumDenom(queryFormatter, connection, user.getUserID());
            addTRif40NumDenom(queryFormatter, connection, "rif40");
            
            queryFormatter.addQueryLine(0, " ORDER BY 1, 2, 4;");
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
    
    /** Add T_RIF40_NUM_DENOM UNION to query
     *
	 * @param queryFormatter SQLGeneralQueryFormatter
	 * @param connection the connection
	 * @param user the user
	 * @return SQLGeneralQueryFormatter object
	 * @throws RIFServiceException the RIF service exception
	 */ 
    private void addTRif40NumDenom(
        final SQLGeneralQueryFormatter queryFormatter, 
        final Connection connection, 
        final String schemaName) throws Exception {
        
        if (doesTableExist(connection, schemaName, "t_rif40_num_denom")) { 
            queryFormatter.addQueryLine(0, "UNION");
            queryFormatter.addQueryLine(0, "SELECT ta.geography AS geography_name,");
            queryFormatter.addQueryLine(0, "       ta.geography_desciption,");
            queryFormatter.addQueryLine(0, "       ta.numerator_table,");
            queryFormatter.addQueryLine(0, "       ta.numerator_description,");
            queryFormatter.addQueryLine(0, "       h.theme AS theme_name,");
            queryFormatter.addQueryLine(0, "       h.description AS theme_description,");
            queryFormatter.addQueryLine(0, "       ta.denominator_table,");
            queryFormatter.addQueryLine(0, "       d.description AS denominator_description,");
            queryFormatter.addQueryLine(0, "       0 AS automatic");
            queryFormatter.addQueryLine(0, "  FROM ( SELECT nd.geography,");
            queryFormatter.addQueryLine(0, "                g.description AS geography_desciption,");
            queryFormatter.addQueryLine(0, "                nd.numerator_table,");
            queryFormatter.addQueryLine(0, "                nd.denominator_table,");
            queryFormatter.addQueryLine(0, "                n.description AS numerator_description,");
            queryFormatter.addQueryLine(0, "                n.theme");
            queryFormatter.addQueryLine(0, "           FROM " + schemaName + ".t_rif40_num_denom nd");
            if (rifDatabaseProperties.getDatabaseType() == DatabaseType.SQL_SERVER) {
                queryFormatter.addQueryLine(0, "                LEFT OUTER JOIN rif40.rif40_geographies g ON (g.geography = nd.geography)");
                queryFormatter.addQueryLine(0, "                LEFT OUTER JOIN rif40.rif40_tables n ON (n.table_name = nd.numerator_table)");
                queryFormatter.addQueryLine(0, "          WHERE rrif40.if40_is_object_resolvable(nd.numerator_table) = 1 AND");
                queryFormatter.addQueryLine(0, "                rif40.rif40_is_object_resolvable(nd.denominator_table) = 1");
                queryFormatter.addQueryLine(0, "       ) ta");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40.rif40_tables d              ON d.table_name = ta.denominator_table");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40.rif40_health_study_themes h ON h.theme      = ta.theme");
            }
            else {
                queryFormatter.addQueryLine(0, "                LEFT OUTER JOIN rif40_geographies g ON (g.geography = nd.geography)");
                queryFormatter.addQueryLine(0, "                LEFT OUTER JOIN rif40_tables n ON (n.table_name = nd.numerator_table)");
                queryFormatter.addQueryLine(0, "          WHERE rif40_is_object_resolvable(nd.numerator_table) = 1 AND");
                queryFormatter.addQueryLine(0, "                rif40_is_object_resolvable(nd.denominator_table) = 1");
                queryFormatter.addQueryLine(0, "       ) ta");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40_tables d              ON d.table_name = ta.denominator_table");
                queryFormatter.addQueryLine(0, "       LEFT JOIN rif40_health_study_themes h ON h.theme      = ta.theme");
            }
        }            
    }
}