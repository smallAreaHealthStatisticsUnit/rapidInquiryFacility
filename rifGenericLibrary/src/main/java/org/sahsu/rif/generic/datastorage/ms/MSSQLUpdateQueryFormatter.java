package org.sahsu.rif.generic.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;
import org.sahsu.rif.generic.datastorage.UpdateQueryFormatter;

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

public final class MSSQLUpdateQueryFormatter extends AbstractSQLQueryFormatter 
	implements UpdateQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The into table. */
	private String updateTable;
	
	/** The or all where conditions. */
	private boolean orAllWhereConditions;	
	
	/** The insert fields. */
	private ArrayList<String> updateFields;
	private ArrayList<String> updateFieldsCasts;

	/** The where conditions. */
	private ArrayList<String> whereConditions;
	
	/** The where like field names. */
	private ArrayList<String> whereLikeFieldNames;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL insert query formatter.
	 */
	public MSSQLUpdateQueryFormatter() {

		orAllWhereConditions = false;
		updateFields = new ArrayList<String>();	
		updateFieldsCasts = new ArrayList<String>();	
		whereConditions = new ArrayList<String>();
		whereLikeFieldNames = new ArrayList<String>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Or all where conditions.
	 *
	 * @param orAllWhereConditions the or all where conditions
	 */
	public void orAllWhereConditions(
		final boolean orAllWhereConditions) {
		
		this.orAllWhereConditions = orAllWhereConditions;
	}	
	
	/**
	 * Sets the into table.
	 *
	 * @param intoTable the new into table
	 */
	public void setUpdateTable(
		final String updateTable) {
		
		this.updateTable = updateTable;
	}
	
	/**
	 * Adds the insert field.
	 *
	 * @param insertField the insert field
	 */
	public void addUpdateField(
		final String updateField) {
		
		updateFields.add(updateField);
		updateFieldsCasts.add(null);
	}
			
	/**
	 * Adds the insert field.
	 *
	 * @param insertField: the insert field
	 * @param cast: apply cast to the insert field
	 */
	public void addUpdateField(
		final String updateField, final String cast){
		
		updateFields.add(updateField);
		updateFieldsCasts.add(cast);
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
		whereCondition.append(tableB);
		whereCondition.append(".");
		whereCondition.append(fieldNameB);
		
		whereConditions.add(whereCondition.toString());
	}
	
	/**
	 * Adds the where join condition.
	 *
	 * @param tableFieldA the table field a
	 * @param tableFieldB the table field b
	 */
	public void addWhereJoinCondition(
		final String tableFieldA,
		final String tableFieldB) {

		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(tableFieldA);
		whereCondition.append("=");
		whereCondition.append(tableFieldB);
		
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
	 * Adds the where BETWEEN with limits
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	public void addWhereBetweenParameter(
		final String fieldName,
		final String startValue,
		final String endValue) {

		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(fieldName);
		whereCondition.append(" BETWEEN ");
		whereCondition.append(startValue);
		whereCondition.append(" AND ");
		whereCondition.append(endValue);		
		whereConditions.add(whereCondition.toString());
	}
	
	
	/**
	 * Adds the where like field name.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereLikeFieldName(
		final String fieldName) {
		
		whereLikeFieldNames.add(fieldName);
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
		addQueryPhrase(0, "UPDATE ");
		addQueryPhrase(getSchemaTableName(updateTable));
		padAndFinishLine();
		addQueryPhrase(0, "SET ");
		
		int numberOfUpdateFields = updateFields.size();
		for (int i = 0; i < numberOfUpdateFields; i++) {
			if (i != 0) {
				addQueryPhrase(",");
			}
			addQueryPhrase(updateFields.get(i));
			String cast=updateFieldsCasts.get(i);
			if (cast == null) {
				addQueryPhrase("=?");
			}
			else {
				addQueryPhrase("= CAST(? AS " + cast + ")");
			}
		}		
		
		ArrayList<String> allWhereConditions = new ArrayList<String>();
		allWhereConditions.addAll(whereConditions);
		
		//now add in the like conditions
		for (String whereLikeFieldName : whereLikeFieldNames) {
			StringBuilder condition = new StringBuilder();
			condition.append(whereLikeFieldName);
			condition.append(" LIKE ?");
			allWhereConditions.add(condition.toString());
		}
		
		
		int numberOfWhereConditions = allWhereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			
			if (orAllWhereConditions == true) {
				for (int i = 0; i < numberOfWhereConditions; i++) {
					if (i > 0) {
						addQueryPhrase(" OR");
						padAndFinishLine();
					}
					addQueryPhrase(1, convertCase(allWhereConditions.get(i)));
				}			
			}
			else {
				for (int i = 0; i < numberOfWhereConditions; i++) {
					if (i > 0) {
						addQueryPhrase(" AND");
						padAndFinishLine();
					}
					addQueryPhrase(1, convertCase(allWhereConditions.get(i)));
				}				
			}
			
		}

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
