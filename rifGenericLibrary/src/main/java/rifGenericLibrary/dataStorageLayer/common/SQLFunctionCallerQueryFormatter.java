package rifGenericLibrary.dataStorageLayer.common;

import java.util.ArrayList;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLSelectQueryFormatter.SortOrder;


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

public final class SQLFunctionCallerQueryFormatter 
	extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	
	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The use distinct. */
	private boolean useDistinct;
	
	private String functionName;
	private int numberOfFunctionParameters;

	/** The select fields. */
	private ArrayList<String> selectFields;
	private ArrayList<String> whereConditions;

	/** The order by conditions. */
	private ArrayList<String> orderByConditions;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL select query formatter.
	 */
	public SQLFunctionCallerQueryFormatter() {
		useDistinct = false;
		selectFields = new ArrayList<String>();
		whereConditions = new ArrayList<String>();
		orderByConditions = new ArrayList<String>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getFunctionName() {
		return functionName;
	}
	
	public void setFunctionName(final String functionName) {
		this.functionName = functionName;
	}
	
	public int getNumberOfFunctionParameters() {
		return numberOfFunctionParameters;
	}
	
	public void setNumberOfFunctionParameters(final int numberOfFunctionParameters) {
		this.numberOfFunctionParameters = numberOfFunctionParameters;
	}

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	public void addSelectField(
		final String selectField) {

		selectFields.add(selectField);		
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
	 * Adds the order by condition.
	 *
	 * @param fieldName the field name
	 */
	public void addOrderByCondition(
		final String fieldName) {
			
		addOrderByCondition(null, fieldName, SortOrder.ASCENDING);
	}	

	/**
	 * Adds the order by condition.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 * @param sortOrder the sort order
	 */
	public void addOrderByCondition(
		final String tableName,
		final String fieldName,
		final SortOrder sortOrder) {
			
		StringBuilder orderByCondition = new StringBuilder();
			
		if (tableName != null) {
			orderByCondition.append(tableName);
			orderByCondition.append(".");
		}
		orderByCondition.append(fieldName);
		orderByCondition.append(" ");
		orderByCondition.append(sortOrder.sqlForm());
		orderByConditions.add(orderByCondition.toString());
	}
	
	/**
	 * Sets the use distinct.
	 *
	 * @param useDistinct the new use distinct
	 */
	public void setUseDistinct(
		final boolean useDistinct) {
		
		this.useDistinct = useDistinct;
	}
	
	/*
	 * @see rifServices.dataStorageLayer.SQLQueryFormatter#generateQuery()
	 */
	public String generateQuery() {
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "SELECT ");
		if (useDistinct == true) {
			addQueryPhrase("DISTINCT");
		}
		padAndFinishLine();
		
		int numberOfSelectFields = selectFields.size();
		if (numberOfSelectFields == 0) {
			addQueryPhrase(1, "*");
			padAndFinishLine();
		}
		else {
			for (int i = 0; i < numberOfSelectFields; i++) {
				if(i > 0) {
					addQueryPhrase(",");
					finishLine();
				}
				addQueryPhrase(1, convertCase(selectFields.get(i)));
			}
		}
		padAndFinishLine();
		
		addQueryPhrase(0, "FROM");
		padAndFinishLine();
		
		String databaseSchemaName = getDatabaseSchemaName();
		if (databaseSchemaName != null) {
			addQueryPhrase(1, databaseSchemaName);
			addQueryPhrase(".");			
		}
		addQueryPhrase(functionName);
		addQueryPhrase("(");
		
		for (int i = 0; i < numberOfFunctionParameters; i++) {
			if (i != 0) {
				addQueryPhrase(",");
			}
			addQueryPhrase("?");
		}
		addQueryPhrase(")");
		
		int numberOfWhereConditions = whereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			for (int i = 0; i < numberOfWhereConditions; i++) {
				if (i > 0) {
					addQueryPhrase(" AND");
					padAndFinishLine();					
				}
				addQueryPhrase(1, convertCase(whereConditions.get(i)));
			}
		}

		int numberOfOrderByConditions = orderByConditions.size();
		if (numberOfOrderByConditions > 0) {
			padAndFinishLine();
			addQueryPhrase(0, " ORDER BY");
			padAndFinishLine();
			for (int i = 0; i < numberOfOrderByConditions; i++) {
				if (i > 0) {
					addQueryPhrase(",");
				}
				addQueryPhrase(convertCase(orderByConditions.get(i)));
			}
		}
		
		addQueryPhrase(";");
		finishLine();
				
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
