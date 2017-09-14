package rifServices.system;

import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.businessConceptLayer.Parameter;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;



/**
 * Class that holds configuration settings for rif services.  These will appear
 * in <code>RIFServiceStartupProperties.properties</code>, a properties file
 * containing a list of name-value pairs
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public final class RIFServiceStartupOptions {

	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private int maximumMapAreasAllowedForSingleDisplay;
	private RIFLogger rifLogger = RIFLogger.getLogger();

	private boolean isWebDeployment;
	
	private String databaseDriverClassName;
	
	/** The database driver. */
	private String databaseDriverPrefix;
	
	/** The host. */
	private String host;
	
	/** The port. */
	private String port;
	
	/** The database name. */
	private String databaseName;
	
	
	private DatabaseType databaseType;
	private boolean isDatabaseCaseSensitive;
	private boolean sslSupported;
	private boolean useSSLDebug;
	private String trustStore;
	private String trustStorePassword;
	
	private String rifServiceClassDirectoryPath;
	
	private String odbcDataSourceName;
	
	/** The server side cache directory. */
	private File serverSideCacheDirectory;
		
	private String webApplicationDirectory;

	private String rScriptDirectory;
	
	
	private String extractDirectory;
	
	private String extraExtractFilesDirectoryPath;
	
	private boolean useStrictValidationPolicy;
	
	// ==========================================
	// Section Construction
	// ==========================================

	
	/**
	 * Instantiates a new RIF service startup options.
	 */
	private RIFServiceStartupOptions(
		final boolean isWebDeployment,
		final boolean useStrictValidationPolicy) {
		
		this.isWebDeployment = isWebDeployment;
		this.useStrictValidationPolicy = useStrictValidationPolicy;
		
		//We should be able to read startup properties from
		//a startup properties file
		
		databaseDriverClassName 
			= RIFServiceStartupProperties.getDatabaseDriverClassName();
		databaseDriverPrefix
			= RIFServiceStartupProperties.getDatabaseDriverPrefix();
		host
			= RIFServiceStartupProperties.getHost();
		port
			= RIFServiceStartupProperties.getPort();
		databaseName
			= RIFServiceStartupProperties.getDatabaseName();
		webApplicationDirectory
			= RIFServiceStartupProperties.getWebApplicationDirectory();
		rScriptDirectory
			= RIFServiceStartupProperties.getRScriptDirectory();

		
		odbcDataSourceName
			= RIFServiceStartupProperties.getODBCDataSourceName();
		
		extractDirectory
			= RIFServiceStartupProperties.getExtractDirectoryName();
		
		maximumMapAreasAllowedForSingleDisplay
			= RIFServiceStartupProperties.getMaximumMapAreasAllowedForSingleDisplay();
		
		isDatabaseCaseSensitive
			= RIFServiceStartupProperties.isDatabaseCaseSensitive();
		sslSupported
			= RIFServiceStartupProperties.isSSLSupported();

		if (sslSupported) {
			rifLogger.info(this.getClass(), "RIFServicesStartupOptions -- using SSL debug");
			useSSLDebug
				= RIFServiceStartupProperties.useSSLDebug();
			if (useSSLDebug) {			
				System.setProperty(
					"javax.net.debug", 
					"ssl");				
			}
			
			trustStore
				= RIFServiceStartupProperties.getTrustStore();
			System.setProperty(
				"javax.net.ssl.trustStore", 
				trustStore);
			trustStorePassword
				= RIFServiceStartupProperties.getTrustStorePassword();
			
			System.setProperty(
				"javax.net.ssl.trustStorePassword", 
				trustStorePassword);
		}
		
		extraExtractFilesDirectoryPath
			= RIFServiceStartupProperties.getExtraDirectoryForExtractFiles();
	}

	public static RIFServiceStartupOptions newInstance(
		final boolean isWebDeployment,
		final boolean useStrictValidationPolicy) {
		
		RIFServiceStartupOptions rifServiceStartupOptions
			= new RIFServiceStartupOptions(
				isWebDeployment,
				useStrictValidationPolicy);
		return rifServiceStartupOptions;		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	public String getExtraExtractFilesDirectoryPath() {
		return extraExtractFilesDirectoryPath;
	}
	
	public ArrayList<Parameter> extractParameters() {
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		
		Parameter databaseDriverPrefixParameter
			= Parameter.newInstance(
				"db_driver_prefix", 
				databaseDriverPrefix);		
		parameters.add(databaseDriverPrefixParameter);

		Parameter databaseHostParameter
			= Parameter.newInstance(
				"db_host",
				host);
		parameters.add(databaseHostParameter);

		Parameter databasePortParameter
			= Parameter.newInstance(
				"db_port",
				port);			
		parameters.add(databasePortParameter);

		Parameter databaseNameParameter
			= Parameter.newInstance(
				"db_name",
				databaseName);			
		parameters.add(databaseNameParameter);

		Parameter databaseDriverClassNameParameter
			= Parameter.newInstance(
				"db_driver_class_name",
				databaseDriverClassName);
		parameters.add(databaseDriverClassNameParameter);
				
		return parameters;
	}
	
	public RIFDatabaseProperties getRIFDatabaseProperties() {
				
		RIFDatabaseProperties rifDatabaseProperties
			= RIFDatabaseProperties.newInstance(
				databaseType, 
				isDatabaseCaseSensitive,
				sslSupported);
		
		return rifDatabaseProperties;
	}
	
	
	public String getODBCDataSourceName() {
		
		return odbcDataSourceName;	
	}
	
	public String getExtractDirectory() {
		return extractDirectory;
		
	}
	
	/**
	 * Gets the database driver class name
	 *
	 * @return the database driver
	 */
	public String getDatabaseDriverClassName() {
		
		return databaseDriverClassName;
	}

	/**
	 * Sets the database driver.
	 *
	 * @param databaseDriver the new database driver
	 */
	public void setDatabaseDriverClassName(
		final String databaseDriverClassName) {

		this.databaseDriverClassName = databaseDriverClassName;
	}

	/**
	 * Gets the database driver class name
	 *
	 * @return the database driver
	 */
	public String getDatabaseDriverPrefix() {
		
		return databaseDriverPrefix;
	}

	/**
	 * Sets the database driver.
	 *
	 * @param databaseDriver the new database driver
	 */
	public void setDatabaseDriverPrefix(
		final String databaseDriverPrefix) {

		this.databaseDriverPrefix = databaseDriverPrefix;
	}
	
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {

		return host;
	}

	/**
	 * Sets the host.
	 *
	 * @param host the new host
	 */
	public void setHost(
		final String host) {

		this.host = host;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public String getPort() {

		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(
		final String port) {

		this.port = port;
	}

	/**
	 * Gets the database name.
	 *
	 * @return the database name
	 */
	public String getDatabaseName() {

		return databaseName;
	}

	/**
	 * Sets the database name.
	 *
	 * @param databaseName the new database name
	 */
	public void setDatabaseName(
		final String databaseName) {

		this.databaseName = databaseName;
	}
	
	/**
	 * Gets the server side cache directory.
	 *
	 * @return the server side cache directory
	 */
	public File getServerSideCacheDirectory() {

		return serverSideCacheDirectory;
	}
	
	/**
	 * Sets the server side cache directory.
	 *
	 * @param serverSideCacheDirectory the new server side cache directory
	 */
	public void setServerSideCacheDirectory(
		final File serverSideCacheDirectory) {

		this.serverSideCacheDirectory = serverSideCacheDirectory;
	}
	
	/**
	 * Gets the record type.
	 *
	 * @return the record type
	 */
	private String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("rifServiceStartupOptions.label");
		return recordType;
	}
	
	
	public String getWebApplicationDirectory() {
		return webApplicationDirectory;
	}

	
	public void setRIFServiceClassDirectory(
		final String rifServiceClassDirectoryPath) {
		
		this.rifServiceClassDirectoryPath = rifServiceClassDirectoryPath;		
	}
	
	public String getRIFServiceResourcePath()
		throws RIFServiceException {
		/*
		String currentDirectoryPath = null;
		if (rifServiceClassDirectoryPath == null) {
			currentDirectoryPath = (new File(".")).getAbsolutePath();			
		}
		else {
			currentDirectoryPath = (new File(rifServiceClassDirectoryPath)).getAbsolutePath();			
		}
		*/

		StringBuilder path = new StringBuilder();
		//path.append(currentDirectoryPath);
		//path.append(File.separator);

		if (isWebDeployment) {
			rifLogger.info(this.getClass(), "RIFServiceStartupOptions is web deployment");
			Map<String, String> environmentalVariables = System.getenv();


//			String catalineHome = "C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5";
			
			String catalinaHome = environmentalVariables.get("CATALINA_HOME");
			if (catalinaHome == null) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.INVALID_STARTUP_OPTIONS, 
						"CATALINA_HOME not set in the environment");
				rifLogger.error(this.getClass(), "RIFServiceStartupOptions error", rifServiceException);
				throw rifServiceException;
			}
			/*
			String[] tokens2 = catalinaHome.split("\\");
			if (tokens2 == null) {
				rifLogger.info(this.getClass(), "tokens are null");
			}
			else {
				for (String token2 : tokens2) {
					rifLogger.info(this.getClass(), "token222:"+token2+"==");
				}	
			}
			
			String[] tokens3 = catalinaHome.split("\\");
			if (tokens3 == null) {
				rifLogger.info(this.getClass(), "tokens are null");
			}
			else {
				for (String token3 : tokens3) {
					rifLogger.info(this.getClass(), "token333:"+token3+"==");
				}	
			}
			*/
					
			String catalinaHomeDirectoryPath = environmentalVariables.get("CATALINA_HOME");
			rifLogger.info(this.getClass(), "Get CATALINA_HOME="+catalinaHomeDirectoryPath);
			catalinaHomeDirectoryPath = catalinaHomeDirectoryPath.replace("\\", "\\\\");
			path.append(catalinaHomeDirectoryPath);
			path.append("\\\\");
			path.append("webapps");
			path.append("\\\\");
			path.append("rifServices");
			path.append("\\\\");
			path.append("WEB-INF");	
			path.append("\\\\");
			path.append("classes");		
			
		}
		else {
			rifLogger.info(this.getClass(), "RIFServiceStartupOptions is NOT web deployment");
			path.append((new File(".")).getAbsolutePath());
			path.append(File.separator);
			path.append("target");
			path.append(File.separator);
			path.append("classes");			
		}
				
		return path.toString();
	}
	
	public void setWebApplicationDirectory(
		final String webApplicationDirectory) {
		this.webApplicationDirectory = webApplicationDirectory;
	}
	
	public String getRScriptDirectory() {
		return rScriptDirectory;
	}
	
	public void setRScriptDirectory(
		final String rScriptDirectory) {
		
		this.rScriptDirectory = rScriptDirectory;
	}
			
	public String getReadOnlyDatabaseConnectionString() {
		
		StringBuilder urlText = new StringBuilder();
		urlText.append(databaseDriverPrefix);
		urlText.append(":");
		urlText.append("//");
		urlText.append(host);
		urlText.append(":");
		urlText.append(port);
		urlText.append("/");
		urlText.append(databaseName);
		
		return urlText.toString();
	}	
	
	public String getWriteOnlyDatabaseConnectionString() {
		
		StringBuilder urlText = new StringBuilder();
		urlText.append(databaseDriverPrefix);
		urlText.append(":");
		urlText.append("//");
		urlText.append(host);
		urlText.append(":");
		urlText.append(port);
		urlText.append("/");
		urlText.append(databaseName);
		
		return urlText.toString();
	}	
	
	
	public int getMaximumMapAreasAllowedForSingleDisplay() {
		return maximumMapAreasAllowedForSingleDisplay;
	}
	
	public void setMaximumMapAreasAllowedForSingleDisplay(
		final int maximumMapAreasAllowedForSingleDisplay) {
		
		this.maximumMapAreasAllowedForSingleDisplay = maximumMapAreasAllowedForSingleDisplay;
	}
	
	
	public boolean useStrictValidationPolicy() {
		return useStrictValidationPolicy;
	}
	
	public void setUseStrictValidationPolicy(
		final boolean useStrictValidationPolicy) {
		this.useStrictValidationPolicy = useStrictValidationPolicy;
	}
	
	public boolean useSSLDebug() {
		return useSSLDebug;
	}

	public void setUseSSLDebug(
		final boolean useSSLDebug) {

		this.useSSLDebug = useSSLDebug;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(
		final String trustStorePassword) {

		this.trustStorePassword = trustStorePassword;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/**
	 * Check security violations.
	 *
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		String recordType = getRecordType();

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		if (databaseDriverClassName != null) {
			String databaseDriverLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseDriver.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				databaseDriverLabel,
				databaseDriverClassName);
		}

		if (databaseDriverPrefix != null) {
			String databaseDriverLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseDriverPrefix.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				databaseDriverLabel,
				databaseDriverPrefix);
		}

		
		
		if (host != null) {
			String hostLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.host.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				hostLabel,
				host);
		}
		
		if (port != null) {
			String portLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.port.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				portLabel,
				port);
		}
	
		if (databaseName != null) {
			String databaseNameLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseName.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				databaseNameLabel,
				databaseName);
		}		
	}
	
	/**
	 * Check errors.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkErrors() 
		throws RIFServiceException {

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		String recordType = getRecordType();
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		if (fieldValidationUtility.isEmpty(databaseDriverClassName)) {
			String databaseDriverLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseDriverClassName.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseDriverLabel);
			errorMessages.add(errorMessage);			
		}

		if (fieldValidationUtility.isEmpty(databaseDriverPrefix)) {
			String databaseDriverLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseDriverPrefix.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseDriverLabel);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(host)) {
			String hostLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.host.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					hostLabel);
			errorMessages.add(errorMessage);
		}

		if (fieldValidationUtility.isEmpty(port)) {
			String portLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.port.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					portLabel);
			errorMessages.add(errorMessage);
		}
		
		if (fieldValidationUtility.isEmpty(databaseName)) {
			String databaseNameLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseName.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseNameLabel);
			errorMessages.add(errorMessage);
		}
		
		if (errorMessages.isEmpty() == false) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_STARTUP_OPTIONS, 
					errorMessages);
			rifLogger.error(this.getClass(), "RIFServiceStartupOptions error", rifServiceException);
			throw rifServiceException;
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
