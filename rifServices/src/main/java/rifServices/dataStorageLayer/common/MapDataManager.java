package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.crypto.Data;

import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.MapArea;
import rifServices.dataStorageLayer.ms.MSSQLMapDataManager;
import rifServices.dataStorageLayer.pg.PGSQLMapDataManager;
import rifServices.system.RIFServiceStartupOptions;

public interface MapDataManager extends SQLManager {

	static MapDataManager getInstance(RIFServiceStartupOptions options) {

		switch (options.getRifDatabaseType()) {
			case SQL_SERVER:
				return new MSSQLMapDataManager(options);
			case POSTGRESQL:
				return new PGSQLMapDataManager(options);
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unknown database type in MapDataManager");
		}

	}
	ArrayList<MapArea> getAllRelevantMapAreas(
			Connection connection,
			Geography geography,
			AbstractGeographicalArea geographicalArea)
		throws RIFServiceException;
}
