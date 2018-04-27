package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;

public interface CovariateManager extends SQLManager {
	/**
	 * Gets the covariates for investigation.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param diseaseMappingStudy the disease mapping study
	 * @param investigation the investigation
	 * @return the covariates for investigation
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<AbstractCovariate> getCovariatesForInvestigation(
			Connection connection,
			User user,
			DiseaseMappingStudy diseaseMappingStudy,
			Investigation investigation)
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
	ArrayList<AbstractCovariate> getCovariates(
			Connection connection,
			Geography geography,
			GeoLevelToMap geoLevelToMap)
		throws RIFServiceException;
	
	@SuppressWarnings("resource")
	void checkNonExistentCovariates(
			Connection connection,
			Geography geography,
			GeoLevelToMap geoLevelToMap,
			ArrayList<AbstractCovariate> covariates)
		throws RIFServiceException;
}
