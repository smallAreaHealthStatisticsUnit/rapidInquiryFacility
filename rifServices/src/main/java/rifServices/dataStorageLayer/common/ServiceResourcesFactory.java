package rifServices.dataStorageLayer.common;

import rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources;
import rifServices.dataStorageLayer.pg.PGSQLRIFServiceResources;
import rifServices.system.RIFServiceStartupOptions;

public class ServiceResourcesFactory {
	
	public static ServiceResources getInstance(RIFServiceStartupOptions options) {
		
		switch (options.getRifDatabaseType()) {
			
			case POSTGRESQL:
				return PGSQLRIFServiceResources.newInstance(options);
	
			case SQL_SERVER:
				return MSSQLRIFServiceResources.newInstance(options);
	
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unknown database type: " + options.getRifDatabaseType());
		}
	}
}
