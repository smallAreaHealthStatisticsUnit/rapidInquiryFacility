package org.sahsu.rif.services.datastorage.pg;

import java.sql.SQLException;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrix;
import org.sahsu.rif.services.datastorage.common.AbstractAdjacencyMatrixDao;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class PostgresAdjacencyMatrixDao extends AbstractAdjacencyMatrixDao {

	public PostgresAdjacencyMatrixDao(
			final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
	}

	@Override
	public AdjacencyMatrix getByStudyId(final User user, final String studyId)
			throws SQLException, RIFServiceException {

		FunctionCallerQueryFormatter formatter = new FunctionCallerQueryFormatter();
		formatter.setFunctionName("rif40_GetAdjacencyMatrix");
		formatter.setDatabaseSchemaName("rif40_xml_pkg");
		formatter.setNumberOfFunctionParameters(1);

		return getAdjacencyMatrix(user, studyId, formatter);
	}

}
