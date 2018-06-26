package org.sahsu.rif.services.datastorage.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.system.files.study.StudyCsvs;

import lombok.Builder;

/**
 * A generic DAO for doing "SELECT * FROM [schema].[table]" queries.
 */
@Builder
public class GenericSelectAllDao extends BaseSQLManager {

	private RIFServiceStartupOptions options;
	private final String schemaName;
	private final String extractTableName;
	private final User user;

	private GenericSelectAllDao(
			RIFServiceStartupOptions o, String schemaName, String extractTableName, User user) {

		super(o);
		this.schemaName = schemaName;
		this.extractTableName = extractTableName;
		this.user = user;
	}

	public void dumpAllToCsv(final StudyCsvs csvs) throws RIFServiceException {

		SelectQueryFormatter formatter = SelectQueryFormatter.getInstance(
				rifServiceStartupOptions.getRifDatabaseType());
		formatter.setDatabaseSchemaName(schemaName);
		formatter.addFromTable(extractTableName);
		formatter.addSelectField("*");

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement statement = SQLQueryUtility.createPreparedStatement(
				connection, formatter.generateQuery())) {

			ResultSet resultSet = statement.executeQuery();
			csvs.extractTableToCsv(resultSet);

		} catch (SQLException | IOException e) {

			throw new RIFServiceException(e);
		} finally {

			reclaimPooledReadConnection(user, connection);
		}
	}
}
