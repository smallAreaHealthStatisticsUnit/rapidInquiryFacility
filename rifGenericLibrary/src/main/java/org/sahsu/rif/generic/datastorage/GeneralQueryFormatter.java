package org.sahsu.rif.generic.datastorage;


/**
 * This is a query formatter that can be used when no other query formatting class is appropriate
 * (eg: SQLSelectQueryFormatter, SQLCountQueryFormatter do not seem to fit.)
 */
public final class GeneralQueryFormatter extends AbstractSQLQueryFormatter {

	public void addQuery(final QueryFormatter queryFormatter) {
		StringBuilder query = getQueryBuilder();
		query.append(queryFormatter.generateQuery());
	}
}


