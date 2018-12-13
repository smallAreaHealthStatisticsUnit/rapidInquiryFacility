package org.sahsu.rif.services.system;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	private static RIFTilesGenerator rifTilesGenerator = null;
	private static ExecutorService exec = null;

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
					rifTilesGenerator = new RIFTilesGenerator();
					String username=rifServiceStartupOptions.getOptionalRIfServiceProperty("tileGeneratorUsername", "null");
					if (username.equals("null")) {
						logger.info(getClass(), 
							"RIF Middleware Tile Generator cannot be run: tileGeneratorUsername not set in RIFServiceStartupProperties.properties");
					}
					else {
						rifTilesGenerator.initialise(
							username,
							rifServiceStartupOptions);
						exec = Executors.newSingleThreadExecutor();
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
	
		if (rifTilesGenerator != null && exec != null && !exec.isShutdown()) {	
			logger.info(getClass(), "Shutdown requested for RIF Middleware Service");
			try {
				rifTilesGenerator.doStop();
				Thread.sleep(1000); // 1 sec
				exec.shutdownNow();
				if (!exec.awaitTermination(60, TimeUnit.SECONDS)) {
					logger.error(getClass(), "RIFervice executor thread did not terminate");
				}
				rifTilesGenerator=null;
				if (exec.isShutdown()) {
					logger.info(getClass(), "Shutdown requested for RIF Middleware Service: tile generator shutdown completed");
				}
				else {
					logger.warning(getClass(), "Shutdown requested for RIF Middleware Service: tile generator shutdown is incomplete");
				}
			} catch (InterruptedException interruptedException) {
				logger.error(getClass(), "RIFervice executor thread INTERRUPTEDEXCEPTION", interruptedException);
				// (Re-)Cancel if current thread also interrupted
				exec.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
		else if (rifTilesGenerator != null && exec != null && exec.isShutdown()) {	
			logger.info(getClass(), "Shutdown requested for RIF Middleware Service: tile generator is already shutdown");
		}
		else {
			logger.info(getClass(), "Shutdown requested for RIF Middleware Service: tile generator was never started");
		}
	
	}

	boolean isRunning() {
		return running;
	}
}
