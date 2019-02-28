package org.sahsu.rif.generic.datastorage;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLSelectQueryFormatter;

public interface SelectQueryFormatter extends QueryFormatter {

	static SelectQueryFormatter getInstance(final DatabaseType type) {

		switch (type) {
			case POSTGRESQL:
				return new PGSQLSelectQueryFormatter();
			case SQL_SERVER:
				return new MSSQLSelectQueryFormatter();
			case UNKNOWN:
			default:
				throw new IllegalStateException("SelectQueryFormatter.getInstance: unknown "
				                                + "database type");
		}
	}

	/**
	 * Sets the use distinct.
	 *
	 * @param useDistinct the new use distinct
	 */
	void setUseDistinct(
			boolean useDistinct);

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	void addSelectField(
			String tableName,
			String selectField);

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	void addSelectField(
			String selectField);

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	void addSelectFieldWithAlias(
			String selectField,
			String aliasName);

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	void addSelectFieldWithAlias(
			String tableName,
			String selectField,
			String aliasName);

	/**
	 * adds a constant value in a select statement
	 * eg:
	 * @param fieldValue
	 * @param aliasName
	 */
	void addTextLiteralSelectField(
			String fieldValue,
			String aliasName);

	/**
	 * Adds the from table.
	 *
	 * @param fromTable the from table
	 */
	void addFromTable(
			String fromTable);

	/**
	 * Adds the where join condition.
	 *
	 * @param tableA the table a
	 * @param fieldNameA the field name a
	 * @param tableB the table b
	 * @param fieldNameB the field name b
	 */
	void addWhereJoinCondition(
			String tableA,
			String fieldNameA,
			String tableB,
			String fieldNameB);

	/**
	 * Adds the where join condition.
	 *
	 * @param tableFieldA the table field a
	 * @param tableFieldB the table field b
	 */
	void addWhereJoinCondition(
			String tableFieldA,
			String tableFieldB);

	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	void addWhereParameter(
			String fieldName);

	/**
	 * Adds the where parameter.
	 *
	 */
	void addWhereParameterWithOperator(
			String tableA,
			String tableAField,
			String operator,
			String tableB,
			String tableBField);

	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	void addWhereParameterWithLiteralValue(
			String tableName,
			String fieldName,
			String literalValue);

	/**
	 * Adds the where parameter with operator.
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	void addWhereParameterWithOperator(
			String fieldName,
			String operator);

	/**
	 * Adds the where parameter.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 */
	void addWhereParameter(
			String tableName,
			String fieldName);
			
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
			final ArrayList<String> inValues);
			
	/**
	 * Adds the order by condition.
	 *
	 * @param fieldName the field name
	 */
	void addOrderByCondition(
			String fieldName);

	/**
	 * Adds the order by condition.
	 *
	 * @param fieldName the field name
	 * @param sortOrder the sort order
	 */
	void addOrderByCondition(
			String fieldName,
			SortOrder sortOrder);

	/**
	 * Adds the order by condition.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 */
	void addOrderByCondition(
			String tableName,
			String fieldName);

	/**
	 * Adds the order by condition.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 * @param sortOrder the sort order
	 */
	void addOrderByCondition(
			String tableName,
			String fieldName,
			SortOrder sortOrder);

	@Override
	String generateQuery();

}
