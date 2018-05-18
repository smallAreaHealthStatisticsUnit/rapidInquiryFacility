package org.sahsu.rif.generic.datastorage.pg;

import java.util.ArrayList;

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

public final class PGSQLCountQueryFormatter
		extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================
	/**
	 * The Enum SortOrder.
	 */
	public enum SortOrder {
		
	/** The ascending. */
	ASCENDING, 
	/** The descending. */
	DESCENDING};

	
	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The use distinct. */
	private boolean useDistinct;
	
	/** The count field. */
	private String countField;
	
	/** The from tables. */
	private ArrayList<String> fromTables;
	
	/** The where conditions. */
	private ArrayList<String> whereConditions;
	
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL count query formatter.
	 */
	public PGSQLCountQueryFormatter() {

		useDistinct = false;
		fromTables = new ArrayList<String>();
		whereConditions = new ArrayList<String>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Sets the use distinct.
	 *
	 * @param useDistinct the new use distinct
	 */
	public void setUseDistinct(
		final boolean useDistinct) {
		
		this.useDistinct = useDistinct;
	}
	
	/**
	 * Sets the count field.
	 *
	 * @param countField the new count field
	 */
	public void setCountField(
		final String countField) {

		this.countField = countField;
	}
	
	/**
	 * Adds the from table.
	 *
	 * @param fromTable the from table
	 */
	public void addFromTable(
		final String fromTable) {
		
		fromTables.add(fromTable);
	}
	
	/**
	 * Adds the where join condition.
	 *
	 * @param tableA the table a
	 * @param fieldNameA the field name a
	 * @param tableB the table b
	 * @param fieldNameB the field name b
	 */
	public void addWhereJoinCondition(
		final String tableA,
		final String fieldNameA,
		final String tableB,
		final String fieldNameB) {

		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(getSchemaTableName(tableA));
		whereCondition.append(".");
		whereCondition.append(fieldNameA);
		whereCondition.append("=");
		whereCondition.append(getSchemaTableName(tableB));
		whereCondition.append(".");
		whereCondition.append(fieldNameB);
		
		whereConditions.add(whereCondition.toString());
	}
	
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String fieldName) {
		
		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(fieldName);
		whereCondition.append("=?");

		whereConditions.add(whereCondition.toString());
	}
	
	/**
	 * Adds the where parameter with operator.
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	public void addWhereParameterWithOperator(
		final String fieldName,
		final String operator) {

		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(fieldName);
		whereCondition.append(operator);
		whereCondition.append("?");
		
		whereConditions.add(whereCondition.toString());
	}

	/**
	 * Adds the where parameter.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String tableName, 
		final String fieldName) {
		
		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(getSchemaTableName(tableName));
		whereCondition.append(".");		
		whereCondition.append(fieldName);
		whereCondition.append("=?");

		whereConditions.add(whereCondition.toString());
	}

	

	
	@Override
	public String generateQuery() {
		
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "SELECT");
		padAndFinishLine();
		if (useDistinct == true) {
			addQueryPhrase(1, "COUNT( DISTINCT ");
		}
		else {
			addQueryPhrase(1, "COUNT( ");
		}

		addQueryPhrase(countField);
		addQueryPhrase(")");
		padAndFinishLine();
		addQueryPhrase(0, "FROM ");
		int numberOfFromTables = fromTables.size();
		for (int i = 0; i < numberOfFromTables; i++) {
			if (i > 0) {
				addQueryPhrase(",");
			}
			addQueryPhrase(1, convertCase(getSchemaTableName(fromTables.get(i))));
		}
		
		int numberOfWhereConditions = whereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			
			for (int i = 0; i < numberOfWhereConditions; i++) {
				if (i > 0) {
					addQueryPhrase(" AND");
					padAndFinishLine();
				}
				addQueryPhrase(1, convertCase(whereConditions.get(i)));
			}
		}
				
		addQueryPhrase(";");
				
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
