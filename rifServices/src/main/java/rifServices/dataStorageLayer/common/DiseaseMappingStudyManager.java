package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Project;

public interface DiseaseMappingStudyManager extends SQLManager {
	/**
	 * Gets the projects.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return the projects
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<Project> getProjects(
			Connection connection,
			User user)
		throws RIFServiceException;
	
	void clearStudiesForUser(
			Connection connection,
			User user)
			throws RIFServiceException;
	
	void checkNonExistentItems(
			User user,
			Connection connection,
			DiseaseMappingStudy diseaseMappingStudy)
				throws RIFServiceException;
	
	void checkDiseaseMappingStudyExists(
			Connection connection,
			String studyID)
					throws RIFServiceException;
}
