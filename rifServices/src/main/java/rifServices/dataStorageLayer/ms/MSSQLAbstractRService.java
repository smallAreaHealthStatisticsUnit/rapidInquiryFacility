package rifServices.dataStorageLayer.ms;

import rifServices.businessConceptLayer.CalculationMethod;
import rifGenericLibrary.businessConceptLayer.Parameter;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.time.*;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import rifGenericLibrary.util.RIFLogger;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public abstract class MSSQLAbstractRService {

	// ==========================================
	// Section Constants
	// ==========================================
	public static enum OperatingSystemType {WINDOWS, UNIX};
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String odbcDataSourceName;
	private String userID;
	private String password;
	
	private ArrayList<Parameter> parameters;	
	private ArrayList<String> parametersToVerify;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLAbstractRService() {
		parameters = new ArrayList<Parameter>();
		
		parametersToVerify = new ArrayList<String>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	protected void addParameter(final String name, final String value) {
		Parameter parameter = Parameter.newInstance(name, value);
		parameters.add(parameter);
	}
	
	protected void addParameters(final ArrayList<Parameter> _parameters) {
		this.parameters.addAll(_parameters);		
	}
	
	protected void addParameterToVerify(final String parameterToVerify) {
		parametersToVerify.add(parameterToVerify);
	}

	protected void addParameterToVerify(final ArrayList<String> _parametersToVerify) {
		parametersToVerify.addAll(_parametersToVerify);
	}
		
	protected void setUser(
		final String userID, 
		final String password) {
		
		this.userID = userID;
		this.password = password;
	}
	
	protected void setODBCDataSourceName(final String odbcDataSourceName) {
		this.odbcDataSourceName = odbcDataSourceName;
	}	
	
	protected void setCalculationMethod(final CalculationMethod calculationMethod) {
		addParameter("model", calculationMethod.getName());
	}
	
	//Fetch parameters array list
	protected ArrayList<Parameter> getParameterArray() {
	
		addParameter("odbcDataSource", odbcDataSourceName);
		addParameter("userID", userID);
		addParameter("password", password);
		
		return(parameters);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	/*
	 * Logging R console output to RIFLogger
	 */
	static class LoggingConsole implements RMainLoopCallbacks {
		// ==========================================
		// Section Constants
		// ==========================================
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
		
		LoggingConsole(Logger log) { // Constructor
			this.log = log; // Not used!
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
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
