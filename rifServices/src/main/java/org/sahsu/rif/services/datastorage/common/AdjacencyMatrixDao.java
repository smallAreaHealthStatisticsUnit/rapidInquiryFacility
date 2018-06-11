package org.sahsu.rif.services.datastorage.common;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.datastorage.ms.SqlServerAdjacencyMatrixDao;
import org.sahsu.rif.services.datastorage.pg.PostgresAdjacencyMatrixDao;

public interface AdjacencyMatrixDao {

	static AdjacencyMatrixDao getInstance(DatabaseType type, DataSource dataSource) throws
	                                                                      RIFServiceException {

		switch (type) {

			case SQL_SERVER:
				return new SqlServerAdjacencyMatrixDao(dataSource);
			case POSTGRESQL:
				return new PostgresAdjacencyMatrixDao(dataSource);
			case UNKNOWN:
			default:
				throw new RIFServiceException(
						"Unknown database type %s in AdjacencyMatrixDao", type);
		}

	}

	AdjacencyMatrix getByStudyId(String studyId) throws SQLException;
}
