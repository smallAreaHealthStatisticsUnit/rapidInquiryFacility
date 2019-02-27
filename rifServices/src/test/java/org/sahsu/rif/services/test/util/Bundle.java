package org.sahsu.rif.services.test.util;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

public class Bundle extends ListResourceBundle {

	@Override
	protected Object[][] getContents() {

		return new Object[][] {

				{"database.driverClassName", "com.test.Driver"},
				{"database.jdbcDriverPrefix", "??"},
				{"database.host", "localhost"},
				{"database.port", "8080"},
				{"database.databaseName", "sahsu"},
				{"extractDirectory", "/"},
				{"odbcDataSourceName", "odbc"},
				{"database.databaseType", "none"},
				{"database.isCaseSensitive", "true"},
				{"database.isSSLSupported", "true"},
				{"database.useSSLDebug", "true"},
				{"database.sslTrustStore", ""},
				{"database.sslTrustStorePassword", ""}
		};
	}
}
