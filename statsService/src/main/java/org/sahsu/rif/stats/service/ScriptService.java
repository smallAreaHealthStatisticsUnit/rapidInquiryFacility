package org.sahsu.rif.stats.service;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.Parameters;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.stats.service.logging.LoggingConsole;

/**
 * Provides the link to R functions for the Rapid Inquiry Facility's Statistics Service.
 */
final class ScriptService {

	// Singleton because there can only be one R engine running in a JVM.
	private static final ScriptService THE_INSTANCE = new ScriptService();
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

	private static final String lineSeparator = System.lineSeparator();

	private boolean running;
	private Rengine engine;
	private Path scriptPath;

	static ScriptService instance() {

		return THE_INSTANCE;
	}

	private ScriptService() {

		// Prevent instantiation. This should never happen, of course.
		if (THE_INSTANCE != null) {

			throw new IllegalStateException("Service cannot be instantiated");
		}
	}

	void start() {

		if (!isRunning()) {

			try {

				engine = Rengine.getMainEngine();
				if (engine == null) {

					String[] rArgs = { "--vanilla" };
					engine = new Rengine(rArgs, false, new LoggingConsole());
				}

				if (!engine.waitForR()) {

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
		engine.end();
		engine = null;
	}

	boolean isRunning() {

		return running;
	}

	int runScript(Parameters parameters) {

		// Set connection details and parameters
		StringBuilder logMsg = new StringBuilder();
		for (Parameter parameter : parameters.getParameters()) {

			String name = parameter.getName();
			String value = parameter.getValue();

			switch (name) {
				case "password":
					// Hide password
					logMsg.append(name).append("=XXXXXXXX").append(lineSeparator);
					engine.assign(name, value);
					break;
				case "covariate_name":
					logMsg.append("names.adj.1=").append(value).append(lineSeparator);
					engine.assign("names.adj.1", value);
					logMsg.append("adj.1=").append(getRAdjust(value)).append(lineSeparator);
					engine.assign("adj.1", getRAdjust(value));
					break;
				default:
					logMsg.append(name).append("=").append(value).append(lineSeparator);
					engine.assign(name, value);
					break;
			}
		}

		logger.info(getClass(), "R parameters: " + lineSeparator
		                           + logMsg.toString());


		// We do either Risk Analysis or Smoothing
		if (isRiskAnalysis(parameters)) {

			logger.info(getClass(), "Calling Risk Analysis R function");
			engine.eval("returnValues <- runRRiskAnalFunctions()");
		} else {

			logger.info(getClass(), "Calling Disease Mapping R function");
			engine.eval("returnValues <- runRSmoothingFunctions()");
		}

		int exitValue;
		String rErrorTrace="No R error tracer (see Tomcat log)";
		REXP exitValueFromR = engine.eval("as.integer(returnValues$exitValue)");
		if (exitValueFromR != null) {

			exitValue = exitValueFromR.asInt();
		} else {

			logger.warning(this.getClass(), "JRI R ERROR: exitValueFromR (returnValues$exitValue)"
			                                + " is NULL");
			exitValue = 1;
		}

		REXP errorTraceFromR = engine.eval("returnValues$errorTrace");
		if (errorTraceFromR != null) {

			String[] strArr=errorTraceFromR.asStringArray();
			StringBuilder strBuilder = new StringBuilder();

			for (final String aStrArr : strArr) {
				strBuilder.append(aStrArr).append(lineSeparator);
			}
			int index;
			String toReplace="'";
			while ((index = strBuilder.lastIndexOf(toReplace)) != -1) {

				// Replace ' with " to reduce JSON parse errors
				strBuilder.replace(index, index + toReplace.length(), "\"");
			}

			rErrorTrace = strBuilder.toString();
		} else {

			logger.warning(getClass(),
			                  "JRI R ERROR: errorTraceFromR (returnValues$errorTrace) is NULL");
		}

		return 0;
	}

	private void performEngineChecks() {

		Rengine.DEBUG = 10;
		engine.eval("Rpid <- Sys.getpid()");
		REXP rPid = engine.eval("Rpid");
		logger.info(getClass(), "Rengine Started" +
		                        "; Rpid: " + rPid.asInt() +
		                        "; JRI version: " + Rengine.getVersion() +
		                        "; thread ID: " + Thread.currentThread().getId());
		//Check library path
		engine.eval("rm(list=ls())"); //just in case!
		engine.eval("print(.libPaths())");

		// Session Info
		engine.eval("print(sessionInfo())");
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

			engine.eval("source('" + scriptString + "')");
			logger.info(this.getClass(), "Done: '" + scriptString + "'");
		}
		else {

			logger.error(getClass(),"R script: '" + script + "' does not exist");
		}
	}

	private String getRAdjust(String covar) {

		String name = covar.toUpperCase();
		if (!name.equals("NONE")) {
			return "TRUE";
		} else {
			return "FALSE";
		}
	}

	boolean isRiskAnalysis(Parameters parameters) {

		return parameters.stream().filter(
				p -> p.getName().equals("studyType"))
				       .anyMatch(
				       		p -> p.getValue().equals("riskAnalysis"));
	}
}
