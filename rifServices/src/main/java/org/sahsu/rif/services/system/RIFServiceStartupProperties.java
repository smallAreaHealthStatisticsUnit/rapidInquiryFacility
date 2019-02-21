package org.sahsu.rif.services.system;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.fileformats.AppResourceBundle;
import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Reads values from the RIFServiceStartupProperties.properties file.
 */

public final class RIFServiceStartupProperties {

	private static final String STARTUP_PROPERTIES_FILE = "RIFServiceStartupProperties.properties";

	private static ResourceBundle resourceBundle = null;
	protected static RIFLogger rifLogger = RIFLogger.getLogger();
 
	private static Hashtable<String, Integer> parameterWarnings = new Hashtable<>();

	/**
	 * This is the standard way to get an instance of a
	 * {@link RIFServiceStartupProperties}. The values will be loaded from
	 * the standard file.
	 *
	 * @return the {@link RIFServiceStartupProperties}
	 */
	public static RIFServiceStartupProperties getInstance() {

		return new RIFServiceStartupProperties();
	}

	/**
	 * This version is mainly for testing. Pass in a populated
	 * {@link ResourceBundle}.
	 * @param bundle the settings to use
	 * @return the {@link RIFServiceStartupProperties}.
	 */
	public static RIFServiceStartupProperties getInstance(ResourceBundle bundle) {

		return new RIFServiceStartupProperties(bundle);
	}

	/*
	 * Test cases can call this constructor and pass in a ResourceBundle.
	 */
	private RIFServiceStartupProperties(ResourceBundle rb) {

		resourceBundle = rb;
	}

	private RIFServiceStartupProperties() {

		if (resourceBundle == null) {
			AppResourceBundle bundle = new AppResourceBundle(
					AppFile.getServicesInstance(STARTUP_PROPERTIES_FILE));
			String msg = String.format("%s: loaded resource bundle %s%n",
			                           getClass().getSimpleName(),
			                           bundle.appFile().asString());
			rifLogger.info(getClass(), msg);
			resourceBundle = bundle.bundle();

		}
	}

	boolean isSSLSupported() {
		String property
			= getMandatoryRIfServiceProperty("database.isSSLSupported");
		return Boolean.valueOf(property);		
	}
	
	String getDatabaseDriverClassName() {
		return getMandatoryRIfServiceProperty("database.driverClassName");
	}
   
	String getDatabaseDriverPrefix() {
		return getMandatoryRIfServiceProperty("database.jdbcDriverPrefix");
	}
	
	String getHost() {
		return getMandatoryRIfServiceProperty("database.host");
	}
	
	String getPort() {
		return getMandatoryRIfServiceProperty("database.port");
	}

	String getDatabaseName() {
		return getMandatoryRIfServiceProperty("database.databaseName");
	}

	String getExtractDirectoryName() {
		return getMandatoryRIfServiceProperty("extractDirectory");
	} 
	
	String getODBCDataSourceName() {
		return getOptionalRIfServiceProperty("odbcDataSourceName");
	} 

	private String getMandatoryRIfServiceProperty(String propertyName) {
		String propertyValue;
		try {
			propertyValue=getProperty(propertyName);
		}
		catch(Exception exception) {
			rifLogger.error(getClass().getSimpleName(),
				"Error fetching mandatory property: " + propertyName, exception);
			throw exception;
		}

		return propertyValue;		
	}
	
	private void updateParameterWarnings(String propertyName) {
		if (parameterWarnings.containsKey(propertyName)) {
			parameterWarnings.put(propertyName, parameterWarnings.get(propertyName)+1);
		}
		else {
			parameterWarnings.put(propertyName, 1);
			rifLogger.info(getClass().getSimpleName(),
				"Unable to fetch optional property [MissingResourceException]: " + propertyName);
		}
	}
	
	
	private String getOptionalRIfServiceProperty(String propertyName) {
		String propertyValue=null;
		try {
			propertyValue=getProperty(propertyName);
		}
		catch(MissingResourceException exception) { 
			updateParameterWarnings(propertyName);
		}
		catch(Exception exception) {
			rifLogger.error(getClass().getSimpleName(),
				"Unable to fetch optional property: " + propertyName, exception);
			throw exception;
		}

		return propertyValue;		
	}

