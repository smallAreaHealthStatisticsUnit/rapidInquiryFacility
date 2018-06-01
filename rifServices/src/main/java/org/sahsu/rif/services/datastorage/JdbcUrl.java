package org.sahsu.rif.services.datastorage;

import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

/**
 * Converts the various database properties into a URL suitable for JDBC.
 */
public class JdbcUrl {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private final RIFServiceStartupOptions options;

	public JdbcUrl(final RIFServiceStartupOptions options) {
		this.options = options;
	}

	public String url() {

		String databaseDriverPrefix = options.getDatabaseDriverPrefix();

		switch (databaseDriverPrefix) {
			case "jdbc:sqlserver":
				return databaseDriverPrefix
				       + ":"
				       + "//"
				       + options.getHost()
				       + ":"
				       + options.getPort();
			case "jdbc:postgresql":
				return databaseDriverPrefix
				       + ":"
				       + "//"
				       + options.getHost()
				       + ":"
				       + options.getPort()
				       + "/"
				       + options.getDatabaseName();
			default:
				rifLogger.error(getClass(),
				                "Unsupported database driver: " + databaseDriverPrefix);
				return null;
		}
	}
}
