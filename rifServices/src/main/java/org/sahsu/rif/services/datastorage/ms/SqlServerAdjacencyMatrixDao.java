package org.sahsu.rif.services.datastorage.ms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.GeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrixRow;
import org.sahsu.rif.services.datastorage.common.AbstractAdjacencyMatrixDao;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class SqlServerAdjacencyMatrixDao extends AbstractAdjacencyMatrixDao {

	public SqlServerAdjacencyMatrixDao(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
	}

	@Override
	public List<AdjacencyMatrixRow> getByStudyId(final User user, final int studyId)
			throws SQLException, RIFServiceException {

		String adjacencyTable = getAdjacencyTable(user, studyId,
		                                          rifDatabaseProperties.getDatabaseType());

		return getAdjacencyMatrix(user, studyId,
		                          createFormatterForAdjacencyMatrix(studyId, adjacencyTable));
	}

	private String getAdjacencyTable(final User user, final int studyId,
			 final DatabaseType type) throws SQLException, RIFServiceException {

		final String adjacencyTable;
		SelectQueryFormatter formatter = SelectQueryFormatter.getInstance(type);
		formatter.setDatabaseSchemaName("rif40");
		formatter.addSelectField("adjacencytable");
		formatter.addFromTable("rif40_studies");
		formatter.addFromTable("rif40_geographies");
		formatter.addWhereParameter("rif40_studies","study_id" );
		formatter.addWhereJoinCondition(
				"rif40_studies", "geography",
				"rif40_geographies", "geography");

		logSQLQuery("Get Adjacency Table", formatter);

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement adjTableFormatter = SQLQueryUtility.createPreparedStatement(
				                            connection, formatter.generateQuery())) {

			adjTableFormatter.setInt(1, studyId);
			ResultSet resultSet = adjTableFormatter.executeQuery();
			if (resultSet.next()) {
				adjacencyTable = resultSet.getString(1);
			} else {
				throw new RIFServiceException("No rows returned for adjacency table");
			}

			// Also an error if there is more than one row.
			if (resultSet.next()) {
				throw new RIFServiceException("More than one row returned for adjacency table");
			}
		} finally {

			reclaimPooledReadConnection(user, connection);
		}

		return adjacencyTable;
	}

	private GeneralQueryFormatter createFormatterForAdjacencyMatrix(
			final int studyId, final String adjacencyTable) {

		GeneralQueryFormatter formatter = new GeneralQueryFormatter();
		formatter.setDatabaseSchemaName("rif40");
		formatter.addQueryLine(0, "WITH b as (");
		formatter.addQueryLine(1,"SELECT b1.area_id, b3.geolevel_id FROM ");
		formatter.addQueryLine(2, "rif40_study_areas b1,");
		formatter.addQueryLine(2, "rif40_studies b2,");
		formatter.addQueryLine(2, "rif40_geolevels b3");
		formatter.addQueryLine(1, "WHERE b1.study_id = " + studyId);
		formatter.addQueryLine(1, "AND b1.study_id = b2.study_id");
		formatter.addQueryLine(1, "AND b2.geography = b3.geography");
		formatter.addQueryLine(0,")");
		formatter.addQueryLine(0,
		                       "SELECT c1.areaid AS area_id, c1.num_adjacencies, "
		                       + "c1.adjacency_list");
		formatter.addQueryLine(1, "FROM " + adjacencyTable + " c1, b");
		formatter.addQueryLine(1, "WHERE c1.geolevel_id = b.geolevel_id");
		formatter.addQueryLine(1, "AND c1.areaid = b.area_id");
		formatter.addQueryLine(1, "ORDER BY area_id");
		return formatter;
	}
}
