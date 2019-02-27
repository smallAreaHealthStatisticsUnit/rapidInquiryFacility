package org.sahsu.taxonomyservices;

import java.nio.file.Path;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.util.TaxonomyLogger;

public class StartService {

	private static StartService THE_INSTANCE;
	private final TaxonomyLogger logger = TaxonomyLogger.getLogger();

	private boolean running;

	public static StartService instance() {

		if (THE_INSTANCE == null) {

			THE_INSTANCE = new StartService();
		}

		return THE_INSTANCE;
	}

	public Response start() {

		Response result = Response.ok(String.valueOf(true), MediaType.APPLICATION_JSON).build();

		if (!isRunning()) {
			try {

				FederatedTaxonomyService federatedTaxonomyService =
						FederatedTaxonomyService.getFederatedTaxonomyService();
				federatedTaxonomyService.initialise();

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
