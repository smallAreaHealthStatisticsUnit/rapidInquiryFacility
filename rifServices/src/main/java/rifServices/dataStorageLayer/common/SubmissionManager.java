package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;

public interface SubmissionManager extends SQLManager {

	RIFStudySubmission getRIFStudySubmission(
			Connection connection,
			User user,
			String studyID)
				throws RIFServiceException;
	DiseaseMappingStudy getDiseaseMappingStudy(
			Connection connection,
			User user,
			String studyID)
		throws RIFServiceException;
}
