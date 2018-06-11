package org.sahsu.rif.services.datastorage.pg;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.datastorage.common.AdjacencyMatrixDao;

public class PostgresAdjacencyMatrixDao implements AdjacencyMatrixDao {

	private final DataSource dataSource;

	public PostgresAdjacencyMatrixDao(final DataSource dataSource) {

		this.dataSource = dataSource;
	}

	@Override
	public AdjacencyMatrix getByStudyId(final String studyId) throws SQLException {

		Connection connection = dataSource.getConnection();
		FunctionCallerQueryFormatter formatter = new FunctionCallerQueryFormatter();
		formatter.setFunctionName("rif40_GetAdjacencyMatrix");
		formatter.setDatabaseSchemaName("rif40_xml_pkg");
		formatter.setNumberOfFunctionParameters(1);


		/*
		  sql <- paste("SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(", studyID, ")")
    AdjRowset=doSQLQuery(sql)
    numberOfRows <- nrow(AdjRowset)
		 */

		return null;
	}
}
