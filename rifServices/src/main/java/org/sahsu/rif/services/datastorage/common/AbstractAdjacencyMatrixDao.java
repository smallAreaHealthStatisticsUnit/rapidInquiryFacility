package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.QueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrixRow;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import com.google.common.collect.Lists;

public abstract class AbstractAdjacencyMatrixDao extends BaseSQLManager
		implements AdjacencyMatrixDao {

	public AbstractAdjacencyMatrixDao(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
	}

	protected List<AdjacencyMatrixRow> getAdjacencyMatrix(final User user, final int studyId,
			final QueryFormatter formatter) throws SQLException, RIFServiceException {

		final List<AdjacencyMatrixRow> matrix = Lists.newArrayList();

		logSQLQuery("Get Adjacency Matrix", formatter);

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement getMatrix = SQLQueryUtility.createPreparedStatement(
				connection, formatter.generateQuery())) {

			getMatrix.setInt(1, studyId);
			ResultSet resultSet = getMatrix.executeQuery();

			while(resultSet.next()) {

				AdjacencyMatrixRow row = AdjacencyMatrixRow.builder()
						         .areaId(resultSet.getString(1))
						         .numAdjacencies(resultSet.getInt(2))
						         .adjacencyList(resultSet.getString(3))
						         .build();
				matrix.add(row);
			}
		} finally {

			reclaimPooledReadConnection(user, connection);
		}

		return matrix;
	}
}
