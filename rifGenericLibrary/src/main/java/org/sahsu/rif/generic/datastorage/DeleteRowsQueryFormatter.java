package org.sahsu.rif.generic.datastorage;

import java.util.ArrayList;

public final class DeleteRowsQueryFormatter extends AbstractSQLQueryFormatter {

	/** The from table. */
	private String fromTable;
	
	/** The where conditions. */
	private ArrayList<String> whereConditions = new ArrayList<>();

	public void setFromTable(
			final String fromTable) {
		
		this.fromTable = fromTable;
	}

	public void addWhereParameter(
			final String fieldName) {

		final String whereCondition = fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	public void addWhereParameterWithOperator(
			final String fieldName,
			final String operator) {

		final String whereCondition = fieldName
		                              + operator
		                              + "?";
		whereConditions.add(whereCondition);
	}
	
	public String generateQuery() {
	
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "DELETE FROM ");
		addQueryPhrase(getSchemaTableName(fromTable));

		
		int numberOfWhereConditions = whereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase("WHERE");
			padAndFinishLine();			
			for (int i = 0; i < numberOfWhereConditions; i++) {
				if (i > 0) {
					addQueryPhrase(" AND");
					padAndFinishLine();
				}
				addQueryPhrase(1, convertCase(whereConditions.get(i)));
			}
		}
		
		return super.generateQuery();		
	}

	public void addWhereParameterWithLiteralValue(
			final String fieldName,
			final String literalValue) {

		final String whereCondition = fieldName
		                              + "='"
		                              + literalValue
		                              + "'";
		whereConditions.add(whereCondition);
	}
}
