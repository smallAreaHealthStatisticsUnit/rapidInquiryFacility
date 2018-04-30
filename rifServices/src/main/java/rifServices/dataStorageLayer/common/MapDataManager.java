package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.MapArea;

public interface MapDataManager extends SQLManager {
	
	ArrayList<MapArea> getAllRelevantMapAreas(
			Connection connection,
			Geography geography,
			AbstractGeographicalArea geographicalArea)
		throws RIFServiceException;
}
