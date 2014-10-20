package rifServices.dataStorageLayer;



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
 * Copyright 2014 Imperial College London, developed by the Small Area
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
	
	/**
	 * The Enum AlphabeticalCase.
	 */
	public enum AlphabeticalCase {
		/** The lower case. */
		LOWER_CASE, 
		/** The upper case. */
		UPPER_CASE, 
		/** The mixed case. */
		MIXED_CASE};
	
	/** The alphabetical case. */
	protected AlphabeticalCase alphabeticalCase;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL query formatter.
	 */
	public AbstractSQLQueryFormatter() {

		alphabeticalCase = AlphabeticalCase.MIXED_CASE;
		query = new StringBuilder();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Convert case.
	 *
	 * @param sqlPhrase the sql phrase
	 * @return the string
	 */
	protected String convertCase(
		final String sqlPhrase) {

		if (alphabeticalCase == AlphabeticalCase.LOWER_CASE) {
			return sqlPhrase.toLowerCase();				
		}
		else if (alphabeticalCase == AlphabeticalCase.UPPER_CASE) {
			return sqlPhrase.toUpperCase();				
		}
		else {
			return sqlPhrase;								
		}		
	}
	
	/**
	 * Sets the alphabetical case type.
	 *
	 * @param alphabeticalCase the new alphabetical case type
	 */
	public void setAlphabeticalCaseType(
		final AlphabeticalCase alphabeticalCase) {
		
		this.alphabeticalCase = alphabeticalCase;
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
		return query.toString();
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
