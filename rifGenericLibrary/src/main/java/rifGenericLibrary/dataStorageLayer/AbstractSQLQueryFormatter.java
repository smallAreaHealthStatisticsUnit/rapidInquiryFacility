package rifGenericLibrary.dataStorageLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import rifGenericLibrary.util.RIFLogger;

public class AbstractSQLQueryFormatter implements QueryFormatter {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	private static Map<String, String> environmentalVariables = System.getenv();
	private static String catalinaHome = environmentalVariables.get("CATALINA_HOME");
	
	/** The query. */
	private StringBuilder query;
	
	private String databaseSchemaName;
	
	private DatabaseType databaseType;
	private boolean isCaseSensitive;
	
	private boolean endWithSemiColon;

	/**
	 * Instantiates a new SQL query formatter.
	 */
	public AbstractSQLQueryFormatter() {
		isCaseSensitive = true;
		query = new StringBuilder();
		endWithSemiColon = false;
	}

	@Override
	public void setDatabaseSchemaName(final String databaseSchemaName) {
		this.databaseSchemaName = databaseSchemaName;
	}
	
	@Override
	public String getDatabaseSchemaName() {
		return databaseSchemaName;
	}
	
	protected String getSchemaTableName(final String tableName) {
		StringBuilder schemaTableName = new StringBuilder();
		
		if (databaseSchemaName != null) {
			schemaTableName.append(databaseSchemaName);
			schemaTableName.append(".");			
		}
		schemaTableName.append(tableName);
		return schemaTableName.toString();
	}
	
	protected StringBuilder getQueryBuilder() {
		return query;
	}
	
	/**
	 * Convert case.
	 *
	 * @param sqlPhrase the sql phrase
	 * @return the string
	 */
	protected String convertCase(
		final String sqlPhrase) {

		if (!isCaseSensitive) {
			return sqlPhrase.toUpperCase();				
		}
		else {
			return sqlPhrase;								
		}		
	}
	
	@Override
	public DatabaseType getDatabaseType() {
		return databaseType;
	}
	
	@Override
	public void setDatabaseType(
			final DatabaseType databaseType) {
		
		this.databaseType = databaseType;
	}
	
	@Override
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	
	@Override
	public void setCaseSensitive(
			final boolean isCaseSensitive) {

		this.isCaseSensitive = isCaseSensitive;
	}
	
	/*
	 * This method is largely stubbed and will be filled out later.  The purpose
	 * is to help convert to lower case or upper case, depending on database-specific
	 * needs.  eg: one vendor uses capital letters, another does not
	 */
	@Override
	public void addCaseSensitivePhrase(
			final String queryPhrase) {
		
		query.append(queryPhrase);
	}

	@Override
	public void addPaddedQueryPhrase(
			final String queryPhrase) {
		
		query.append(queryPhrase);
		query.append(" ").append(lineSeparator);
	}
	
	@Override
	public void addBlankLine() {
		query.append(lineSeparator).append(lineSeparator);
	}

	@Override
	public void addPaddedQueryLine(
			final int indentationLevel,
			final String queryPhrase) {
			
		addIndentation(indentationLevel);	
		query.append(queryPhrase);
		query.append(" ").append(lineSeparator);
	}

	@Override
	public void addQueryPhrase(
			final int indentationLevel,
			final String queryPhrase) {
		
		addIndentation(indentationLevel);	
		query.append(queryPhrase);		
	}
	
	@Override
	public void addQueryPhrase(
			final String queryPhrase) {
		
		query.append(queryPhrase);
	}

	@Override
	public void createQueryFromFile(final String fileName, final String[] args,
			final DatabaseType databaseType)
			throws Exception {
		setDatabaseType(databaseType);
		FileReader file = getQueryFileReader(fileName);

		try (BufferedReader bufferedReader = new BufferedReader(file)) {
			// wrap a BufferedReader around FileReader
			String line;

			// use the readLine method of the BufferedReader to read one line at a time.
			// the readLine method returns null when there is nothing else to read.
			while ((line = bufferedReader.readLine()) != null) { // CR/CRLF independent
				query.append(line).append(lineSeparator);
			}
		} finally {

			for (int i = 0; i < args.length; i++) {
				queryReplaceAll("%" + (i + 1), args[i]); // replace %1 with args[1] etc
			}
		}		
	}

	@Override
	public void queryReplaceAll(String from, String to) {
		int index = query.indexOf(from);
		while (index != -1)
		{
			query.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = query.indexOf(from, index);
		}
	}

