package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.StudyState;

public interface StudyStateManager extends SQLManager {
	void clearStudyStatusUpdates(
			Connection connection,
			User user,
			String studyID)
		throws RIFServiceException;
	
	/**
	 * Gets the covariates.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	StudyState getStudyState(
			Connection connection,
			User user,
			String studyID)
		throws RIFServiceException;
	
	void rollbackStudy(
			Connection connection,
			String studyID)
			throws RIFServiceException;
	
	void updateStudyStatus(
			Connection connection,
			User user,
			String studyID,
			StudyState studyState,
			String statusMessage,
			String traceMessage)
				throws RIFServiceException;
	
	RIFResultTable getCurrentStatusAllStudies(
			Connection connection,
			User user)
					throws RIFServiceException;
	
	void checkNonExistentStudyID(
			Connection connection,
			User user,
			String studyID)
						throws RIFServiceException;
}