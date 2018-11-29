package org.sahsu.rif.services.system;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.graphics.RIFTilesGenerator;

/**
 * Create PNG tiles for geolevels with more than 5000 areas.
 * <p>
 * Runs RIFTilesGenerator as a separate thread
 * </p>
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
final class RIFService {

	private final static RIFService THE_INSTANCE = new RIFService(); // Not static
	private final RIFLogger logger = RIFLogger.getLogger();

	private boolean running;
	private Path scriptPath;

	static RIFService instance() { // Not static

		return THE_INSTANCE;
	}

	private RIFService() {

		// Prevent instantiation. This should never happen, of course.
		if (THE_INSTANCE != null) {

			throw new IllegalStateException("Service cannot be instantiated");
		}
	}

	public void start() {

		logger.info(getClass(), "Starting the RIF Middleware Service");
		
		if (!isRunning()) {

			try {

				running = true;
				logger.info(getClass(), "RIF Middleware Service started");
				
				try {
					RIFServiceStartupOptions rifServiceStartupOptions =
						RIFServiceStartupOptions.newInstance(true, false);
					RIFTilesGenerator rifTilesGenerator = new RIFTilesGenerator();
					String username=rifServiceStartupOptions.getOptionalRIfServiceProperty("tileGeneratorUsername", "null");
					String password=rifServiceStartupOptions.getOptionalRIfServiceProperty("tileGeneratorPassword", "null");
			
					if (username.equals("null") || password.equals("null")) {
						logger.info(getClass(), 
							"RIF Middleware Tile Generator cannot be run: tileGeneratorUsername or tileGeneratorPassword not set in RIFServiceStartupProperties.properties");
					}
					else {
						rifTilesGenerator.initialise(
							username,
							password,
							rifServiceStartupOptions);
						
						ExecutorService exec = Executors.newSingleThreadExecutor();
						exec.execute(rifTilesGenerator);
						logger.info(getClass(), "RIF Middleware Tile Generator running");
					}
				} catch (Exception exception) {
					String errorMsg = "Error running the RIF Middleware Tile Generator";
					logger.error(getClass(), errorMsg, exception);
				}
			} catch (Exception exception) {
				String errorMsg = "Couldn't start the RIF Middleware Service";
				logger.error(getClass(), errorMsg, exception);
			}
			
		}
		
	}

	void stop() {
		logger.info(getClass(), "Shutdown requested for RIF Middleware Service");
	}

	boolean isRunning() {
		return running;
	}
}
