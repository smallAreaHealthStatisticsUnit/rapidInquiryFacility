package rifGenericLibrary.dataStorageLayer.ms;

import java.util.ArrayList;


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

public final class MSSQLSelectQueryFormatter 
	extends AbstractMSSQLQueryFormatter {

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
	
	/** The or all where conditions. */
	private boolean orAllWhereConditions;
	
	/** The select fields. */
	private ArrayList<String> selectFields;
	
	/** The from tables. */
	private ArrayList<String> fromTables;
	
	/** The where conditions. */
	private ArrayList<String> whereConditions;
	
	/** The order by conditions. */
	private ArrayList<String> orderByConditions;
	
	/** The where like field names. */
	private ArrayList<String> whereLikeFieldNames;
	
	private String ctasTable;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL select query formatter.
	 */
	public MSSQLSelectQueryFormatter() {
		
		useDistinct = false;
		orAllWhereConditions = false;
		selectFields = new ArrayList<String>();
		fromTables = new ArrayList<String>();
		whereConditions = new ArrayList<String>();
		orderByConditions = new ArrayList<String>();
		ctasTable = null;
		whereLikeFieldNames = new ArrayList<String>();
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
	
	
	public void setCTASTable(final String ctasTable) {
		this.ctasTable = ctasTable;
	}
	
	public String getCTASTable() {
		return ctasTable;
	}

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	public void addSelectField(
		final String tableName,
		final String selectField) {

		StringBuilder selectPhrase = new StringBuilder();
		selectPhrase.append(tableName);
		selectPhrase.append(".");
		selectPhrase.append(selectField);
		
		selectFields.add(selectPhrase.toString());		
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
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	public void addSelectFieldWithAlias(
		final String selectField,
		final String aliasName) {

		StringBuilder selectFieldPhrase
			= new StringBuilder();
		selectFieldPhrase.append(selectField);
		selectFieldPhrase.append(" AS ");
		selectFieldPhrase.append(aliasName);
		
		selectFields.add(selectFieldPhrase.toString());		
	}
	
	
	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	public void addSelectFieldWithAlias(
		final String tableName,
		final String selectField,
		final String aliasName) {

		StringBuilder selectFieldPhrase
			= new StringBuilder();
		selectFieldPhrase.append(tableName);
		selectFieldPhrase.append(".");
		selectFieldPhrase.append(selectField);
		selectFieldPhrase.append(" AS ");
		selectFieldPhrase.append(aliasName);
		
		selectFields.add(selectFieldPhrase.toString());		
	}
	
	
	
	/**
	 * adds a constant value in a select statement
	 * eg: 
	 * @param fieldValue
	 * @param aliasName
	 */
	public void addTextLiteralSelectField(
		final String fieldValue,
		final String aliasName) {
		
		StringBuilder textLiteralSelectPhrase = new StringBuilder();
		textLiteralSelectPhrase.append("'");
		textLiteralSelectPhrase.append(fieldValue);		
		textLiteralSelectPhrase.append("' AS ");
		textLiteralSelectPhrase.append(aliasName);
		selectFields.add(textLiteralSelectPhrase.toString());
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
	 * Or all where conditions.
	 *
	 * @param orAllWhereConditions the or all where conditions
	 */
	public void orAllWhereConditions(
		final boolean orAllWhereConditions) {
		
		this.orAllWhereConditions = orAllWhereConditions;
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
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameterWithOperator(
		final String tableA,
		final String tableAField,
		final String operator,
		final String tableB,
		final String tableBField) {
		
		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(getSchemaTableName(tableA));
		whereCondition.append(".");
		whereCondition.append(tableAField);
		whereCondition.append(" ");
		whereCondition.append(operator);
		whereCondition.append(" ");
		whereCondition.append(getSchemaTableName(tableB));
		whereCondition.append(".");
		whereCondition.append(tableBField);
		whereConditions.add(whereCondition.toString());
	}
	
	
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameterWithLiteralValue(
		final String tableName,
		final String fieldName,
		final String literalValue) {
		
		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(getSchemaTableName(tableName));
		whereCondition.append(".");
		whereCondition.append(fieldName);
		whereCondition.append("='");
		whereCondition.append(literalValue);
		whereCondition.append("'");

		whereConditions.add(whereCondition.toString());
	}
		
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameterWithLiteralValue(
		final String fieldName,
		final String literalValue) {
		
		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(fieldName);
		whereCondition.append("='");
		whereCondition.append(literalValue);
		whereCondition.append("'");

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
	 * @param fieldName the field name
	 * @param sortOrder the sort order
	 */
	public void addOrderByCondition(
		final String fieldName,
		final SortOrder sortOrder) {
		
		addOrderByCondition(
			null, 
			fieldName, 
			sortOrder);
	}
	
	/**
	 * Adds the order by condition.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 */
	public void addOrderByCondition(
		final String tableName,
		final String fieldName) {

		addOrderByCondition(
			tableName, 
			fieldName, 
			SortOrder.ASCENDING);		
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
		if (sortOrder == SortOrder.ASCENDING) {
			orderByCondition.append("ASC");
		}
		else {
			orderByCondition.append("DESC");			
		}
		
		orderByConditions.add(orderByCondition.toString());
	}
	
	@Override
	public String generateQuery() {

		resetAccumulatedQueryExpression();
		
		if (ctasTable != null) {
			addQueryPhrase(0, "CREATE TABLE ");
			addQueryPhrase(ctasTable);
			addQueryPhrase(" AS");
			padAndFinishLine();
		}
		addQueryPhrase(0, "SELECT");
		if (useDistinct == true) {
			addQueryPhrase(" DISTINCT");
		}
		padAndFinishLine();

		
		int numberOfSelectFields = selectFields.size();
		for (int i = 0; i < numberOfSelectFields; i++) {
			if(i > 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, convertCase(selectFields.get(i)));
		}
		padAndFinishLine();
	
		addQueryPhrase(0, "FROM");
		padAndFinishLine();
		int numberOfFromTables = fromTables.size();
		for (int i = 0; i < numberOfFromTables; i++) {
			if (i > 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, convertCase(getSchemaTableName(fromTables.get(i))));
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
		
		int numberOfOrderByConditions = orderByConditions.size();
		if (numberOfOrderByConditions > 0) {
			padAndFinishLine();
			addQueryPhrase(0, "ORDER BY");
			padAndFinishLine();
			for (int i = 0; i < numberOfOrderByConditions; i++) {
				if (i > 0) {
					addQueryPhrase(",");
				}
				addQueryPhrase(1, convertCase(orderByConditions.get(i)));
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
