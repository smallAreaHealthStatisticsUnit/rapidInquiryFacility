package org.sahsu.rif.generic.datastorage;


public enum DatabaseType {
	POSTGRESQL("postgresql",
	           "pg",
	           "P O S T G R E S Q L",
	           "SELECT rif40_startup(?) AS rif40_init;",
	           "currval('rif40_study_id_seq'::regclass);",
	           "TO_CHAR(creation_date, 'DD MON YYYY HH24:MI:SS')"),

	SQL_SERVER("sqlServer",
	           "ms",
	           "M S S Q L S E R V E R",
	           "EXEC rif40.rif40_startup ?",
	           "rif40.rif40_sequence_current_value('rif40.rif40_study_id_seq')",
	           "convert(VARCHAR(20), creation_date, 13)"),

	UNKNOWN("unknown", "unk", "UNKNOWN", "", "", "");
		
	private String name;
	private String shortName;
	private String banner;
	private String initialisationQuery;
	private String studyIdQuery;
	private String convertDateToString;
	
	DatabaseType(
			final String name,
			final String shortName,
			final String banner,
			final String initialisationQuery,
			final String studyIdQuery,
			final String convertDateToString) {
		this.name = name;
		this.shortName = shortName;
		this.banner = banner;
		this.initialisationQuery = initialisationQuery;
		this.studyIdQuery = studyIdQuery;
		this.convertDateToString = convertDateToString;
	}
	
	public String getName() {
		return name;
	}	
	
	public String getShortName() {
		return shortName;
	}

	/**
	 * Returns a "banner" format of the database name, suitable for log files, etc.
	 * @return The banner form of the database name.
	 */
	public String banner() {
		return banner;
	}

	/**
	 * Gets the initialisation query string specific to the database type.
	 * @return the query string
	 */
	public String initialisationQuery() {
		return initialisationQuery;
	}

	/**
	 * Gets the study ID query string specific to the database type.
	 * @return the query string
	 */
	public String studyIdQuery() {
		return studyIdQuery;
	}

	/**
	 * Gets the platform-specific SQL function for converting dates to strings.
	 * @return the SQL function string
	 */
	public String convertDateToString() {
		return convertDateToString;
	}
}