	/**
	 * Get query file handle from file name
	 *
	 * Looks in:
	 * %CATALINA_HOME%\conf\dataStorageLayerSQL\common
	 * %CATALINA_HOME%\conf\dataStorageLayerSQL\<database type: ms or pg>
	 * %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\dataStorageLayerSQL\common
	 * %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\dataStorageLayerSQL\<database type: ms or pg>
	 *
	 * @param  fileName		Name of file containing query
	 * @return FileReader	Handle
	 */		
	private FileReader getQueryFileReader(String fileName)  
			throws Exception {

		FileReader file;
		
		String fileName1;
		String fileName2;
		String fileName3;
		String fileName4;
		
		if (fileName == null) {
			throw new Exception("getQueryFileReader: fileName not set"); 
		}
		if (databaseType == null) {
			throw new Exception("getQueryFileReader: databaseType not set"); 
		}
		if (databaseType.getShortName() == null) {
			throw new Exception("getQueryFileReader: databaseType.getShortName() returns null"); 
		}
		String basePath1;
		String basePath2;
		if (catalinaHome != null) {
// e.g. %CATALINA_HOME%\conf\dataStorageLayerSQL
			basePath1=catalinaHome + File.separator + "conf" + File.separator + "dataStorageLayerSQL"; 
// e.g. %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\dataStorageLayerSQL
			basePath2=catalinaHome + File.separator + "webapps" + File.separator + 
				"rifServices" + File.separator + "WEB-INF" + File.separator + "classes" + 
				File.separator + "dataStorageLayerSQL"; 
		} else {
			throw new Exception("getQueryFileReader: CATALINA_HOME not set in environment"); 
		}
		fileName1=basePath1 + File.separator + "common" + File.separator + fileName;
		fileName2=basePath1 + File.separator + 
			databaseType.getShortName() + File.separator + fileName;
		fileName3=basePath2 + File.separator + "common" + File.separator + fileName;
		fileName4=basePath2+ File.separator + 
			databaseType.getShortName() + File.separator + fileName;
		
		try {
			file=new FileReader(fileName1);
		} 
		catch (IOException ioException) {
			try {
				file=new FileReader(fileName2);
			}
			catch (IOException ioException2) {
				try {
					file=new FileReader(fileName3);
				}				
				catch (IOException ioException3) {
					try {
						file=new FileReader(fileName4);
					}
					catch (IOException ioException4) {				
						rifLogger.error(this.getClass(), 
							"getQueryFileReader error for files: " + 
								fileName1 + ", " + fileName2 + ", " + fileName3 + " and " + fileName3, 
							ioException4);
						throw ioException4;
					}
				}
			}
		}			
		return file;
	}
	
	@Override
	public void addQueryLine(
			final int indentationLevel,
			final String queryPhrase) {
		
		addIndentation(indentationLevel);		
		query.append(queryPhrase);
		query.append(lineSeparator);
	
	}

	@Override
	public void addUnderline() {
		
		query.append(" -- ");
		for (int i = 0; i < 60; i++) {
			query.append("=");
		}
		query.append(lineSeparator);
	}
	
	@Override
	public void addComment(
			final String lineComment) {
		
		query.append(" -- ");
		query.append(lineComment);
	}
	
	@Override
	public void addCommentLine(
			final String lineComment) {
			
		query.append(" -- ");
		query.append(lineComment);
		query.append(lineSeparator);
	}	
	
	private void addIndentation(
		final int indentationLevel) {
		
		for (int i = 0; i < indentationLevel; i++) {
			query.append("   ");
		}
		
		//query.append("\t");
	}
	
	@Override
	public void finishLine(
			final String queryPhrase) {

		query.append(queryPhrase).append(lineSeparator);
	}
	
	@Override
	public void finishLine() {
		query.append(lineSeparator);
	}
	
	@Override
	public void padAndFinishLine() {
		query.append(" ").append(lineSeparator);
	}
	
	protected void resetAccumulatedQueryExpression() {
		query = new StringBuilder();
	}
	
	
	@Override
	public String generateQuery() {
		StringBuilder result = new StringBuilder();
		result.append(query.toString());
		
		if (endWithSemiColon) {
			result.append(";");
		}
		result.append(lineSeparator);
		return result.toString();
	}
	
	@Override
	public void considerWritingSemiColon() {
	}
	
	@Override
	public boolean endWithSemiColon() {
		return endWithSemiColon;
	}
	
	@Override
	public void setEndWithSemiColon(boolean endWithSemiColon) {
		this.endWithSemiColon = endWithSemiColon;
	}
}
