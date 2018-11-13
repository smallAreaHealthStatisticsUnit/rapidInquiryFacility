package org.sahsu.rif.generic.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;
import org.sahsu.rif.generic.datastorage.InsertQueryFormatter;

public final class MSSQLInsertQueryFormatter extends AbstractSQLQueryFormatter
		implements InsertQueryFormatter {

	/** The into table. */
	private String intoTable;
	
	/** The insert fields. */
	private ArrayList<String> insertFields;

	private ArrayList<Boolean> useQuotesForLiteral;

	private int insertFieldCount;
	
	/**
	 * Instantiates a new SQL insert query formatter.
	 */
	public MSSQLInsertQueryFormatter() {
		insertFields = new ArrayList<>();
		useQuotesForLiteral = new ArrayList<>();
		insertFieldCount=0;
	}
	
	/**
	 * Gets the insert field count
	 */		
	public int getInsertFieldCount() {
		return this.insertFieldCount;
	}
	
	@Override
	public void setIntoTable(
			final String intoTable) {
		
		this.intoTable = intoTable;
	}
	
	@Override
	public void addInsertField(
			final String insertField) {
		
		this.insertFieldCount++;
		insertFields.add(insertField);
	}
	
	@Override
	public void addInsertField(
			final String insertField,
			final Boolean useQuotes) {
				
		this.insertFieldCount++;
		insertFields.add(insertField);
		useQuotesForLiteral.add(useQuotes);
	}

	@Override
	public String generateQuery() {
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "INSERT INTO ");
		addQueryPhrase(getSchemaTableName(intoTable));
		addQueryPhrase("(");
		padAndFinishLine();

		int numberOfInsertFields = insertFields.size();
		for (int i = 0; i < numberOfInsertFields; i++) {
			if (i != 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, insertFields.get(i));			
		}
		addQueryPhrase(")");
		padAndFinishLine();
		addQueryPhrase(0, "VALUES (");
		for (int i = 0; i < numberOfInsertFields; i++) {
			if (i != 0) {
				addQueryPhrase(",");
			}
			addQueryPhrase("?");			
		}

		addQueryPhrase(");");
		finishLine();
		
		return super.generateQuery();		
	}

	@Override
	public String generateQueryWithLiterals(
			final String... literalValues) {
		
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "IF NOT EXISTS(SELECT * FROM ");
		addQueryPhrase(getSchemaTableName(intoTable));
		addQueryPhrase(" WHERE ");
		addQueryPhrase(insertFields.get(0).trim());
		addQueryPhrase(" = '");
		addQueryPhrase(literalValues[0]);
		addQueryPhrase("')");
		finishLine();
		addQueryPhrase(0, "INSERT INTO ");
		addQueryPhrase(getSchemaTableName(intoTable));
		addQueryPhrase("(");
		padAndFinishLine();

		int numberOfInsertFields = insertFields.size();
		for (int i = 0; i < numberOfInsertFields; i++) {
			if (i != 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, insertFields.get(i).trim());			
		}

		addQueryPhrase(")");
		padAndFinishLine();
		addQueryPhrase(0, "VALUES (");
		for (int i = 0; i < numberOfInsertFields; i++) {
			if (i != 0) {
				addQueryPhrase(",");
			}
			
			if (literalValues[i] == null) {
				addQueryPhrase("null");				
			}
			else {
				if (useQuotesForLiteral.get(i)) {
					addQueryPhrase("'");
					addQueryPhrase(literalValues[i]);
					addQueryPhrase("'");					
				}
				else {
					addQueryPhrase(literalValues[i]);
				}
			}
		}

		addQueryPhrase(")");
		return super.generateQuery();
	}
}
