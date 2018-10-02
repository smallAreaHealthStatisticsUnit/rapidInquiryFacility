package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;

import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.datastorage.ms.MSSQLGenerateResultsSubmissionStep;
import org.sahsu.rif.services.datastorage.pg.PGSQLGenerateResultsSubmissionStep;

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
	boolean performStep(final Connection connection, final String studyID) throws RIFServiceException;
	
	String getResult();
	String getStack();
}
