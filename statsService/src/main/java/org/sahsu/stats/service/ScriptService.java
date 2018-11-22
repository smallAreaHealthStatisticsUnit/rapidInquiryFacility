package org.sahsu.stats.service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.SystemUtils;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.Parameters;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.util.StatisticsLogger;
import org.sahsu.stats.service.logging.LoggingConsole;

/**
 * Provides the link to R functions for the Rapid Inquiry Facility's Statistics Service.
 * A singleton because, using JRI, we can only have one instance of the R engine running per JVM.
 */
final class ScriptService {

	// Singleton because there can only be one R engine running in a JVM.
	private static final ScriptService THE_INSTANCE = new ScriptService();
	private final StatisticsLogger logger = StatisticsLogger.getLogger();
	private static final List<String> R_STARTUP_SCRIPTS = new ArrayList<>();
	static {

		R_STARTUP_SCRIPTS.add("Statistics_Common.R");
		R_STARTUP_SCRIPTS.add("CreateWindowsScript.R");
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

		logger.info(getClass(), "Starting the Statistics Service");

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

	Response runScript(Parameters parameters) {

		logger.info(getClass(), "In runScript");

		assignParametersToR(parameters);

		// This one can't be loaded on startup because it needs some of the parameters we've
		// just set
		loadRScript("Statistics_JRI.R");

		// Load appropriate script according to the database type
		if (dbIsPostgres(parameters)) {

			loadRScript("JdbcHandler.R");
		} else {

			loadRScript("OdbcHandler.R");
		}

		// We do either Risk Analysis or Smoothing
		if (isRiskAnalysis(parameters)) {

			loadRScript("performRiskAnal.R");
			logger.info(getClass(), "Calling Risk Analysis R function");
			engine.eval("returnValues <- runRRiskAnalFunctions()");
		} else {

			loadRScript("performSmoothingActivity.R");
			logger.info(getClass(), "Calling Disease Mapping R function");
			engine.eval("returnValues <- runRSmoothingFunctions()");
		}

		ResponseBuilder builder = handleResponse();

		return builder.build();
	}

	private ResponseBuilder handleResponse() {

		// If the exit value is null, that's an error in itself. Zero is OK, any other number
		// is an error.
		int exitValue;
		REXP exitValueFromR = engine.eval("as.integer(returnValues$exitValue)");
		if (exitValueFromR != null) {

			exitValue = exitValueFromR.asInt();
		} else {

			logger.warning(this.getClass(),
			               "JRI R ERROR: exitValueFromR (returnValues$exitValue):"
			                                + " received NULL, expected integer");
			exitValue = 1;
		}

		REXP outputFromR = engine.eval("returnValues$errorTrace");
		String formattedOutputFromR = "";
		if (outputFromR != null) {

			String[] strArr = outputFromR.asStringArray();
			StringBuilder strBuilder = new StringBuilder();

			for (final String aStrArr : strArr) {

				strBuilder.append(aStrArr).append(lineSeparator);
			}

			// Replace ' with " to reduce JSON parsing errors
			formattedOutputFromR = strBuilder.toString().replaceAll("'", "\"");
		} else {

			// Similarly, if the misnamed errorTrace is null, we consider it worthy of a warning.
			logger.warning(getClass(),
			                  "JRI R WARNING: outputFromR (returnValues$errorTrace) is NULL");
		}

		ResponseBuilder builder;
		if (exitValue > 0) {

			builder = Response.serverError();
		} else {

			builder = Response.ok();
		}

		builder.entity(formattedOutputFromR).type(MediaType.APPLICATION_JSON);
		return builder;
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

		for (String s : R_STARTUP_SCRIPTS) {

			loadRScript(s);
		}
	}

	private void loadRScript(String script) {

		Path pathToScript = scriptPath.resolve(script);
		String scriptString = pathToScript.toString();
		logger.info(getClass(), "Loading R script " + scriptString);
		if (pathToScript.toFile().exists()) {

			// Need to double-escape Windows path separator, or things get confused when we pass
			// the file to R.
			if (SystemUtils.IS_OS_WINDOWS) {

				scriptString = scriptString.replace("\\","\\\\");
				logger.info(this.getClass(), "Source(" + File.separator + "): '"
				                                + scriptString + "'");
			} else {
				logger.info(this.getClass(), "Source: '" + scriptString + "'");
			}

			engine.eval("source('" + scriptString + "')");
			logger.info(this.getClass(), "Done: '" + scriptString + "'");
		}
		else {

			logger.error(getClass(),"R script: '"
			                        + scriptPath.resolve(scriptString).toString()
			                        + "' does not exist");
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

	private boolean dbIsPostgres(Parameters parameters) {

		return parameters.stream().filter(
				p -> p.getName().equals("databaseType"))
				       .anyMatch(
						       p -> p.getValue().equals(DatabaseType.POSTGRESQL.getShortName()));
	}

	private void assignParametersToR(final Parameters parameters) {
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
	}
}
