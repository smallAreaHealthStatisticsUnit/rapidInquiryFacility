package org.sahsu.rif.generic.datastorage.pg;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;

/**
 * Convenience class used to help format typical SELECT FROM WHERE clauses.
 * We don't expect all SQL queries to follow the basic SELECT statement but
 * the utility class is meant to help format the text and alignment of SQL
 * queries, and to reduce the risk of having syntax problems occur.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class PGSQLFieldVarianceQueryFormatter
		extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
		
	/** The count field. */
	private String fieldOfInterest;
	
	/** The from tables. */
	private String fromTable;
			
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL count query formatter.
	 */
	public PGSQLFieldVarianceQueryFormatter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	/**
	 * Sets the count field.
	 *
	 * @param countField the new count field
	 */
	public void setFieldOfInterest(
		final String fieldOfInterest) {

		this.fieldOfInterest = fieldOfInterest;
	}
	
	/**
	 * Adds the from table.
	 *
	 * @param fromTable the from table
	 */
	
	public void setFromTable(
		final String fromTable) {
		
		this.fromTable = fromTable;
	}
	
	
	@Override
	public String generateQuery() {
		
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "SELECT");
		padAndFinishLine();
		addQueryPhrase(1, fieldOfInterest);
		addQueryPhrase(" AS value,");
		padAndFinishLine();
		addQueryPhrase(1, "COUNT(");
		addQueryPhrase(fieldOfInterest);
		addQueryPhrase(") AS frequency");
		padAndFinishLine();

		addQueryPhrase(0, "FROM ");
		padAndFinishLine();
		addQueryPhrase(1, getSchemaTableName(fromTable));		
		padAndFinishLine();
		addQueryPhrase(0, "GROUP BY");
		padAndFinishLine();
		addQueryPhrase(1, "value");
		padAndFinishLine();
		addQueryPhrase(0, "ORDER BY");
		padAndFinishLine();
		addQueryPhrase(1, "COUNT(");
		addQueryPhrase(fieldOfInterest);
		addQueryPhrase(") DESC;");
				
		return super.generateQuery();
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
