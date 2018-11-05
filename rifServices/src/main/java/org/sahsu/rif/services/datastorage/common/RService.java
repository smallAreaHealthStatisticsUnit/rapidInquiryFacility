package org.sahsu.rif.services.datastorage.common;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.CalculationMethod;

public interface RService {

	void sourceRScript(Rengine rengine, Path script) throws Exception;
	
	void addParameter(String name, String value);

	void addParameters(List<Parameter> _parameters);

	void setUser(String userID, String password);

	void setODBCDataSourceName(String odbcDataSourceName);

	void setCalculationMethod(CalculationMethod calculationMethod);

	//Fetch parameters array list
	List<Parameter> getParameters();

	/*
	 * Logging R console output to RIFLogger
	 */
	class LoggingConsole implements RMainLoopCallbacks {

		private static Logger log; 	// Not used!
									// [Keeps RMainLoopCallbacks happy which uses
									// java.util.logging.Logger and not
									// org.apache.logging.log4j.Logger;]
		private static final RIFLogger rifLogger = RIFLogger.getLogger();
		private static String lineSeparator = System.getProperty("line.separator");
		private static int logCalls=0;
		private static int rFlushCount=0;
		private static StringBuilder message = new StringBuilder();
		private Instant start=Instant.now();
		private Instant end;

		public LoggingConsole(Logger log) { // Constructor
			LoggingConsole.log = log; // Not used!
		}

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
			addMessage(lineSeparator + "rBusy[" + Integer.toString(which) + "]" + lineSeparator);
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
			end=Instant.now();

			rFlushCount++;
			rifLogger.info(this.getClass(),
				"rFlushConsole[" + Integer.toString(rFlushCount) + "] calls: " + Integer.toString(logCalls) +
				", length: " + Integer.toString(message.length()) +
				", time period: " + Duration.between(start, end).toString() +
				lineSeparator + message.toString());
			message.delete(1, message.length());
			logCalls=0;
			start=Instant.now();
		}

		public void rLoadHistory(Rengine re, String filename) {
		}

		public void rSaveHistory(Rengine re, String filename) {
		}
	}
}
