package org.sahsu.rif.generic.datastorage.pg;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;

/**
 * Convenience class used to help format typical SELECT FROM WHERE clauses.
 * We don't expect all SQL queries to follow the basic SELECT statement but
 * the utility class is meant to help format the text and alignment of SQL
 * queries, and to reduce the risk of having syntax problems occur.
 */

public final class PGSQLSelectQueryFormatter extends AbstractSQLQueryFormatter
		implements SelectQueryFormatter {

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
	
	/**
	 * Instantiates a new SQL select query formatter.
	 */
	public PGSQLSelectQueryFormatter() {
		
		useDistinct = false;
		orAllWhereConditions = false;
		selectFields = new ArrayList<String>();
		fromTables = new ArrayList<String>();
		whereConditions = new ArrayList<String>();
		orderByConditions = new ArrayList<String>();
		ctasTable = null;
		whereLikeFieldNames = new ArrayList<String>();
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

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	public void addSelectField(
		final String tableName,
		final String selectField) {

		final String selectPhrase = tableName
		                            + "."
		                            + selectField;
		selectFields.add(selectPhrase);
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

		final String selectFieldPhrase = selectField
		                                 + " AS "
		                                 + aliasName;
		selectFields.add(selectFieldPhrase);
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

		final String selectFieldPhrase = tableName
		                                 + "."
		                                 + selectField
		                                 + " AS "
		                                 + aliasName;
		selectFields.add(selectFieldPhrase);
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

		final String textLiteralSelectPhrase = "'"
		                                       + fieldValue
		                                       + "' AS "
		                                       + aliasName;
		selectFields.add(textLiteralSelectPhrase);
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

		final String whereCondition = getSchemaTableName(tableA)
		                              + "."
		                              + fieldNameA
		                              + "="
		                              + getSchemaTableName(tableB)
		                              + "."
		                              + fieldNameB;
		whereConditions.add(whereCondition);
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

		final String whereCondition = tableFieldA
		                              + "="
		                              + tableFieldB;
		whereConditions.add(whereCondition);
	}
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String fieldName) {

		final String whereCondition = fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	
	/**
	 * Adds the where parameter.
	 *
	 */
	public void addWhereParameterWithOperator(
		final String tableA,
		final String tableAField,
		final String operator,
		final String tableB,
		final String tableBField) {

		final String whereCondition = getSchemaTableName(tableA)
		                              + "."
		                              + tableAField
		                              + " "
		                              + operator
		                              + " "
		                              + getSchemaTableName(tableB)
		                              + "."
		                              + tableBField;
		whereConditions.add(whereCondition);
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

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + "='"
		                              + literalValue
		                              + "'";
		whereConditions.add(whereCondition);
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

		final String whereCondition = fieldName
		                              + operator
		                              + "?";
		whereConditions.add(whereCondition);
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

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	/**
	 * Adds the where parameter.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 * @param inValues: Array of String values
	 */	
	public void addWhereIn(
			final String tableName,
			final String fieldName,
			final ArrayList<String> inValues) {

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + " IN ('" + String.join("', '", inValues) + "')";
		whereConditions.add(whereCondition);
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
	@Override
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
		if (useDistinct) {
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

		ArrayList<String> allWhereConditions = new ArrayList<String>(whereConditions);
		
		//now add in the like conditions
		for (String whereLikeFieldName : whereLikeFieldNames) {
			final String condition = whereLikeFieldName
			                         + " LIKE ?";
			allWhereConditions.add(condition);
		}
		
		
		int numberOfWhereConditions = allWhereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			
			if (orAllWhereConditions) {
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

		if (endWithSemiColon()) {
			addQueryPhrase(";");
			finishLine();
			addBlankLine();
		}

				
		return super.generateQuery();
	}
}
