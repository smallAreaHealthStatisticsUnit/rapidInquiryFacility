package org.sahsu.rif.stats.service;

import org.sahsu.rif.generic.util.TaxonomyLogger;

/**
 * Provides the statistics services for the Rapid Enquiry Facility.
 */
public final class Service {

	// Singleton because there can only be one R engine running in a JVM.
	private static final Service THE_INSTANCE = new Service();
	private final TaxonomyLogger logger = TaxonomyLogger.getLogger();

	private boolean running;

	static Service instance() {

		return THE_INSTANCE;
	}

	private Service() {

		// Prevent instantiation. This should never happen, of course.
		if (THE_INSTANCE != null) {

			throw new IllegalStateException("Service cannot be instantiated");
		}
	}

	void start() {

		if (!isRunning()) {
			try {

				/*
				   Start the R engine here
				 */

				running = true;

			} catch (Exception exception) {

				String errorMsg = "Couldn't start the statistics service";
				logger.error(getClass(), errorMsg, exception);
			}
		}
	}

	void stop() {

		/*
		   And stop the R engine here.
		 */
	}

	boolean isRunning() {

		return running;
	}
}
