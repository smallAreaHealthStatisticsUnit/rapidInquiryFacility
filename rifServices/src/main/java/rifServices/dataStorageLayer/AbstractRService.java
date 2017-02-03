package rifServices.dataStorageLayer;


import rifServices.businessConceptLayer.CalculationMethod;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.util.FilePathCleaner;

import java.util.ArrayList;
import java.util.Map;
import java.io.*;
import java.util.Date;


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

public abstract class AbstractRService {

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

	
	//private String rScriptFileName;
	private String rFilePath;
	private String rScriptProgramPath;
	
	
	private OperatingSystemType operatingSystemType;
	private ArrayList<Parameter> parameters;
	
	private ArrayList<String> parametersToVerify;
	private ArrayList<String> commandLineComponents;
	
	private CalculationMethod calculationMethod;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRService() {
		parameters = new ArrayList<Parameter>();
		
		parametersToVerify = new ArrayList<String>();
		commandLineComponents = new ArrayList<String>();
		rFilePath = "";
		String separatorCharacter = "\\\\";
		rScriptProgramPath 
			= FilePathCleaner.correctWindowsPathForSpaces(
					separatorCharacter, 
					generateDefaultRScriptProgramPath());
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

	
	protected String getRFilePath() {
		return rFilePath;
	}
	
	/*
	 * This refers to the path of the R program we want to run (eg: R-based Bayesian smoothing routine)
	 * @TODO KLG: for some reason, when we generate a batch file, we have to put a double back slash
	 * into the path of the R program. 
	 */
	protected void setRFilePath(
		final String rFilePath) {

		this.rFilePath
			= FilePathCleaner.correctWindowsPathForSpaces(
				"\\\\", 
				rFilePath);
		System.out.println("AbstractRService setRScriptFileName 2 cleanedPath=="+this.rFilePath+"==");		
	}

	protected String getRScriptProgramPath() {
		return this.rScriptProgramPath;		
	}
	
	protected void setRScriptProgramPath(
		final String rScriptProgramPath) {
		
		String separatorCharacter = System.getProperty("file.separator");

		this.rScriptProgramPath
			= FilePathCleaner.correctWindowsPathForSpaces(
				separatorCharacter, 
				rScriptProgramPath);
		System.out.println("AbstractRService setRScriptFileName 3 cleanedPath==" + this.rScriptProgramPath + "==");			
	}
	
	private String generateDefaultRScriptProgramPath() {
	

		StringBuilder mainCommand = new StringBuilder();
		
		Map<String, String> environmentVariables = System.getenv();
		
		String rHomeValue = environmentVariables.get("R_HOME");
		System.out.println("R HOME IS=="+rHomeValue+"==");
		mainCommand.append(rHomeValue);
		mainCommand.append(File.separator);
		mainCommand.append("bin");
		mainCommand.append(File.separator);
		mainCommand.append("x64");		
		mainCommand.append(File.separator);
		mainCommand.append("RScript");

		return mainCommand.toString();
	}
		
	
	protected void setCalculationMethod(final CalculationMethod calculationMethod) {
		this.calculationMethod = calculationMethod;
		
		addParameter("r_model", calculationMethod.getName());
	}
	
	protected void setOperatingSystemType(final OperatingSystemType operatingSystemType) {
		//Might be useful if someone names their R file with a space in it,
		//which is something that may be common in Windows.
		this.operatingSystemType = operatingSystemType;
	}
	
	protected String generateCommandLineExpression() {

		commandLineComponents.clear();
		
		StringBuilder mainCommand = new StringBuilder();
		mainCommand.append(getRScriptProgramPath());
		mainCommand.append(" ");
		mainCommand.append(getRFilePath());
		commandLineComponents.add(mainCommand.toString());
			
		for (Parameter parameter : parameters) {
			
			StringBuilder parameterPhrase = new StringBuilder();
			parameterPhrase.append(" --");
			parameterPhrase.append(parameter.getName());
			parameterPhrase.append("=");
			parameterPhrase.append(parameter.getValue());			
			commandLineComponents.add(parameterPhrase.toString());
		}		
		
		commandLineComponents.add(" --odbc_data_source=" + odbcDataSourceName);
		commandLineComponents.add(" --user_id=" + userID);
		commandLineComponents.add(" --password=" + password);
		
		
		StringBuilder expression = new StringBuilder();
		for (int i = 0; i < commandLineComponents.size(); i++) {
			if (i != 0) {
				expression.append(" ");
			}
			expression.append(commandLineComponents.get(i));			
		}
		
		return expression.toString();
	}

	protected File createBatchFile(
		final String directoryName,
		final String baseFileName) 
		throws IOException {

		StringBuilder fileName = new StringBuilder();
		fileName.append(directoryName);
		fileName.append(File.separator);
		fileName.append(baseFileName);
		fileName.append("_");
		fileName.append(RIFGenericLibraryMessages.getDatePhrase(new Date(System.currentTimeMillis())));
		fileName.append(".bat");
		
		File file = new File(fileName.toString());
		
		OutputStreamWriter outputStreamWriter
			= new OutputStreamWriter(new FileOutputStream(file), "Cp1252");
		BufferedWriter writer = new BufferedWriter(outputStreamWriter);
		System.out.println("Writing batch file=="+generateCommandLineExpression());
		writer.write(generateCommandLineExpression());
		
		writer.flush();
		writer.close();
		
		file.setExecutable(true);
		return file;
	}
	

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void validateParametersToVerify() 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		for (String parameterToVerify : parametersToVerify) {
			Parameter expectedParameter
				= Parameter.getParameter(parameterToVerify, parameters);
			if (expectedParameter == null) {
		
				String errorMessage
					= RIFServiceMessages.getMessage(
						"",
						parameterToVerify);
			}
		}

		/*
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError, errorMessages);
			throw rifServiceException;			
		}
		*/
		
	}
	
	protected void validateCommandLineExpressionComponents() 
		throws RIFServiceException {

		validateParametersToVerify();
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