	boolean getOptionalRIfServiceProperty(String propertyName, boolean defaultValue) {
		boolean propertyValue=defaultValue;
		String stringValue=null;
		try {
			stringValue=getProperty(propertyName);
			if (stringValue == null) {
				propertyValue=defaultValue;
			}
			else if (stringValue.toUpperCase().equals("TRUE") ||
			    stringValue.toUpperCase().equals("YES")) {
				propertyValue=true;
			}
			else if (stringValue.toUpperCase().equals("FALSE") ||
			    stringValue.toUpperCase().equals("NO")) {
				propertyValue=false;
			}
			else {
				propertyValue=defaultValue;
			}
		}
		catch(MissingResourceException exception) { 
			updateParameterWarnings(propertyName);
		}
		catch(Exception exception) {
			rifLogger.error(getClass().getSimpleName(),
				"Unable to fetch optional property: " + propertyName, exception);
			throw exception;
		}
		
		rifLogger.info(getClass().getSimpleName(),
			"getOptionalRIfServiceProperty(Boolean) " + propertyName + ": " + propertyValue + "; string: " +
			stringValue);

		return propertyValue;		
	}
	
	String getOptionalRIfServiceProperty(String propertyName, String defaultValue) {
		String propertyValue=defaultValue;
		try {
			propertyValue=getProperty(propertyName);
		}
		catch(MissingResourceException exception) { 
			updateParameterWarnings(propertyName);
		}
		catch(Exception exception) {
			rifLogger.error(getClass().getSimpleName(),
				"Unable to fetch optional property: " + propertyName, exception);
			throw exception;
		}

		return propertyValue;		
	}

	int getOptionalRIfServiceProperty(String propertyName, int defaultValue)
					throws Exception {
		int propertyValue=defaultValue;
		try {
			propertyValue=Integer.parseInt(StringUtils.trim(getProperty(propertyName)));
		}
		catch(MissingResourceException exception) { 
			updateParameterWarnings(propertyName);
		}
		catch(Exception exception) { // java.util.MissingResourceException
			rifLogger.error(getClass().getSimpleName(),
				"Unable to fetch optional property: " + propertyName, exception);
			throw exception;
		}

		return propertyValue;		
	}
	
	Float getOptionalRIfServiceProperty(String propertyName, Float defaultValue) {
		Float propertyValue=defaultValue;
		try {
			propertyValue=Float.parseFloat(StringUtils.trim(getProperty(propertyName)));
		}
		catch(MissingResourceException exception) { 
			updateParameterWarnings(propertyName);
		}
		catch(Exception exception) {
			rifLogger.debug(getClass().getSimpleName(),
				"Unable to fetch optional property: " + propertyName);
			throw exception;
		}

		return propertyValue;		
	}
	
	String getTaxonomyServicesServer() {

		return getOptionalRIfServiceProperty("taxonomyServicesServer");		
	}

	public DatabaseType getDatabaseType() {
		
		DatabaseType databaseType
			= DatabaseType.UNKNOWN;
		
		String property
			= getMandatoryRIfServiceProperty("database.databaseType");
		if (property != null) {
			property = property.toUpperCase();
			if (property.equals("POSTGRESQL")) {
				databaseType
					= DatabaseType.POSTGRESQL;
			}
			else if (property.equals("SQLSERVER")) {
				databaseType
					= DatabaseType.SQL_SERVER;  			
			}   		
		}
		
		return databaseType;

	}
	
	boolean isDatabaseCaseSensitive() {
		String property
			= getMandatoryRIfServiceProperty("database.isCaseSensitive");
		Boolean result
			= Boolean.valueOf(property);
		return result;
	}

	boolean useSSLDebug() {
		String property
			= getMandatoryRIfServiceProperty("database.useSSLDebug");
		Boolean result
			= Boolean.valueOf(property);
		return result; 	
	}
	
	String getTrustStore() {
		String property
			= getMandatoryRIfServiceProperty("database.sslTrustStore");
		return property; 	
	}
	
	String getTrustStorePassword() {
		String property
			= getMandatoryRIfServiceProperty("database.sslTrustStorePassword");
		return property; 		
	}
	
	/**
	 * Gets the message.
	 *
	 * @param key the key
	 * @return the message
	 */
	public String getProperty(
		final String key) {

		if (resourceBundle != null) {
			return (resourceBundle.getString(key));
		}
		return null;
	}

}
