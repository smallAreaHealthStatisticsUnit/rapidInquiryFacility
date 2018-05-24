package org.sahsu.rif.generic.datastorage;

public class RIFDatabaseProperties {

	private static final String POSTGRES_INITIALISATION_QUERY =
			"SELECT rif40_startup(?) AS rif40_init;";
	private static final String MS_SQL_INITIALISATION_QUERY = "EXEC rif40.rif40_startup ?";
	private static final String POSTGRES_STUDY_ID_QUERY_STRING =
			"currval('rif40_study_id_seq'::regclass);";
	private static final String MS_SQL_STUDY_ID_QUERY_STRING =
			"rif40.rif40_sequence_current_value('rif40.rif40_study_id_seq')";

	private DatabaseType databaseType;
	private boolean isCaseSensitive;
	private boolean isSSLSupported;

	private RIFDatabaseProperties(final DatabaseType databaseType, final boolean isCaseSensitive,
			final boolean isSSLSupported) {

		this.databaseType = databaseType;
		this.isCaseSensitive = isCaseSensitive;
		this.isSSLSupported = isSSLSupported;
	}

	public static RIFDatabaseProperties newInstance(final DatabaseType databaseType,
			final boolean isCaseSensitive, final boolean supportsSSL) {

		return new RIFDatabaseProperties(databaseType, isCaseSensitive, supportsSSL);
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	
	public boolean isSSLSupported() {
		return isSSLSupported;
	}
	
	public DatabaseType getDatabaseType() {
		return databaseType;
	}
}
