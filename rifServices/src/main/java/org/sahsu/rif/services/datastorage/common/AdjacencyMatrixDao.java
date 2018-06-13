package org.sahsu.rif.services.datastorage.common;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.datastorage.ms.SqlServerAdjacencyMatrixDao;
import org.sahsu.rif.services.datastorage.pg.PostgresAdjacencyMatrixDao;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public interface AdjacencyMatrixDao {

	static AdjacencyMatrixDao getInstance(RIFServiceStartupOptions options)
			throws RIFServiceException {

		switch (options.getRifDatabaseType()) {

			case SQL_SERVER:
				return new SqlServerAdjacencyMatrixDao(options);
			case POSTGRESQL:
				return new PostgresAdjacencyMatrixDao(options);
			case UNKNOWN:
			default:
				throw new RIFServiceException(
						"Unknown database type %s in AdjacencyMatrixDao",
						options.getRifDatabaseType());
		}
	}

	/**
	 * Returns an {@link AdjacencyMatrix} given a study ID.
	 * @param studyId the study
	 * @return the adjacency matrix
	 * @throws SQLException for database problems
	 * @throws RIFServiceException for general problems
	 */
	AdjacencyMatrix getByStudyId(final User user, final String studyId)
			throws SQLException, RIFServiceException;
}
