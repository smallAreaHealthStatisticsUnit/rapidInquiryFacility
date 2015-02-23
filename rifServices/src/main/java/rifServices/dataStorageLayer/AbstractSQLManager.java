package rifServices.dataStorageLayer;



import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifServices.system.RIFDatabaseProperties;

import java.sql.SQLException;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * Kevin Garwood
 * @author kgarwood
 * @version
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

abstract class AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseProperties rifDatabaseProperties;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public AbstractSQLManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		this.rifDatabaseProperties = rifDatabaseProperties;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Use appropariate table name case.
	 *
	 * @param tableComponentName the table component name
	 * @return the string
	 */
	protected String useAppropariateTableNameCase(
		final String tableComponentName) {
		
		//TODO: KLG - find out more about when we will need to convert
		//to one case or another
		return tableComponentName;
	}
	
	protected RIFDatabaseProperties getRIFDatabaseProperties() {
		return rifDatabaseProperties;
	}
	
	protected void configureQueryFormatterForDB(
		final AbstractSQLQueryFormatter queryFormatter) {
		
		queryFormatter.setDatabaseType(
			rifDatabaseProperties.getDatabaseType());
		queryFormatter.setCaseSensitive(
			rifDatabaseProperties.isCaseSensitive());
		
	}
	
	/**
	 * Use appropriate field name case.
	 *
	 * @param fieldName the field name
	 * @return the string
	 */
	/*
	protected String useAppropriateFieldNameCase(
		final String fieldName) {

		return fieldName.toLowerCase();
	}
	*/
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {
		
		StringBuilder queryLog = new StringBuilder();
		queryLog.append("==========================================================\n");
		queryLog.append("QUERY NAME:");
		queryLog.append(queryName);
		queryLog.append("\n");
		
		queryLog.append("PARAMETERS:");
		queryLog.append("\n");
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"\n");			
		}
		queryLog.append("\n");
		queryLog.append("SQL QUERY TEXT\n");
		queryLog.append(queryFormatter.generateQuery());
		queryLog.append("\n");
		queryLog.append("==========================================================\n");
		
		System.out.println(queryLog.toString());		
	}
	
	protected void logSQLException(final SQLException sqlException) {
		sqlException.printStackTrace();
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
