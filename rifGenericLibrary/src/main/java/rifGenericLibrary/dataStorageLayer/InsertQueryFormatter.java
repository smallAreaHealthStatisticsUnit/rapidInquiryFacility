package rifGenericLibrary.dataStorageLayer;

import rifGenericLibrary.dataStorageLayer.ms.MSSQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLInsertQueryFormatter;

public interface InsertQueryFormatter extends QueryFormatter {

	public static InsertQueryFormatter getInstance(DatabaseType type) {

		switch (type) {
			case SQL_SERVER:
				return new MSSQLInsertQueryFormatter(true);
			case POSTGRESQL:
				return new PGSQLInsertQueryFormatter();
			case UNKNOWN:
			default:
				throw new IllegalStateException("InsertQueryFormatter.getInstance: unknown "
				                                + "database type");
		}
	}

	/**
	 * Sets the into table.
	 *
	 * @param intoTable the new into table
	 */
	void setIntoTable(
			String intoTable);

	/**
	 * Adds the insert field.
	 *
	 * @param insertField the insert field
	 */
	void addInsertField(
			String insertField);

	void addInsertField(
			String insertField,
			Boolean useQuotes);

	String generateQuery();

	String generateQueryWithLiterals(
			String... literalValues);
}
