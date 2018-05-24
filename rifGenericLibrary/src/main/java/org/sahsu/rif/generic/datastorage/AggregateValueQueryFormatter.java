package org.sahsu.rif.generic.datastorage;

import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.datastorage.ms.MSSQLCountQueryFormatter;

/**
 * A query formatter class that is designed to handle simple MIN, MAX, AVG and SUM
 * operations.  This class may eventually be merged to accommodate COUNT as well,
 * which is currently handled by {@link MSSQLCountQueryFormatter.dataStorageLayer.SQLCountQueryFormatter}.
 */
public final class AggregateValueQueryFormatter extends AbstractSQLQueryFormatter {

 	/**
	 * The Enum OperationType.
	 */
	public enum OperationType {
		/** The min. */
		MIN, 
		/** The max. */
		MAX, 
		/** The avg. */
		AVG,		
		/** The sum. */
		SUM};
	
	/** The from table name. */
	private String fromTableName;
	
	/** The countable field name. */
	private String countableFieldName;
	
	/** The where conditions. */
	private List<String> whereConditions;
	
	/** The operation type. */
	private OperationType operationType;

	/**
	 * Instantiates a new SQL min max query formatter.
	 *
	 * @param operationType the operation type
	 */
	public AggregateValueQueryFormatter(
			final OperationType operationType) {
		
		this.operationType = operationType;
		whereConditions = new ArrayList<>();
	}

	/**
	 * Sets the from table.
	 *
	 * @param fromTableName the new from table
	 */
	public void setFromTable(
		final String fromTableName) {

		this.fromTableName = fromTableName;
	}
	
	/**
	 * Sets the countable field name.
	 *
	 * @param countableFieldName the new countable field name
	 */
	public void setCountableFieldName(
		final String countableFieldName) {
		
		this.countableFieldName = countableFieldName;		
	}
		
	/**
	 * Adds the where parameter.
	 *
	 * @param fieldName the field name
	 */
	public void addWhereParameter(
		final String fieldName) {

		final String whereCondition = fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	/**
	 * Adds the where parameter with operator.
	 *
	 * @param fieldName the field name
	 * @param operator the operator
	 */
	public void addWhereParameterWithOperator(
		final String fieldName,
		final String operator) {

		final String whereCondition = fieldName
		                              + operator
		                              + "?";
		whereConditions.add(whereCondition);
	}

	@Override
	public String generateQuery() {

		resetAccumulatedQueryExpression();
		addQueryPhrase(0, "SELECT");
		padAndFinishLine();
		if (operationType == OperationType.MAX) {
			addQueryPhrase(1, "MAX(");
		}
		else if (operationType == OperationType.MIN) {
			addQueryPhrase(1, "MIN(");			
		}
		else if (operationType == OperationType.AVG) {
			addQueryPhrase(1, "AVG(");			
		}
		else {
			addQueryPhrase(1,"SUM(");			
		}
		addQueryPhrase(countableFieldName);
		addQueryPhrase(") ");
		
		if (operationType == OperationType.MAX) {
			addQueryPhrase(1, "AS maximum");
		}
		else if (operationType == OperationType.MIN) {
			addQueryPhrase(1, "AS minimum");			
		}
		else if (operationType == OperationType.AVG) {
			addQueryPhrase(1, "AS average");			
		}
		else {
			addQueryPhrase(1,"AS total");			
		}
		
		padAndFinishLine();
		
		addQueryPhrase(0, "FROM");
		padAndFinishLine();

		addQueryPhrase(1, convertCase(getSchemaTableName(fromTableName)));
		padAndFinishLine();
		
		int numberOfWhereConditions = whereConditions.size();
		if (numberOfWhereConditions > 0) {			
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			for (int i = 0; i < numberOfWhereConditions; i++) {
				if (i != 0) {
					addQueryPhrase(" AND");
					padAndFinishLine();
				}
				addQueryPhrase(1, convertCase(whereConditions.get(i)));
			}
		}
				
		return super.generateQuery();
	}
}
