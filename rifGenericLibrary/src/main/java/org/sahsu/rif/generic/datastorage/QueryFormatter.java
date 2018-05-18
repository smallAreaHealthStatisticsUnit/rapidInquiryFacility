package org.sahsu.rif.generic.datastorage;

public interface QueryFormatter {

	/**
	 * The Enum SortOrder.
	 */
	enum SortOrder {
		ASCENDING("ASC"),
		DESCENDING("DESC");

		private String sql;

		SortOrder(String sqlForm) {

			this.sql = sqlForm;
		}

		public String sqlForm() {
			return sql;
		}
	}

	void setDatabaseSchemaName(String databaseSchemaName);

	String getDatabaseSchemaName();

	DatabaseType getDatabaseType();

	void setDatabaseType(
			DatabaseType databaseType);

	boolean isCaseSensitive();

	void setCaseSensitive(
			boolean isCaseSensitive);

	/*
	 * This method is largely stubbed and will be filled out later.  The purpose
	 * is to help convert to lower case or upper case, depending on database-specific
	 * needs.  eg: one vendor uses capital letters, another does not
	 */
	void addCaseSensitivePhrase(
			String queryPhrase);

	void addPaddedQueryPhrase(
			String queryPhrase);

	void addBlankLine();

	void addPaddedQueryLine(
			int indentationLevel,
			String queryPhrase);

	void addQueryPhrase(
			int indentationLevel,
			String queryPhrase);

	void addQueryPhrase(
			String queryPhrase);

	/**
	 * Create query from file
	 *
	 * @param  fileName		Name of file containing query
	 * @param  args 		Argument list.
	 * @param  databaseType DatabaseType
	 *						Replaces %1 with args[0] etc
	 *						NO SUPPORT FOR ESCAPING!
	 */
	void createQueryFromFile(String fileName, String[] args, DatabaseType databaseType)
			throws Exception;

	/**
	 * Create query from file
	 *
	 * @param  fileName		Name of file containing query
	 * @param  args 		Argument list.
	 * @param  databaseType DatabaseType
	 *						Replaces %1 with args[1] etc
	 *						NO SUPPORT FOR ESCAPING!
	 */
	void queryReplaceAll(String from, String to);

	void addQueryLine(
			int indentationLevel,
			String queryPhrase);

	void addUnderline();

	void addComment(
			String lineComment);

	void addCommentLine(
			String lineComment);

	void finishLine(
			String queryPhrase);

	void finishLine();

	void padAndFinishLine();

	String generateQuery();

	void considerWritingSemiColon();

	boolean endWithSemiColon();

	void setEndWithSemiColon(boolean endWithSemiColon);

}
