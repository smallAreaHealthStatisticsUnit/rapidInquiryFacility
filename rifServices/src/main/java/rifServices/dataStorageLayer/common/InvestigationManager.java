package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;

public interface InvestigationManager extends SQLManager {
	
	void checkNonExistentItems(
			Connection connection,
			Geography geography,
			GeoLevelToMap geoLevelToMap,
			Investigation investigation)
		throws RIFServiceException;
	
	void checkInvestigationExists(
			Connection connection,
			AbstractStudy study,
			Investigation investigation)
			throws RIFServiceException;
}
