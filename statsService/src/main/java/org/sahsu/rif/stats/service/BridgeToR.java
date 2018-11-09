package org.sahsu.rif.stats.service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.stats.service.logging.LoggingConsole;

/**
 * Provides the statistics services for the Rapid Enquiry Facility.
 */
public final class BridgeToR {

	// Singleton because there can only be one R engine running in a JVM.
	private static final BridgeToR THE_INSTANCE = new BridgeToR();
	private final RIFLogger logger = RIFLogger.getLogger();
	private static final List<String> R_SCRIPTS = new ArrayList<>();
	static {

		R_SCRIPTS.add("OdbcHandler.R");
		R_SCRIPTS.add("JdbcHandler.R");
		R_SCRIPTS.add("Statistics_Common.R");
		R_SCRIPTS.add("Statistics_JRI.R");
		R_SCRIPTS.add("CreateWindowsScript.R");
		R_SCRIPTS.add("performRiskAnal.R");
		R_SCRIPTS.add("performSmoothingActivity.R");
	}

	private boolean running;
	private Rengine rEngine;
	private Path scriptPath;

	static BridgeToR instance() {

		return THE_INSTANCE;
	}

	private BridgeToR() {

		// Prevent instantiation. This should never happen, of course.
		if (THE_INSTANCE != null) {

			throw new IllegalStateException("Service cannot be instantiated");
		}
	}

	void start() {

		if (!isRunning()) {

			try {

				rEngine = Rengine.getMainEngine();
				if (rEngine == null) {

					String[] rArgs = { "--vanilla" };
					rEngine = new Rengine(rArgs, false, new LoggingConsole());
				}

				if (!rEngine.waitForR()) {

					logger.warning(getClass(),
					                  "Cannot load the R engine (probably already loaded)");
				}

				performEngineChecks();
				scriptPath = AppFile.getStatisticsInstance(".").pathToClassesDirectory();
				loadRScripts();

				running = true;
				logger.info(getClass(), "Statistics Service started");

			} catch (Exception exception) {

				String errorMsg = "Couldn't start the Statistics Service";
				logger.error(getClass(), errorMsg, exception);
			}
		}
	}

	void stop() {

		logger.info(getClass(), "Shutdown requested for Statistics Service");
		rEngine.end();
		rEngine = null;
	}

	boolean isRunning() {

		return running;
	}

	private void performEngineChecks() {

		Rengine.DEBUG = 10;
		rEngine.eval("Rpid <- Sys.getpid()");
		REXP rPid = rEngine.eval("Rpid");
		logger.info(getClass(), "Rengine Started" +
		                        "; Rpid: " + rPid.asInt() +
		                        "; JRI version: " + Rengine.getVersion() +
		                        "; thread ID: " + Thread.currentThread().getId());
		//Check library path
		rEngine.eval("rm(list=ls())"); //just in case!
		rEngine.eval("print(.libPaths())");

		// Session Info
		rEngine.eval("print(sessionInfo())");
	}

	private void loadRScripts() {

		for (String s : R_SCRIPTS) {

			loadRScript(scriptPath.resolve(s));
		}
	}

	private void loadRScript(Path script) {

		if (script.toFile().exists()) {

			String scriptString = script.toString();

			// Need to double-escape Windows path separator, or things get confused when we pass
			// the file to R.
			if (SystemUtils.IS_OS_WINDOWS) {

				scriptString = scriptString.replace("\\","\\\\");
				logger.info(this.getClass(), "Source(" + File.separator + "): '"
				                                + scriptString + "'");
			}
			else {
				logger.info(this.getClass(), "Source: '" + scriptString + "'");
			}

			rEngine.eval("source('" + scriptString + "')");
			logger.info(this.getClass(), "Done: '" + scriptString + "'");
		}
		else {

			logger.error(getClass(),"R script: '" + script + "' does not exist");
		}
	}
}
