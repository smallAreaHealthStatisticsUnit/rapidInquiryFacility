package rifGenericLibrary.dataStorageLayer.pg;

import java.util.ArrayList;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.FunctionCallerQueryFormatter;

/**
 * Convenience class used to help format typical queries which call functions
 */
public final class PGSQLFunctionCallerQueryFormatter extends AbstractSQLQueryFormatter
		implements FunctionCallerQueryFormatter {

	private boolean useDistinct;
	private String functionName;
	private int numberOfFunctionParameters;
	private ArrayList<String> selectFields;
	private ArrayList<String> whereConditions;
	private ArrayList<String> orderByConditions;
	
	public PGSQLFunctionCallerQueryFormatter() {
		useDistinct = false;
		selectFields = new ArrayList<>();
		whereConditions = new ArrayList<>();
		orderByConditions = new ArrayList<>();
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public void setFunctionName(final String functionName) {
		this.functionName = functionName;
	}

	@Override
	public void setNumberOfFunctionParameters(final int numberOfFunctionParameters) {
		this.numberOfFunctionParameters = numberOfFunctionParameters;
	}

	/**
	 * Adds the select field.
	 *
	 * @param selectField the select field
	 */
	@Override
	public void addSelectField(final String selectField) {

		selectFields.add(selectField);		
	}	
	
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	@Override
	public void addWhereParameter(final String fieldName) {

		final String whereCondition = fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	/**
	 * Adds the order by condition.
	 *
	 * @param fieldName the field name
	 */
	@Override
	public void addOrderByCondition(final String fieldName) {
			
		addOrderByCondition(null, fieldName, SortOrder.ASCENDING);
	}	

	/**
	 * Adds the order by condition.
	 *
	 * @param tableName the table name
	 * @param fieldName the field name
	 * @param sortOrder the sort order
	 */
	@Override
	public void addOrderByCondition(final String tableName, final String fieldName,
			final SortOrder sortOrder) {
			
		StringBuilder orderByCondition = new StringBuilder();
			
		if (tableName != null) {
			orderByCondition.append(tableName);
			orderByCondition.append(".");
		}
		orderByCondition.append(fieldName);
		orderByCondition.append(" ");
		orderByCondition.append(sortOrder.sqlForm());
		orderByConditions.add(orderByCondition.toString());
	}
	
	/**
	 * Sets the use distinct.
	 *
	 * @param useDistinct the new use distinct
	 */
	@Override
	public void setUseDistinct(
		final boolean useDistinct) {
		
		this.useDistinct = useDistinct;
	}
	
	/*
	 * @see rifServices.dataStorageLayer.SQLQueryFormatter#generateQuery()
	 */
	@Override
	public String generateQuery() {
		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "SELECT ");
		if (useDistinct) {
			addQueryPhrase("DISTINCT");
		}
		padAndFinishLine();
		
		int numberOfSelectFields = selectFields.size();
		if (numberOfSelectFields == 0) {
			addQueryPhrase(1, "*");
			padAndFinishLine();
		}
		else {
			for (int i = 0; i < numberOfSelectFields; i++) {
				if(i > 0) {
					addQueryPhrase(",");
					finishLine();
				}
				addQueryPhrase(1, convertCase(selectFields.get(i)));
			}
		}
		padAndFinishLine();
		
		addQueryPhrase(0, "FROM");
		padAndFinishLine();
		
		String databaseSchemaName = getDatabaseSchemaName();
		if (databaseSchemaName != null) {
			addQueryPhrase(1, databaseSchemaName);
			addQueryPhrase(".");			
		}
		addQueryPhrase(functionName);
		addQueryPhrase("(");
		
		for (int i = 0; i < numberOfFunctionParameters; i++) {
			if (i != 0) {
				addQueryPhrase(",");
			}
			addQueryPhrase("?");
		}
		addQueryPhrase(")");
		
		int numberOfWhereConditions = whereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			for (int i = 0; i < numberOfWhereConditions; i++) {
				if (i > 0) {
					addQueryPhrase(" AND");
					padAndFinishLine();					
				}
				addQueryPhrase(1, convertCase(whereConditions.get(i)));
			}
		}

		int numberOfOrderByConditions = orderByConditions.size();
		if (numberOfOrderByConditions > 0) {
			padAndFinishLine();
			addQueryPhrase(0, " ORDER BY");
			padAndFinishLine();
			for (int i = 0; i < numberOfOrderByConditions; i++) {
				if (i > 0) {
					addQueryPhrase(",");
				}
				addQueryPhrase(convertCase(orderByConditions.get(i)));
			}
		}
		
		addQueryPhrase(";");
		finishLine();
				
		return super.generateQuery();
	}
}
