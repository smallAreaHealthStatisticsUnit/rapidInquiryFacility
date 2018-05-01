package rifGenericLibrary.dataStorageLayer.pg;


import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.InsertQueryFormatter;

import java.util.ArrayList;

public final class PGSQLInsertQueryFormatter extends AbstractSQLQueryFormatter
		implements InsertQueryFormatter {

	/** The into table. */
	private String intoTable;
	
	/** The insert fields. */
	private ArrayList<String> insertFields;
	
	private ArrayList<Boolean> useQuotesForLiteral;

	/**
	 * Instantiates a new SQL insert query formatter.
	 */
	public PGSQLInsertQueryFormatter() {
		insertFields = new ArrayList<>();
		useQuotesForLiteral = new ArrayList<>();
	}

	/**
	 * Sets the into table.
	 *
	 * @param intoTable the new into table
	 */
	public void setIntoTable(
		final String intoTable) {
		
		this.intoTable = intoTable;
	}
	
	/**
	 * Adds the insert field.
	 *
	 * @param insertField the insert field
	 */
	@Override
	public void addInsertField(
		final String insertField) {
		
		insertFields.add(insertField);
	}

	@Override
	public void addInsertField(
		final String insertField,
		final Boolean useQuotes) {
			
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
		
		addQueryPhrase(0, "SELECT ");
		for (int i = 0; i < numberOfInsertFields; i++) {
			if (i != 0) {
				addQueryPhrase(", ");
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
			addQueryPhrase(" AS ");
			addQueryPhrase(insertFields.get(i).trim());			
		}
		finishLine();
		addQueryPhrase(0, "FROM ");
		addQueryPhrase(getSchemaTableName(intoTable));
		finishLine();
		addQueryPhrase("WHERE ");
		addQueryPhrase(insertFields.get(0).trim());
		addQueryPhrase(" NOT IN (SELECT ");
		addQueryPhrase(insertFields.get(0).trim());
		addQueryPhrase(" FROM ");
		addQueryPhrase(getSchemaTableName(intoTable));
		addQueryPhrase(")");
	
		return super.generateQuery();		
	}
}
