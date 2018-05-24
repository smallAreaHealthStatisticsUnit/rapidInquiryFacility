package org.sahsu.rif.generic.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;

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

public final class MSSQLCreateTableQueryFormatter extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private int textFieldLength;
	
	/** The into table. */
	private String tableToCreate;
	
	/** The insert fields. */
	private ArrayList<String> fieldDeclarations = new ArrayList<>();
	
	private boolean isTemporaryTable;
	private static final String DEFAULT_VAR_CHAR_LENGTH = "200";
	
	// ==========================================
	// Section Construction
	// ==========================================

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * @TODO: although both SQL Server and PostgreSQL can support the concept of
	 * temporary tables, this looks like an area where there may need to be 
	 * different implementations
	 * 
	 * Adds a # to table name for temporary tables in the create table, but the table name with # would have to be used in subsequent SQL calls to the temperary table
	 */
	public void useTemporaryTable() {
		isTemporaryTable = true;
	}
	
	//how is this meant to be used?  Default string length?
	public void setTextFieldLength(final int textFieldLength) {
		this.textFieldLength = textFieldLength;
	}
	
	public void setTableName(final String tableToCreate) {
		this.tableToCreate = tableToCreate;		
	}

	public void addDateFieldDeclaration(
		final String fieldName,
		final boolean isNullAllowed) {
				
		addFieldDeclaration(
			fieldName, 
			"DATE", 
			isNullAllowed);
	}
	
	public void addSmallIntegerFieldDeclaration(
		final String fieldName,
		final boolean isNullAllowed) {
		
		addFieldDeclaration(
			fieldName, 
			"SMALLINT", 
			isNullAllowed);	
	}
	
	
	public void addIntegerFieldDeclaration(
		final String fieldName,
		final boolean isNullAllowed) {
		
		addFieldDeclaration(
			fieldName, 
			"INTEGER", 
			isNullAllowed);	
	}
		
	public void addDoubleFieldDeclaration(
		final String fieldName,
		final boolean isNullAllowed) {
		
		addFieldDeclaration(
			fieldName, 
			"DOUBLE PRECISION", 
			isNullAllowed);
		
	}
	
	
	public void addTextFieldDeclaration(
		final String fieldName,
		final boolean isNullAllowed) {
		
		StringBuilder textFieldType = new StringBuilder();
		
		textFieldType.append("NVARCHAR");

		//textFieldType.append("NVARCHAR(DEFAULT_VAR_CHAR_LENGTH)");
		if (textFieldLength > 0 ) {
			textFieldType.append("("+textFieldLength+")");
		} else {
			textFieldType.append("(" + DEFAULT_VAR_CHAR_LENGTH +")");		
		}
		
		addFieldDeclaration(
			fieldName,
			textFieldType.toString(),
			isNullAllowed);
	}
	
	public void addSequenceField(
		final String fieldName,
		final String sequenceName) {
				
		StringBuilder defaultPhrase = new StringBuilder();
		defaultPhrase.append("DEFAULT (NEXT VALUE FOR ");
		defaultPhrase.append(sequenceName); //assume sequenceName contains schema
		defaultPhrase.append(")");
		
		addFieldDeclaration(
			fieldName,
			"INTEGER",
			defaultPhrase.toString(),
			false);		
	}
	
	public void addCreationTimestampField(
		final String fieldName) {
		
		StringBuilder timeStampFieldType = new StringBuilder();
		timeStampFieldType.append("DATETIME2 (0)");

		addFieldDeclaration(
			fieldName,
			timeStampFieldType.toString(),
			"DEFAULT (sysdatetime())",
			false);
		
	}
	
	public void addTextFieldDeclaration(
		final String fieldName,
		final int length,
		final boolean isNullAllowed) {
			
		StringBuilder textFieldType = new StringBuilder();
		textFieldType.append("NVARCHAR(");
		textFieldType.append(String.valueOf(length));
		textFieldType.append(")");
			
		addFieldDeclaration(
			fieldName,
			textFieldType.toString(),
			isNullAllowed);
	}	

	public void addTextFieldDeclaration(
		final String fieldName,
		final int length,
		final String defaultTextValue,
		final boolean isNullAllowed) {
				
		StringBuilder textFieldType = new StringBuilder();
		textFieldType.append("NVARCHAR(");
		textFieldType.append(String.valueOf(length));
		textFieldType.append(")");
			
		if (defaultTextValue != null) {
			StringBuilder defaultPhrase = new StringBuilder();
			defaultPhrase.append("DEFAULT");
			defaultPhrase.append(" '");
			defaultPhrase.append(defaultTextValue);
			defaultPhrase.append("'");	

			addFieldDeclaration(
				fieldName,
				textFieldType.toString(),
				defaultPhrase.toString(),
				isNullAllowed);			
		}
		else {

			addFieldDeclaration(
				fieldName,
				textFieldType.toString(),
				null,
				isNullAllowed);
		}
	}	
	
	private void addFieldDeclaration(
		final String fieldName,
		final String dataType,
		final String defaultPhrase,
		final boolean isNullAllowed) {
		
		StringBuilder fieldDeclaration = new StringBuilder();
		fieldDeclaration.append(fieldName.toLowerCase());
		fieldDeclaration.append(" ");
		
		if (isCaseSensitive() == false) {
			fieldDeclaration.append(dataType.toUpperCase());
		}
		else {
			fieldDeclaration.append(dataType);		
		}
		
		if (isNullAllowed == false) {
			fieldDeclaration.append(" NOT NULL");
		}
		
		if (defaultPhrase != null) {
			fieldDeclaration.append(" ");
			fieldDeclaration.append(defaultPhrase);
		}
		
		fieldDeclarations.add(fieldDeclaration.toString());
		
	}
	
	private void addFieldDeclaration(
		final String fieldName,
		String dataType,
		final boolean isNullAllowed) {

		addFieldDeclaration(
			fieldName,
			dataType,
			null,
			isNullAllowed);		
	}
	
	@Override
	public String generateQuery() {
		resetAccumulatedQueryExpression();
		
		addQueryPhrase(0, "CREATE TABLE ");
		if ((isTemporaryTable == true) && !(tableToCreate.substring(0,1).equals("#"))) {
			tableToCreate = "#"+tableToCreate; //local temporary tables begin with #, global with ## 
		}
		addQueryPhrase(getSchemaTableName(tableToCreate));

		addQueryPhrase(" (");
		padAndFinishLine();
		
		int numberOfFieldDeclarations = fieldDeclarations.size();
		for (int i = 0; i < numberOfFieldDeclarations; i++) {
			if (i != 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, fieldDeclarations.get(i));			
		}
		addQueryPhrase(")");
		
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
