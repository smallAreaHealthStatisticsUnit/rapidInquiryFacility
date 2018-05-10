package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.ms.MSSQLGenerateResultsSubmissionStep;
import rifServices.dataStorageLayer.pg.PGSQLGenerateResultsSubmissionStep;

public interface GenerateResultsSubmissionStep {

	static GenerateResultsSubmissionStep getInstance(StudyStateManager manager, DatabaseType type) {

		switch (type) {
			case POSTGRESQL:
				return new PGSQLGenerateResultsSubmissionStep(manager);
			case SQL_SERVER:
				return new MSSQLGenerateResultsSubmissionStep(manager);
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unknown database type in "
				                                + "GenerateResultsSubmissionStep");
		}
	}

	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param studyID the ID of the study
	 * @throws RIFServiceException if anything goes wrong
	 */
	void performStep(final Connection connection, final String studyID) throws RIFServiceException;
}
