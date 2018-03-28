package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Geography;

public interface ResultsQueryManager extends SQLManager {
	
	RIFResultTable getTileMakerCentroids(
			Connection connection,
			User user,
			Geography geography,
			GeoLevelSelect geoLevelSelect)
			throws RIFServiceException;
	
	String getTileMakerTiles(
			Connection connection,
			User user,
			Geography geography,
			GeoLevelSelect geoLevelSelect,
			Integer zoomlevel,
			Integer x,
			Integer y)
				throws RIFServiceException;
	
	String getStudyName(
			Connection connection,
			String studyID)
					throws RIFServiceException;
}
