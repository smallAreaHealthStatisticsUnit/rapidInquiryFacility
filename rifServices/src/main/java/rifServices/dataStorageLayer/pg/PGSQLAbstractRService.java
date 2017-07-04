package rifServices.dataStorageLayer.pg;

import rifServices.businessConceptLayer.CalculationMethod;
import rifGenericLibrary.businessConceptLayer.Parameter;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;


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

public abstract class PGSQLAbstractRService {

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

	private String rFilePath;
	private String rScriptProgramPath;
	
	private ArrayList<Parameter> parameters;	
	private ArrayList<String> parametersToVerify;
	private CalculationMethod calculationMethod;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLAbstractRService() {
		parameters = new ArrayList<Parameter>();
		
		parametersToVerify = new ArrayList<String>();
		rFilePath = "";	
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
		this.calculationMethod = calculationMethod;
		
		addParameter("r_model", calculationMethod.getName());
	}
	
	//Generate param string array
	protected String[] generateParameterArray() {
	
		String[] parametersArray = new String[13];
		int i = 0;
		for (Parameter parameter : parameters) {
			parametersArray[i] = parameter.getValue();
			i++;
		}	
		parametersArray[10] = odbcDataSourceName;
		parametersArray[11] = userID;
		parametersArray[12] = password;

		//same order of args as in the old batch file
		/*
		0		"jdbc:postgresql", //db_driver_prefix
		1		"localhost", //dbHost
		2		"5432", //dbPort
		3		"sahsuland_dev", //dbName
		4		"db_driver_class_name", //org.postgresql.Driver
		5		"14", //studyID
		6		"MY_NEW_INVESTIGATION", //investigationName
		7		"NONE", //covariate_name 
		8		"9", //investigationId
		9		"het_r_procedure", //r_model
		10		"odbcDataSource", //PostgreSQL30
		11		"dwmorley", //userID
		12		"*******" //password
		 */
		return parametersArray;
	}

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	/*
	 * Logging R console output in Tomcat
	 */
	static class LoggingConsole implements RMainLoopCallbacks {
		private Logger log;

		LoggingConsole(Logger log) {
			this.log = log;
		}

		public void rWriteConsole(Rengine re, String text, int oType) {
			log.info(String.format("rWriteConsole: %s", text));
		}

		public void rBusy(Rengine re, int which) {
			log.info(String.format("rBusy: %s", which));
		}

		public void rShowMessage(Rengine re, String message) {
			log.info(String.format("rShowMessage: %s",  message));
		}

		public String rReadConsole(Rengine re, String prompt, int addToHistory) {
			return null;
		}

		public String rChooseFile(Rengine re, int newFile) {
			return null;
		}

		public void rFlushConsole(Rengine re) {
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
