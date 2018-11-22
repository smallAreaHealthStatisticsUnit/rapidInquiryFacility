package org.sahsu.stats.service.logging;

import java.time.Duration;
import java.time.Instant;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.util.StatisticsLogger;

/*
 * Logging R console output to StatisticsLogger
 */
public class LoggingConsole implements RMainLoopCallbacks {

	private static final StatisticsLogger logger = StatisticsLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static int logCalls=0;
	private static int rFlushCount=0;
	private static StringBuilder message = new StringBuilder();
	private Instant start=Instant.now();

	private void addMessage(String text) {

		logCalls++;
		message.append(text);
	}

	public void rWriteConsole(Rengine re, String text, int oType) {
		long millis = Duration.between(start, Instant.now()).toMillis();

		if (oType == 1) { // Error/Warning
			addMessage("R Error/Warning/Notice: " + text);
		}
		else {
			addMessage(text);
		}

		if (millis > 1000) { // Force flush every second
			this.rFlushConsole(re);
		}
	}

	public void rBusy(Rengine re, int which) {

		String msg = lineSeparator + "R Engine " + (which == 1 ? "entered" : "left")
		             + " the busy state" + lineSeparator;
		addMessage(msg);
	}

	public void rShowMessage(Rengine re, String message) {
		addMessage(lineSeparator + "rShowMessage: " + message + lineSeparator);
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		return null;
	}

	public String rChooseFile(Rengine re, int newFile) {
		return null;
	}

	public void rFlushConsole(Rengine re) {

		final Instant end = Instant.now();

		rFlushCount++;
		logger.info(this.getClass(),
		               "rFlushConsole[" + Integer.toString(rFlushCount) + "] calls: " + Integer.toString(logCalls) +
		               ", length: " + Integer.toString(message.length()) +
		               ", time period: " + Duration.between(start, end).toString() +
		               lineSeparator + message.toString());
		message.delete(1, message.length());
		logCalls=0;
		start=Instant.now();
	}

	public void rLoadHistory(Rengine re, String filename) {}

	public void rSaveHistory(Rengine re, String filename) {}
}
