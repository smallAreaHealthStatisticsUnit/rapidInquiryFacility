package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;

import static org.sahsu.rif.generic.datastorage.SQLQueryUtility.createPreparedStatement;

/**
 * Utility class to get the covariate description from the database.
 */
class CovariateDescription {

	private final SQLManager manager;
	private final Connection connection;
	private final String geographyName;
	private final String geoLevelName;
	private final DatabaseType dbType;
	private String covariateName;

	CovariateDescription(final SQLManager manager, final Connection connection,
			final String geographyName, final String geoLevelName,
			final String covariateName) {

		this.manager = manager;
		this.connection = connection;
		dbType = manager.getDbType();
		this.geographyName = geographyName;
		this.geoLevelName = geoLevelName;
		this.covariateName = covariateName;
	}

	String get() throws SQLException {

		SelectQueryFormatter covariateTableFormatter = SelectQueryFormatter.getInstance(dbType);

		// We need to set this as case sensitive, because it's interrogating values in
		// columns in the information schema.
		covariateTableFormatter.setCaseSensitive(true);
		covariateTableFormatter.setDatabaseSchemaName("rif40");
		covariateTableFormatter.addFromTable("rif40_geolevels");
		covariateTableFormatter.addSelectField("covariate_table");
		covariateTableFormatter.addWhereParameter("geography");
		covariateTableFormatter.addWhereParameter("geolevel_name");
		PreparedStatement ps = createPreparedStatement(connection, covariateTableFormatter);
		ps.setString(1, geographyName);
		ps.setString(2, geoLevelName);
		ResultSet rs = ps.executeQuery();
		connection.commit();
		if (rs.next()) {
			String covariatesSubTableName = rs.getString(1);
			return manager.getColumnComment(connection, "rif_data", covariatesSubTableName,
			                                covariateName);
		}
		return "";
	}
}
