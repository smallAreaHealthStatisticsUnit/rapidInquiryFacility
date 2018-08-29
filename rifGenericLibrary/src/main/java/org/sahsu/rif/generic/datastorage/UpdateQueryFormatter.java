package org.sahsu.rif.generic.datastorage;

import org.sahsu.rif.generic.datastorage.ms.MSSQLUpdateQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLUpdateQueryFormatter;

public interface UpdateQueryFormatter extends QueryFormatter {

	static UpdateQueryFormatter getInstance(final DatabaseType type) {

		switch (type) {
			case POSTGRESQL:
				return new PGSQLUpdateQueryFormatter();
			case SQL_SERVER:
				return new MSSQLUpdateQueryFormatter();
			case UNKNOWN:
			default:
				throw new IllegalStateException("UpdateQueryFormatter.getInstance: unknown "
				                                + "database type");
		}
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
		final boolean orAllWhereConditions);
	
	/**
	 * Sets the into table.
	 *
	 * @param intoTable the new into table
	 */
	public void setUpdateTable(
		final String updateTable);
	
	/**
	 * Adds the insert field.
	 *
	 * @param insertField the insert field
	 */
	public void addUpdateField(
		final String updateField);
		
	/**
	 * Adds the insert field.
	 *
	 * @param insertField: the insert field
	 * @param cast: apply cast to the insert field
	 */
	public void addUpdateField(
		final String updateField, final String cast);
	
	
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
		final String fieldNameB);
	
	/**
	 * Adds the where join condition.
	 *
	 * @param tableFieldA the table field a
	 * @param tableFieldB the table field b
	 */
	public void addWhereJoinCondition(
		final String tableFieldA,
		final String tableFieldB);
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String fieldName);
	
	/**
	 * Adds the where parameter with operator.
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	public void addWhereParameterWithOperator(
		final String fieldName,
		final String operator);

	/**
	 * Adds the where BETWEEN with limits
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	public void addWhereBetweenParameter(
		final String fieldName,
		final String startValue,
		final String endValue);
	
	
	/**
	 * Adds the where like field name.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereLikeFieldName(
		final String fieldName);
	
	/**
	 * Adds the where parameter.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String tableName, 
		final String fieldName);

	public String generateQuery();
}
