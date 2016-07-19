package rifServices.statisticalServices;

import rifServices.system.RIFServiceStartupOptions;
import rifGenericLibrary.system.ClassFileLocator;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.businessConceptLayer.Parameter;




import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.*;

//import org.rosuda.JRI.*;


/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class BayesianSmoothingService {

	public static void main(String[] args) {
		BayesianSmoothingService bayesianSmoothingService
			= new BayesianSmoothingService();
		try {
			
			//Specify the executable that will be invoked by the service
			//to call R.			
			StringBuilder commandLineExecutable = new StringBuilder();
			commandLineExecutable.append("C:");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("Program Files");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("R");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("R-3.2.2");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("bin");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("x64");
			commandLineExecutable.append(File.separator);
			commandLineExecutable.append("R");
			//rCommandLinePath.append("R CMD BATCH");
			
			
			
			
			ArrayList<Parameter> parameters = new ArrayList<Parameter>();
			
			//Add parameters related to database access
			Parameter userParameter
				= Parameter.newInstance("user", "kgarwood");
			RIFServiceStartupOptions rifStartupOptions 
				= RIFServiceStartupOptions.newInstance(false, false);
			parameters.add(userParameter);
			
			//Note this is just for testing and will be eliminated later
			Parameter passwordParameter
				= Parameter.newInstance("password", "kgarwood");
			parameters.add(passwordParameter);
			
			Parameter databaseDriverPrefix
				= Parameter.newInstance(
					"db_driver_prefix", 
					rifStartupOptions.getDatabaseDriverPrefix());
			parameters.add(databaseDriverPrefix);
			Parameter databaseHostParameter
				 = Parameter.newInstance(
					"db_host",
					rifStartupOptions.getHost());
			parameters.add(databaseHostParameter);
			Parameter databasePortParameter
				= Parameter.newInstance(
					"db_port",
					rifStartupOptions.getPort());			
			parameters.add(databasePortParameter);
			Parameter databaseNameParameter
				= Parameter.newInstance(
					"db_name",
					rifStartupOptions.getDatabaseName());			
			parameters.add(databaseNameParameter);
			Parameter databaseDriverClassNameParameter
				= Parameter.newInstance(
					"db_driver_class_name",
					rifStartupOptions.getDatabaseDriverClassName());
			parameters.add(databaseDriverClassNameParameter);
					
			//Add parameters related to files used in the executable			
			Parameter sourceCodeFileParameter
				= Parameter.newInstance(
					"r_source_file", 
					"TestSmoothingRoutine.R");
			parameters.add(sourceCodeFileParameter);
			
			StringBuilder outputFilePath = new StringBuilder();
			outputFilePath.append("C:");
			outputFilePath.append(File.separator);
			outputFilePath.append("rif_test");
			outputFilePath.append(File.separator);
			outputFilePath.append(sourceCodeFileParameter.getValue());
			outputFilePath.append("out");
			Parameter rOutputFileParameter
				= Parameter.newInstance(
					"r_out_file", 
					outputFilePath.toString());
			parameters.add(rOutputFileParameter);
			
			//Add parameter related to the model
			Parameter rModelParameter 
				= Parameter.newInstance(
					"r_model", 
					"BYHETCAR");
			parameters.add(rModelParameter);
						
			Parameter studyIDParameter 
				= Parameter.newInstance(
					"study_id", 
					"1");
			parameters.add(studyIDParameter);			
				
			bayesianSmoothingService.initialise(
				commandLineExecutable.toString(),
				parameters,
				BayesianSmoothingService.OperatingSystemType.WINDOWS);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================
	public static enum OperatingSystemType {WINDOWS, UNIX};
	
	// ==========================================
	// Section Properties
	// ==========================================	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public BayesianSmoothingService() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void initialise(
		final String commandLineExecutable,
		final ArrayList<Parameter> parameters,
		final OperatingSystemType operatingSystemType) 
		throws RIFServiceException {
		
		//do validation checks
		checkMissingParameters(parameters);
				
		File rCodeFile = determineSmoothingSourceCodeFile(parameters);
		checkInputOutputFileProperties(
			commandLineExecutable,
			parameters, 
			rCodeFile,
			operatingSystemType);
		checkDatabaseConnectionString(parameters);

		String commandLineInvocation
			= createRCommandLineInvocation(
				commandLineExecutable, 
				parameters);
		
		//try {
			System.out.println("RUN=="+commandLineInvocation+"==");
			/*
			Process process
				= Runtime.getRuntime().exec(commandLineInvocation);	
			int exitValue = process.exitValue();
			System.out.println("Exit value=="+exitValue+"==");
			*/
		//}
		//catch(IOException ioException) {
		//	RIFServiceExceptionFactory rifServiceExceptionFactory
		//		= new RIFServiceExceptionFactory();
		//	rifServiceExceptionFactory.createFileCommandLineRunException(commandLineInvocation);
		//}
	}
	
	private String createRCommandLineInvocation(
		final String commandLineExecutable,
		final ArrayList<Parameter> parameters) {
		
		StringBuilder commandLineInvocation = new StringBuilder();
		commandLineInvocation.append(commandLineExecutable);
		commandLineInvocation.append(" CMD BATCH");
		
		for (Parameter parameter : parameters) {
			commandLineInvocation.append(" -");
			commandLineInvocation.append(parameter.getName());
			commandLineInvocation.append(" ");
			commandLineInvocation.append(parameter.getValue());
		}
		
		return commandLineInvocation.toString();		
	}
	
	private File determineSmoothingSourceCodeFile(
		final ArrayList<Parameter> parameters) {
			
		Parameter rSourceFileParameter
			= Parameter.getParameter("r_source_file", parameters);
			
		StringBuilder fileName = new StringBuilder();
		
		String resourceFilePath
			= ClassFileLocator.getClassRootLocation("rifServices");
		fileName.append(resourceFilePath);
		fileName.append(File.separator);
		fileName.append(rSourceFileParameter.getValue());		
		File file = new File(fileName.toString());
		
		return file;
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void checkMissingParameters(
		final ArrayList<Parameter> parameters) 
		throws RIFServiceException {
		
		//Check for parameters related to database access
		RIFServiceExceptionFactory rifServiceExceptionFactory
			= new RIFServiceExceptionFactory();		
		Parameter userParameter
			= Parameter.getParameter("user", parameters);
		if (userParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("user");
		}
		Parameter passwordParameter
			= Parameter.getParameter("password", parameters);
		if (passwordParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("password");
		}
	
		Parameter databasePrefixParameter
			= Parameter.getParameter("db_driver_prefix", parameters);
		if (databasePrefixParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("db_driver_prefix");
		}				
		Parameter databaseDriverClassNameParameter
			= Parameter.getParameter("db_driver_class_name", parameters);		
		if (databaseDriverClassNameParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("db_driver_class_name");
		}		
		Parameter databaseHostParameter
			= Parameter.getParameter("db_host", parameters);		
		if (databaseHostParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("db_host");
		}			
		Parameter databasePortParameter
			= Parameter.getParameter("db_port", parameters);		
		if (databasePortParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("db_port");
		}			
		Parameter databaseNameParameter
			= Parameter.getParameter("db_name", parameters);		
		if (databaseNameParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("db_name");
		}		
		
		//Check for parameters related to input and output file access
		Parameter rSourceFileParameter
			= Parameter.getParameter("r_source_file", parameters);
		if (rSourceFileParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("r_source_file");
		}
		Parameter rOutputFileParameter
			= Parameter.getParameter("r_out_file", parameters);
		if (rOutputFileParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("r_out_file");
		}	

		//Check for parameters related to model operation
		Parameter rModelParameter
			= Parameter.getParameter("r_model", parameters);
		if (rModelParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("r_model");
		}		
		Parameter studyIDParameter
			= Parameter.getParameter("study_id", parameters);
		if (studyIDParameter == null) {
			throw rifServiceExceptionFactory.createNonExistentParameter("study_id");
		}			
	}
	
	/**
	 * This routine mainly checks that files that are specified exist and have the 
	 * appropriate permissions.  For example, we'd like to verify that the command-line
	 * executable actually is executable.  If it's on Windows, then we have to append
	 * *.exe and check that the file exists.
	 * 
	 * @param parameters
	 * @param rCodeFile
	 * @param operatingSystemType
	 * @throws RIFServiceException
	 */
	private void checkInputOutputFileProperties(
		final String commandLineExecutable,
		final ArrayList<Parameter> parameters, 
		final File rSourceCodeFile,
		final OperatingSystemType operatingSystemType) 
		throws RIFServiceException {

		RIFServiceExceptionFactory rifServiceExceptionFactory
			= new RIFServiceExceptionFactory();

		/**
		 * Check whether the command-line executable file exists and has
		 * executable permissions
		 */
		StringBuilder commandLineExecutableFilePath = new StringBuilder();
		commandLineExecutableFilePath.append(commandLineExecutable);
		if (operatingSystemType == OperatingSystemType.WINDOWS) {
			//append .exe
			commandLineExecutableFilePath.append(".exe");
		}		
		File commandLineExecutableFile 
			= new File(commandLineExecutableFilePath.toString());
		if (commandLineExecutableFile.exists() == false) {
			//Error: command line executable was incorrectly specified
			//and does not exist
			rifServiceExceptionFactory.createNonExistentFile(
				commandLineExecutableFile.getAbsolutePath());
		}
		//Check that the command-line executable has execution permissions
		if (commandLineExecutableFile.canExecute() == false) {
			//Error: command line executable was incorrectly specified
			//and does not exist
			rifServiceExceptionFactory.createFileExecutionProblemException(
				commandLineExecutableFile.getAbsolutePath());
		}
		
		/**
		 * Check that R source code file exists
		 */
		if (rSourceCodeFile.exists() == false) {
			//Error: non-existent file
			rifServiceExceptionFactory.createNonExistentFile(
				rSourceCodeFile.getAbsolutePath());
		}
	}
	
	/**
	 * Assemble database connection parameters together and test whether a JDBC connection
	 * is possible.  This is important for when the db connection parameters are passed to
	 * the R program.
	 * 
	 * @param parameter
	 */
	private void checkDatabaseConnectionString(
		final ArrayList<Parameter> parameters) 
		throws RIFServiceException {


		Parameter databaseDriverPrefix
			= Parameter.getParameter("db_driver_prefix", parameters);
		Parameter databaseDriverClassNameParameter
			= Parameter.getParameter("db_driver_class_name", parameters);		
		Parameter databaseHostParameter
			= Parameter.getParameter("db_host", parameters);		
		Parameter databasePortParameter
			= Parameter.getParameter("db_port", parameters);		
		Parameter databaseNameParameter
			= Parameter.getParameter("db_name", parameters);		
		
		StringBuilder databaseConnectionString = new StringBuilder();
		databaseConnectionString.append(databaseDriverPrefix.getValue());
		databaseConnectionString.append(":");		
		databaseConnectionString.append(":");
		databaseConnectionString.append("//");
		databaseConnectionString.append(databaseHostParameter.getValue());
		databaseConnectionString.append(":");
		databaseConnectionString.append(databasePortParameter.getValue());
		databaseConnectionString.append("/");
		databaseConnectionString.append(databaseNameParameter.getValue());

		Properties databaseProperties = new Properties();
		Parameter userParameter
			= Parameter.getParameter("user", parameters);
		Parameter passwordParameter
				= Parameter.getParameter("password", parameters);
		databaseProperties.setProperty("user", userParameter.getValue());
		databaseProperties.setProperty("password", passwordParameter.getValue());
		System.out.println("User=="+userParameter.getValue()+"==password=="+passwordParameter.getValue()+"==");
		
		/*
		Connection connection = null;
		try {
			//We are only checking if it's possible to make a connection
			//If we can, we assume that when the R program gets the parameters
			//to make a connection string, it will be able to connect with no
			//problem.
			connection
				= DriverManager.getConnection(
					databaseConnectionString.toString());
			connection.close();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			throw rifServiceExceptionFactory.createUnableToUseDBConnectionString(
				databaseConnectionString.toString());
		}
		finally {
			SQLQueryUtility.close(connection);
		}
		*/
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
