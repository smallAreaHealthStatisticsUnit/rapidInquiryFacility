package rifGenericLibrary.dataStorageLayer;



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

public abstract class AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The query. */
	private StringBuilder query;
	
	private String databaseSchemaName;
	
	private DatabaseType databaseType;
	private boolean isCaseSensitive;
	
	private boolean endWithSemiColon;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL query formatter.
	 */
	public AbstractSQLQueryFormatter() {
		isCaseSensitive = true;
		query = new StringBuilder();
		endWithSemiColon = false;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setDatabaseSchemaName(final String databaseSchemaName) {
		this.databaseSchemaName = databaseSchemaName;
	}
	
	public String getDatabaseSchemaName() {
		return databaseSchemaName;
	}
	
	protected String getSchemaTableName(final String tableName) {
		StringBuilder schemaTableName = new StringBuilder();
		
		if (databaseSchemaName != null) {
			schemaTableName.append(databaseSchemaName);
			schemaTableName.append(".");			
		}
		schemaTableName.append(tableName);
		return schemaTableName.toString();
	}
	
	protected StringBuilder getQueryBuilder() {
		return query;
	}
	
	/**
	 * Convert case.
	 *
	 * @param sqlPhrase the sql phrase
	 * @return the string
	 */
	protected String convertCase(
		final String sqlPhrase) {

		if (isCaseSensitive == false) {
			return sqlPhrase.toUpperCase();				
		}
		else {
			return sqlPhrase;								
		}		
	}
	
	public DatabaseType getDatabaseType() {
		return databaseType;
	}
	
	public void setDatabaseType(
		final DatabaseType databaseType) {
		
		this.databaseType = databaseType;
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	
	public void setCaseSensitive(
		final boolean isCaseSensitive) {

		this.isCaseSensitive = isCaseSensitive;
	}
	
	/*
	 * This method is largely stubbed and will be filled out later.  The purpose
	 * is to help convert to lower case or upper case, depending on database-specific
	 * needs.  eg: one vendor uses capital letters, another does not
	 */
	public void addCaseSensitivePhrase(
		final String queryPhrase) {
		
		query.append(queryPhrase);
	}

	public void addPaddedQueryPhrase(
		final String queryPhrase) {
		
		query.append(queryPhrase);
		query.append(" ");
		query.append("\n");		
	}
	
	public void addBlankLine() {
		query.append("\n\n");
	}

	public void addPaddedQueryLine(
		final int indentationLevel,
		final String queryPhrase) {
			
		addIndentation(indentationLevel);	
		query.append(queryPhrase);
		query.append(" ");
		query.append("\n");		
	}

	public void addQueryPhrase(
		final int indentationLevel,
		final String queryPhrase) {
		
		addIndentation(indentationLevel);	
		query.append(queryPhrase);		
	}
	
	public void addQueryPhrase(
		final String queryPhrase) {
		
		query.append(queryPhrase);
	}
	
	public void addQueryLine(
		final int indentationLevel,
		final String queryPhrase) {
		
		addIndentation(indentationLevel);		
		query.append(queryPhrase);
		query.append("\n");
	
	}

	public void addUnderline() {
		
		query.append(" -- ");
		for (int i = 0; i < 60; i++) {
			query.append("=");
		}
		query.append("\n");
	}
	
	public void addComment(
		final String lineComment) {
		
		query.append(" -- ");
		query.append(lineComment);
	}
	
	public void addCommentLine(
		final String lineComment) {
			
		query.append(" -- ");
		query.append(lineComment);
		query.append("\n");
	}	
	
	private void addIndentation(
		final int indentationLevel) {
		
		for (int i = 0; i < indentationLevel; i++) {
			query.append("   ");
		}
		
		//query.append("\t");
	}
	
	public void finishLine(
		final String queryPhrase) {
		
		query.append(queryPhrase);
		query.append("\n");
	}
	
	public void finishLine() {
		query.append("\n");
	}
	
	public void padAndFinishLine() {
		query.append(" ");
		query.append("\n");
	}
	
	protected void resetAccumulatedQueryExpression() {
		query = new StringBuilder();
	}
	
	
	public String generateQuery() {
		StringBuilder result = new StringBuilder();
		result.append(query.toString());
		
		if (endWithSemiColon) {
			result.append(";");
		}
		result.append("\n");
		return result.toString();
	}
	
	public void considerWritingSemiColon() {
	}
	
	public boolean endWithSemiColon() {
		return endWithSemiColon;
	}
	
	public void setEndWithSemiColon(boolean endWithSemiColon) {
		this.endWithSemiColon = endWithSemiColon;
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
