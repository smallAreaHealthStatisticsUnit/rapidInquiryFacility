package rifGenericLibrary.dataStorageLayer;


public enum DatabaseType {
	POSTGRESQL("postgresql",
	           "pg",
	           "SELECT rif40_startup(?) AS rif40_init;",
	           "currval('rif40_study_id_seq'::regclass);"),

	SQL_SERVER("sqlServer",
	           "ms",
	           "EXEC rif40.rif40_startup ?",
	           "rif40.rif40_sequence_current_value('rif40.rif40_study_id_seq')"),

	UNKNOWN("unknown", "unk", "", "");
		
	private String name;
	private String shortName;
	private String initialisationQuery;
	private String studyIdQuery;
	
	DatabaseType(
			final String name,
			final String shortName,
			final String initialisationQuery,
			final String studyIdQuery) {
		this.name = name;
		this.shortName = shortName;
		this.initialisationQuery = initialisationQuery;
		this.studyIdQuery = studyIdQuery;
	}
	
	public String getName() {
		return name;
	}	
	
	public String getShortName() {
		return shortName;
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
}
