package rifGenericLibrary.dataStorageLayer.ms;

import java.util.ArrayList;

import rifGenericLibrary.dataStorageLayer.InsertQueryFormatter;

public final class MSSQLInsertQueryFormatter extends AbstractMSSQLQueryFormatter
		implements InsertQueryFormatter {

	/** The into table. */
	private String intoTable;
	
	/** The insert fields. */
	private ArrayList<String> insertFields;

	private ArrayList<Boolean> useQuotesForLiteral;

	/**
	 * Instantiates a new SQL insert query formatter.
	 */
	public MSSQLInsertQueryFormatter(final boolean useGoCommand) {
		super(useGoCommand);
		insertFields = new ArrayList<String>();
		useQuotesForLiteral = new ArrayList<Boolean>();		
	}

	@Override
	public void setIntoTable(
			final String intoTable) {
		
		this.intoTable = intoTable;
	}
	
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
