package rifServices.test.util;

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
				{"webApplicationDirectory", "/"},
				{"rScriptDirectory", "/"},
				{"extractDirectory", "/"},
				{"odbcDataSourceName", "odbc"},
				{"database.databaseType", "none"},
				{"maximumMapAreasAllowedForSingleDisplay", "200"},
				{"database.isCaseSensitive", "true"},
				{"database.isSSLSupported", "true"},
				{"cache", "100"},
				{"webApplicationDirectory", "/"},
				{"extraDirectoryForExtractFiles", "/"},
				{"database.useSSLDebug", "true"},
				{"database.sslTrustStore", ""},
				{"database.sslTrustStorePassword", ""}
		};
	}
}
