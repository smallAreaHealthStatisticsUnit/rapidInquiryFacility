package rifServices.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.util.RIFLogger;
import rifServices.system.files.TomcatBase;
import rifServices.system.files.TomcatFile;

/**
 * Class that holds configuration settings for rif services.  These will appear
 * in <code>RIFServiceStartupProperties.properties</code>, a properties file
 * containing a list of name-value pairs
 */
public class RIFServiceStartupOptions {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();

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

	private String odbcDataSourceName;
	
	/** The server side cache directory. */
	private File serverSideCacheDirectory;

	private String extractDirectory;
	
	private String taxonomyServicesServer;

	private boolean useStrictValidationPolicy;

	private final RIFServiceStartupProperties properties;

	public static RIFServiceStartupOptions newInstance(
			final boolean isWebDeployment,
			final boolean useStrictValidationPolicy) {

		return new RIFServiceStartupOptions(
				isWebDeployment,
				useStrictValidationPolicy);
	}

	public static RIFServiceStartupOptions newInstance(
			final boolean isWebDeployment,
			final boolean useStrictValidationPolicy,
			final ResourceBundle bundle) {

		return new RIFServiceStartupOptions(
				isWebDeployment,
				useStrictValidationPolicy,
				bundle);
	}

	private RIFServiceStartupOptions(
			final boolean isWebDeployment,
			final boolean useStrictValidationPolicy,
			final ResourceBundle bundle) {

		this.isWebDeployment = isWebDeployment;
		this.useStrictValidationPolicy = useStrictValidationPolicy;
		properties = RIFServiceStartupProperties.getInstance(bundle);
		populateOptions();
	}

	private RIFServiceStartupOptions(
		final boolean isWebDeployment,
		final boolean useStrictValidationPolicy) {
		
		this.isWebDeployment = isWebDeployment;
		this.useStrictValidationPolicy = useStrictValidationPolicy;

		// We should be able to read startup properties from
		// a startup properties file
		properties = RIFServiceStartupProperties.getInstance();
		populateOptions();
	}

	private void populateOptions() {
		try {
			databaseDriverClassName = properties.getDatabaseDriverClassName();
			databaseDriverPrefix = properties.getDatabaseDriverPrefix();
			host = properties.getHost();
			port = properties.getPort();
			databaseName = properties.getDatabaseName();
			odbcDataSourceName = properties.getODBCDataSourceName();
			databaseType =  properties.getDatabaseType();
			extractDirectory = properties.getExtractDirectoryName();
			taxonomyServicesServer = properties.getTaxonomyServicesServer();
			isDatabaseCaseSensitive = properties.isDatabaseCaseSensitive();
			sslSupported = properties.isSSLSupported();

			if (sslSupported) {
				rifLogger.info(this.getClass(),
						"RIFServicesStartupOptions -- using SSL debug");
				final boolean useSSLDebug = properties.useSSLDebug();
				if (useSSLDebug) {

					System.setProperty("javax.net.debug", "ssl");
				}

				System.setProperty("javax.net.ssl.trustStore",
						properties.getTrustStore());
				final String trustStorePassword = properties.getTrustStorePassword();

				System.setProperty("javax.net.ssl.trustStorePassword",
				                   trustStorePassword);
			}
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),
				"Error in RIFServiceStartupOptions() constructor", exception);
			throw new RuntimeException(exception);
		}
	}

    public DatabaseType getRifDatabaseType() { // To avoid confusion with
													  // PG/MSSQLAbstractRIFWebServiceResource() etc
		return databaseType;
	}

	public String getOptionalRIfServiceProperty(String propertyName, String defaultValue)
					throws Exception {
		return properties.getOptionalRIfServiceProperty(propertyName, defaultValue);
	}
	
	public Float getOptionalRIfServiceProperty(String propertyName, Float defaultValue)
					throws Exception {
		return properties.getOptionalRIfServiceProperty(propertyName, defaultValue);
	}    	

	public int getOptionalRIfServiceProperty(String propertyName, int defaultValue)
					throws Exception {
		return properties.getOptionalRIfServiceProperty(propertyName, defaultValue);
	}	
	
	public boolean getOptionalRIfServiceProperty(String propertyName, boolean defaultValue)
					throws Exception {
		return properties.getOptionalRIfServiceProperty(propertyName, defaultValue);
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

		return RIFDatabaseProperties.newInstance(
			databaseType,
			isDatabaseCaseSensitive,
			sslSupported);
	}
	
	
	public String getODBCDataSourceName() {
		
		return odbcDataSourceName;	
	}
	
	public String getExtractDirectory() {
		return extractDirectory;
	
	}	
	
	public String getTaxonomyServicesServer() {
		return taxonomyServicesServer;		
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
	 * @param databaseDriverClassName the new database driver
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
	 * @param databaseDriverPrefix the new database driver
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
	 * Gets the record type.
	 *
	 * @return the record type
	 */
	private String getRecordType() {

		return RIFServiceMessages.getMessage("rifServiceStartupOptions.label");
	}

	public String getRIFServiceResourcePath() {

		if (isWebDeployment) {

			rifLogger.info(getClass(), "RIFServiceStartupOptions is web deployment");

			String path = new TomcatFile(new TomcatBase(), ".").pathToClassesDirectory().toString();
			rifLogger.info(getClass(), "Returning path: " + path);

			return path;
		}
		else {
			rifLogger.info(this.getClass(), "RIFServiceStartupOptions is NOT web deployment");

			return (new File(".")).getAbsolutePath()
			       + File.separator
			       + "target"
			       + File.separator
			       + "classes";
		}
	}

	public boolean useStrictValidationPolicy() {
		return useStrictValidationPolicy;
	}

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
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseDriverLabel);
			errorMessages.add(errorMessage);			
		}

		if (fieldValidationUtility.isEmpty(databaseDriverPrefix)) {
			String databaseDriverLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseDriverPrefix.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseDriverLabel);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(host)) {
			String hostLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.host.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					hostLabel);
			errorMessages.add(errorMessage);
		}

		if (fieldValidationUtility.isEmpty(port)) {
			String portLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.port.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					portLabel);
			errorMessages.add(errorMessage);
		}
		
		if (fieldValidationUtility.isEmpty(databaseName)) {
			String databaseNameLabel
				= RIFServiceMessages.getMessage("rifServiceStartupOptions.databaseName.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					databaseNameLabel);
			errorMessages.add(errorMessage);
		}
		
		if (!errorMessages.isEmpty()) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_STARTUP_OPTIONS, 
					errorMessages);
			rifLogger.error(this.getClass(), "RIFServiceStartupOptions error", rifServiceException);
			throw rifServiceException;
		}
	}
}
