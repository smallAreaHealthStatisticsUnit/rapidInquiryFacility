package org.sahsu.rif.generic.datastorage;

import org.sahsu.rif.generic.datastorage.ms.MSSQLRecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLRecordExistsQueryFormatter;

public interface RecordExistsQueryFormatter extends QueryFormatter {

	static RecordExistsQueryFormatter getInstance(final DatabaseType type) {

		switch (type) {
			case POSTGRESQL:
				return new PGSQLRecordExistsQueryFormatter();
			case SQL_SERVER:
				return new MSSQLRecordExistsQueryFormatter();
			case UNKNOWN:
			default:
				throw new IllegalStateException(
						"RecordExistsQueryFormatter.getInstance: unknown database type");
		}
	}


	/**
	 * Sets the from table.
	 *
	 * @param fromTableName the new from table
	 */
	void setFromTable(
			String fromTableName);

	/**
	 * Sets the lookup key field name.
	 *
	 * @param lookupKeyFieldName the new lookup key field name
	 */
	void setLookupKeyFieldName(
			String lookupKeyFieldName);

	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	void addWhereParameter(
			String fieldName);

	/**
	 * Adds the where parameter with operator.
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	void addWhereParameterWithOperator(
			String fieldName,
			String operator);

	@Override
	String generateQuery();
}
