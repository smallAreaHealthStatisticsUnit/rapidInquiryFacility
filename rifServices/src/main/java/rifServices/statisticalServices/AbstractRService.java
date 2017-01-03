package rifServices.statisticalServices;

import rifServices.system.RIFServiceStartupOptions;

import rifServices.businessConceptLayer.CalculationMethod;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.ClassFileLocator;

import java.util.ArrayList;
import java.io.File;
import java.util.Map;


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
	private File rScriptFile;
	private String userID;
	private String password;
	private OperatingSystemType operatingSystemType;
	private ArrayList<Parameter> parameters;
	
	private ArrayList<String> parametersToVerify;
	
	private CalculationMethod calculationMethod;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRService() {
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
	
	protected void setRScriptFileName(final String rScriptFileName) {
		
		String resourceFilePath
			= ClassFileLocator.getClassRootLocation("rifServices");
		StringBuilder fullPath = new StringBuilder();
		fullPath.append(resourceFilePath);
		fullPath.append(File.separator);
		fullPath.append(rScriptFileName);		
		rScriptFile = new File(fullPath.toString());		
	}
	
	protected void setCalculationMethod(final CalculationMethod calculationMethod) {
		this.calculationMethod = calculationMethod;
	}
	
	protected void setOperatingSystemType(final OperatingSystemType operatingSystemType) {
		//Might be useful if someone names their R file with a space in it,
		//which is something that may be common in Windows.
		this.operatingSystemType = operatingSystemType;
	}
	
	protected String generateCommandLineExpression() {
		StringBuilder expression = new StringBuilder();
		
		
		Map<String, String> environmentVariables = System.getenv();
		String rHomeValue = environmentVariables.get("R_HOME");
		expression.append("C:\\\"Program Files\"\\R\\R-3.2.2\\bin\\x64");
		expression.append(File.separator);
		expression.append("RScript ");
		expression.append(rScriptFile.getAbsolutePath());
		expression.append(" ");
		for (Parameter parameter : parameters) {
			expression.append(" --");
			expression.append(parameter.getName());
			expression.append("=");
			expression.append(parameter.getValue());			
		}
		
		expression.append(" --odbc_data_source="+odbcDataSourceName);
		expression.append(" --user_id="+userID);
		expression.append(" --password="+password);
		
		return expression.toString();		
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
