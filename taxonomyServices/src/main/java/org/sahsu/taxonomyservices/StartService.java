package org.sahsu.taxonomyservices;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.fileformats.tomcat.TomcatBase;
import org.sahsu.rif.generic.fileformats.tomcat.TomcatFile;
import org.sahsu.rif.generic.util.TaxonomyLogger;

public class StartService {

	private static StartService THE_INSTANCE;
	private final TaxonomyLogger logger = TaxonomyLogger.getLogger();
	private final WebServiceResponseUtility webServiceResponseUtility;

	private boolean running;

	public static StartService instance() {

		if (THE_INSTANCE == null) {

			THE_INSTANCE = new StartService();
		}

		return THE_INSTANCE;
	}

	private StartService() {

		webServiceResponseUtility = new WebServiceResponseUtility();
	}

	public Response start() {

		Response result = Response.ok(String.valueOf(true), MediaType.APPLICATION_JSON).build();

		if (!isRunning()) {
			try {

				FederatedTaxonomyService federatedTaxonomyService =
						FederatedTaxonomyService.getFederatedTaxonomyService();
				String fullPath = new TomcatFile(new TomcatBase(), ".")
						                  .pathToClassesDirectory().toString();
				federatedTaxonomyService.initialise(fullPath);
				webServiceResponseUtility.serialiseStringResult(String.valueOf(true));

				running = true;

			} catch (Exception exception) {

				String errorMsg = "Couldn't start service";
				logger.error(getClass(), errorMsg, exception);
				result = Response.serverError().entity(errorMsg).type(MediaType.APPLICATION_JSON)
						         .build();
			}
		}

		return result;
	}

	boolean isRunning() {

		return running;
	}
}
