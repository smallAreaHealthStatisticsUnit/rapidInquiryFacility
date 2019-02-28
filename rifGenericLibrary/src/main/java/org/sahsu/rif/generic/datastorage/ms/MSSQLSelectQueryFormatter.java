package org.sahsu.rif.generic.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;

/**
 * Convenience class used to help format typical SELECT FROM WHERE clauses.
 * We don't expect all SQL queries to follow the basic SELECT statement but
 * the utility class is meant to help format the text and alignment of SQL
 * queries, and to reduce the risk of having syntax problems occur.
 */
public final class MSSQLSelectQueryFormatter extends AbstractSQLQueryFormatter
		implements SelectQueryFormatter {

	/** The use distinct. */
	private boolean useDistinct;
	
	/** The or all where conditions. */
	private boolean orAllWhereConditions;
	
	/** The select fields. */
	private ArrayList<String> selectFields;
	
	/** The from tables. */
	private ArrayList<String> fromTables;
	
	/** The where conditions. */
	private ArrayList<String> whereConditions;
	
	/** The order by conditions. */
	private ArrayList<String> orderByConditions;
	
	/** The where like field names. */
	private ArrayList<String> whereLikeFieldNames;
	
	private String ctasTable;
	
	/**
	 * Instantiates a new SQL select query formatter.
	 */
	public MSSQLSelectQueryFormatter() {

		useDistinct = false;
		orAllWhereConditions = false;
		selectFields = new ArrayList<>();
		fromTables = new ArrayList<>();
		whereConditions = new ArrayList<>();
		orderByConditions = new ArrayList<>();
		ctasTable = null;
		whereLikeFieldNames = new ArrayList<>();
	}

	@Override
	public void setUseDistinct(
			final boolean useDistinct) {
		
		this.useDistinct = useDistinct;
	}

	@Override
	public void addSelectField(
			final String tableName,
			final String selectField) {

		final String selectPhrase = tableName
		                            + "."
		                            + selectField;
		selectFields.add(selectPhrase);
	}	
	
	@Override
	public void addSelectField(
			final String selectField) {

		selectFields.add(selectField);		
	}

	@Override
	public void addSelectFieldWithAlias(
			final String selectField,
			final String aliasName) {

		final String selectFieldPhrase = selectField
		                                 + " AS "
		                                 + aliasName;
		selectFields.add(selectFieldPhrase);
	}
	
	
	@Override
	public void addSelectFieldWithAlias(
			final String tableName,
			final String selectField,
			final String aliasName) {

		final String selectFieldPhrase = tableName
		                                 + "."
		                                 + selectField
		                                 + " AS "
		                                 + aliasName;
		selectFields.add(selectFieldPhrase);
	}
	
	
	
	@Override
	public void addTextLiteralSelectField(
			final String fieldValue,
			final String aliasName) {

		final String textLiteralSelectPhrase = "'"
		                                       + fieldValue
		                                       + "' AS "
		                                       + aliasName;
		selectFields.add(textLiteralSelectPhrase);
	}
		
	@Override
	public void addFromTable(
			final String fromTable) {

		fromTables.add(fromTable);
	}

	@Override
	public void addWhereJoinCondition(
			final String tableA,
			final String fieldNameA,
			final String tableB,
			final String fieldNameB) {

		final String whereCondition = getSchemaTableName(tableA)
		                              + "."
		                              + fieldNameA
		                              + "="
		                              + getSchemaTableName(tableB)
		                              + "."
		                              + fieldNameB;
		whereConditions.add(whereCondition);
	}
	
	@Override
	public void addWhereJoinCondition(
			final String tableFieldA,
			final String tableFieldB) {

		final String whereCondition = tableFieldA
		                              + "="
		                              + tableFieldB;
		whereConditions.add(whereCondition);
	}
	
	@Override
	public void addWhereParameter(
			final String fieldName) {

		final String whereCondition = fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}
	
	
	@Override
	public void addWhereParameterWithOperator(
			final String tableA,
			final String tableAField,
			final String operator,
			final String tableB,
			final String tableBField) {

		final String whereCondition = getSchemaTableName(tableA)
		                              + "."
		                              + tableAField
		                              + " "
		                              + operator
		                              + " "
		                              + getSchemaTableName(tableB)
		                              + "."
		                              + tableBField;
		whereConditions.add(whereCondition);
	}
	
	
	
	@Override
	public void addWhereParameterWithLiteralValue(
			final String tableName,
			final String fieldName,
			final String literalValue) {

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + "='"
		                              + literalValue
		                              + "'";
		whereConditions.add(whereCondition);
	}

	@Override
	public void addWhereParameterWithOperator(
			final String fieldName,
			final String operator) {

		StringBuilder whereCondition = new StringBuilder();
		whereCondition.append(fieldName);
		whereCondition.append(operator);
		whereCondition.append("?");
		
		whereConditions.add(whereCondition.toString());
	}

	@Override
	public void addWhereParameter(
			final String tableName,
			final String fieldName) {

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + "=?";
		whereConditions.add(whereCondition);
	}


	@Override
	public void addWhereIn(
			final String tableName,
			final String fieldName,
			final ArrayList<String> inValues) {

		final String whereCondition = getSchemaTableName(tableName)
		                              + "."
		                              + fieldName
		                              + " IN ('" + String.join("', '", inValues) + "')";
		whereConditions.add(whereCondition);
	}
	
	@Override
	public void addOrderByCondition(
			final String fieldName) {
			
		addOrderByCondition(null, fieldName, SortOrder.ASCENDING);
	}

	@Override
	public void addOrderByCondition(
			final String fieldName,
			final SortOrder sortOrder) {
		
		addOrderByCondition(
			null, 
			fieldName, 
			sortOrder);
	}
	
	@Override
	public void addOrderByCondition(
			final String tableName,
			final String fieldName) {

		addOrderByCondition(
			tableName, 
			fieldName, 
			SortOrder.ASCENDING);		
	}
	
	@Override
	public void addOrderByCondition(
			final String tableName,
			final String fieldName,
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
	
	@Override
	public String generateQuery() {

		resetAccumulatedQueryExpression();
		
		if (ctasTable != null) {
			addQueryPhrase(0, "CREATE TABLE ");
			addQueryPhrase(ctasTable);
			addQueryPhrase(" AS");
			padAndFinishLine();
		}
		addQueryPhrase(0, "SELECT");
		if (useDistinct) {
			addQueryPhrase(" DISTINCT");
		}
		padAndFinishLine();

		
		int numberOfSelectFields = selectFields.size();
		for (int i = 0; i < numberOfSelectFields; i++) {
			if(i > 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, convertCase(selectFields.get(i)));
		}
		padAndFinishLine();
	
		addQueryPhrase(0, "FROM");
		padAndFinishLine();
		int numberOfFromTables = fromTables.size();
		for (int i = 0; i < numberOfFromTables; i++) {
			if (i > 0) {
				addQueryPhrase(",");
				finishLine();
			}
			addQueryPhrase(1, convertCase(getSchemaTableName(fromTables.get(i))));
		}

		ArrayList<String> allWhereConditions = new ArrayList<>(whereConditions);
		
		//now add in the like conditions
		for (String whereLikeFieldName : whereLikeFieldNames) {
			final String condition = whereLikeFieldName
			                         + " LIKE ?";
			allWhereConditions.add(condition);
		}
		
		
		int numberOfWhereConditions = allWhereConditions.size();
		if (numberOfWhereConditions > 0) {			
			padAndFinishLine();
			addQueryPhrase(0, "WHERE");
			padAndFinishLine();
			
			if (orAllWhereConditions) {
				for (int i = 0; i < numberOfWhereConditions; i++) {
					if (i > 0) {
						addQueryPhrase(" OR");
						padAndFinishLine();
					}
					addQueryPhrase(1, convertCase(allWhereConditions.get(i)));
				}			
			}
			else {
				for (int i = 0; i < numberOfWhereConditions; i++) {
					if (i > 0) {
						addQueryPhrase(" AND");
						padAndFinishLine();
					}
					addQueryPhrase(1, convertCase(allWhereConditions.get(i)));
				}				
			}
			
		}
		
		int numberOfOrderByConditions = orderByConditions.size();
		if (numberOfOrderByConditions > 0) {
			padAndFinishLine();
			addQueryPhrase(0, "ORDER BY");
			padAndFinishLine();
			for (int i = 0; i < numberOfOrderByConditions; i++) {
				if (i > 0) {
					addQueryPhrase(",");
				}
				addQueryPhrase(1, convertCase(orderByConditions.get(i)));
			}
		}
				
		return super.generateQuery();
	}
}
