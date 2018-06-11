package org.sahsu.rif.services.datastorage.ms;

import javax.sql.DataSource;

import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.datastorage.common.AdjacencyMatrixDao;

public class SqlServerAdjacencyMatrixDao implements AdjacencyMatrixDao {

	private final DataSource dataSource;

	public SqlServerAdjacencyMatrixDao(final DataSource dataSource) {

		this.dataSource = dataSource;
	}

	@Override
	public AdjacencyMatrix getByStudyId(final String studyId) {

		/*
		"SELECT b2.adjacencytable
                 FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
                 WHERE b1.study_id  = ", studyID ,"
                 AND b2.geography = b1.geography"
		 */

		/*
		sql <- paste("SELECT b2.adjacencytable
                 FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
                 WHERE b1.study_id  = ", studyID ,"
                 AND b2.geography = b1.geography");
    adjacencyTableRes=doSQLQuery(sql)
    numberOfRows <- nrow(adjacencyTableRes)
    if (numberOfRows != 1) {
      cat(paste("Expected 1 row; got: " + numberOfRows + "; SQL> ", sql, "\n"), sep="")
      exitValue <<- 1
    }
    adjacencyTable <- tolower(adjacencyTableRes$adjacencytable[1])
#    print(adjacencyTable);
    sql <- paste("WITH b AS ( /* Tilemaker: has adjacency table *
		SELECT b1.area_id, b3.geolevel_id
		FROM [rif40].[rif40_study_areas] b1, [rif40].[rif40_studies] b2, [rif40].[rif40_geolevels] b3
		WHERE b1.study_id  = ", studyID ,"
		AND b1.study_id  = b2.study_id
		AND b2.geography = b3.geography
    )
		SELECT c1.areaid AS area_id, c1.num_adjacencies, c1.adjacency_list
		FROM [rif_data].[", adjacencyTable, "] c1, b
		WHERE c1.geolevel_id   = b.geolevel_id
		AND c1.areaid        = b.area_id
		ORDER BY 1", sep = "")
		AdjRowset=doSQLQuery(sql)
		numberOfRows <- nrow(AdjRowset)
		 */


		return null;
	}
}
