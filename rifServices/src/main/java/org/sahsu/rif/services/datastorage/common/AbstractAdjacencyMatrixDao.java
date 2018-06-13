package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.QueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public abstract class AbstractAdjacencyMatrixDao extends BaseSQLManager
		implements AdjacencyMatrixDao {

	public AbstractAdjacencyMatrixDao(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
	}

	protected AdjacencyMatrix getAdjacencyMatrix(final User user, final String studyId,
			final QueryFormatter formatter) throws SQLException, RIFServiceException {

		final AdjacencyMatrix matrix;

		logSQLQuery("Get Adjacency Matrix", formatter);

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement getMatrix = SQLQueryUtility.createPreparedStatement(
				connection, formatter.generateQuery())) {

			getMatrix.setString(1, studyId);
			ResultSet resultSet = getMatrix.executeQuery();
			matrix = AdjacencyMatrix.builder()
					         .areaId(resultSet.getString(1))
					         .numAdjacencies(resultSet.getInt(2))
					         .adjacencyList(resultSet.getString(3))
					         .build();
		} finally {

			reclaimPooledReadConnection(user, connection);
		}

		return matrix;
	}
}
