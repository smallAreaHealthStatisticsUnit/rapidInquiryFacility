package org.sahsu.rif.generic.datastorage;

public final class CountTableRowsQueryFormatter
	extends AbstractSQLQueryFormatter {

	private String tableName;
	private String countFieldName;
	private boolean isDistinct;
	
	public CountTableRowsQueryFormatter() {

	}


	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	public void setCountFieldName(
		final String countFieldName, 
		final boolean isDistinct) {

		this.countFieldName = countFieldName;
		this.isDistinct = isDistinct;
	}
		
	public String generateQuery() {
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT");
		
		if (countFieldName == null) {
			query.append(" COUNT(*) ");
		}
		else {
			if (isDistinct) {
				query.append(" COUNT(DISTINCT ");
				query.append(countFieldName);
				query.append(") ");
			}
			else {				
				query.append(" COUNT(");
				query.append(countFieldName);
				query.append(") ");
			}
		}
		
		query.append("FROM ");
		query.append(convertCase(getSchemaTableName(tableName)));
		
		return query.toString();		
	}
}
