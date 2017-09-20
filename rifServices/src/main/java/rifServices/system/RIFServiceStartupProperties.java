package rifServices.system;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.dataStorageLayer.DatabaseType;

import rifGenericLibrary.util.RIFLogger;

import java.text.Collator;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Map;

import java.io.*;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;


/**
 * Reads values from the RIFServiceStartupProperties.properties file.
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
 * (8)            Section Accessors
 * (9)            Section Mutators
 * (6)            Section Validation
 * (7)            Section Errors
 * (5)            Section Interfaces
 * (4)            Section Overload
 *
*/

public final class RIFServiceStartupProperties {

    // ==========================================
    // Section Constants
    // ==========================================
	/** The Constant resourceBundle. */
    public static final ResourceBundle resourceBundle;

    /** The collator. */
    private static Collator collator = Collator.getInstance(Locale.getDefault());
	
	protected static RIFLogger rifLogger = RIFLogger.getLogger();
	
    // ==========================================
    // Section Properties
    // ==========================================
    /**
     * the context help cursor
     */

    // ==========================================
    // Section Construction
    // ==========================================

    static {
		resourceBundle=initRIFServiceStartupProperties();
    }

    // ==========================================
    // Section Accessors
    // ==========================================
	
   /**
     * Constructor function.
     *
     * @return the ResourceBundle
     */
	private static ResourceBundle initRIFServiceStartupProperties() {		
		ResourceBundle resourceBundle1=null;
		ResourceBundle resourceBundle2=null;
		Map<String, String> environmentalVariables = System.getenv();
		String dirName1;
		String dirName2;
		String fileName="RIFServiceStartupProperties.properties";
		String catalinaHome = environmentalVariables.get("CATALINA_HOME");
		if (catalinaHome != null) {
//
// Search for RIFServiceStartupProperties.properties in:
//
// %CATALINA_HOME%\
// %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\
//
			dirName1=catalinaHome + "\\conf";
			dirName2=catalinaHome + "\\webapps\\rifServices\\WEB-INF\\classes";
		}
		else {
			rifLogger.warning("rifServices.system.RIFServiceStartupProperties", 
				"RIFServiceStartupProperties: CATALINA_HOME not set in environment"); 
			dirName1="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\conf";
			dirName2="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes";
		}   	
		InputStreamReader reader = null;
		FileInputStream fis = null;
		try {
			File file = new File(dirName1, fileName);
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            resourceBundle1 = new PropertyResourceBundle(reader);
			rifLogger.info("rifServices.system.RIFServiceStartupProperties", 
				"RIFServiceStartupProperties: using: " + dirName1 + "\\" + fileName);
			System.out.println("RIFServiceStartupProperties: using: " + dirName1 + "\\" + fileName);
		} 
		catch (IOException ioException) {
			try {
				File file = new File(dirName2, fileName);
				fis = new FileInputStream(file);
				reader = new InputStreamReader(fis);
				resourceBundle2 = new PropertyResourceBundle(reader);
			
				rifLogger.info("rifServices.system.RIFServiceStartupProperties", 
					"RIFServiceStartupProperties: using: " + dirName2 + "\\" + fileName);
				System.out.println("RIFServiceStartupProperties: using: " + dirName2 + "\\" + fileName);
			} 
			catch (IOException ioException2) {
				rifLogger.error("rifServices.system.RIFServiceStartupProperties", 
					"RIFServiceStartupProperties error for files: " + 
						 dirName1 + "\\" + fileName + " and " +  dirName2 + "\\" + fileName, 
					ioException2);
			}
		} 
		finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (reader != null) {
					reader.close();
				}
			}	
			catch (IOException ioException3) {
				rifLogger.error("rifServices.system.RIFServiceStartupProperties", 
					"RIFServiceStartupProperties error for files: " + 
						 dirName1 + "\\" + fileName + " and " +  dirName2 + "\\" + fileName, 
					ioException3);
			}
		}
		
		if (resourceBundle1 != null) {
			return resourceBundle1;
		}
		else if (resourceBundle2 != null) {
			return resourceBundle2;
		}
		else { // Should never get here
			try {
				RIFServiceExceptionFactory exceptionFactory
					= new RIFServiceExceptionFactory();
				throw exceptionFactory.createFileReadingProblemException();	
			}
			catch (RIFServiceException rifServiceException) {
				rifLogger.error("rifServices.system.RIFServiceStartupProperties", 
					"RIFServiceStartupProperties error for files: " + 
						 dirName1 + "\\" + fileName + " and " +  dirName2 + "\\" + fileName, 
					rifServiceException);
				return null;
			}
		}
	}
   /**
     * Gets the collator.
     *
     * @return the collator
     */	
    public static Collator getCollator() {	  
    	
	  Collator result = (Collator) collator.clone();
	  return result;
    }

    public static boolean isSSLSupported() {
    	String property
    		= getProperty("database.isSSLSupported");
    	return Boolean.valueOf(property);    	
    }
    
    public static String getDatabaseDriverClassName() {
    	return getProperty("database.driverClassName");
    }
   
    public static String getDatabaseDriverPrefix() {
    	return getProperty("database.jdbcDriverPrefix");
    }
    
    public static String getHost() {
    	return getProperty("database.host");
    }
    
    public static String getPort() {
    	return getProperty("database.port");    	
    }

    public static String getDatabaseName() {
    	return getProperty("database.databaseName");    	
    }
	
    public static String getServerSideCacheDirectory() {
    	return getProperty("cache");    	
    }

    public static String getWebApplicationDirectory() {
    	return getProperty("webApplicationDirectory");    	
    }
    
    public static String getRScriptDirectory() {
    	return getProperty("rScriptDirectory");    	
    }
    
    public static String getExtractDirectoryName() {
    	return getProperty("extractDirectory");    	
    }
    
    public static String getExtraDirectoryForExtractFiles() {
    	return getProperty("extraDirectoryForExtractFiles");    	
    }
    
    public static DatabaseType getDatabaseType() {
    	
    	DatabaseType databaseType
    		= DatabaseType.UNKNOWN;
    	
    	String property
    		= getProperty("database.databaseType").toUpperCase();
    	if (property != null) {
    		property = property.toUpperCase();
    		
    		Collator collator
    			= RIFGenericLibraryMessages.getCollator();
    		if (collator.equals(property, "postgresql")) {
    			databaseType
    				= DatabaseType.POSTGRESQL;
    		}
    		else if (collator.equals(property, "sqlServer")) {
    			databaseType
    				= DatabaseType.SQL_SERVER;  			
    		}   		
    	}
    	
    	return databaseType;

    }
    
    public static boolean isDatabaseCaseSensitive() {
    	String property
    		= getProperty("database.isCaseSensitive");
    	Boolean result
    		= Boolean.valueOf(property);
    	return result;
    }
    
    public static int getMaximumMapAreasAllowedForSingleDisplay() {
    	String property
    		= getProperty("maximumMapAreasAllowedForSingleDisplay");
    	Integer maximumValue = 0;
    	try {
    		maximumValue = Integer.valueOf(property);
    	}
    	catch(Exception exception) {
    		maximumValue = 0;
    	}
    	
    	return maximumValue;
    }

    public static boolean useSSLDebug() {
    	String property
    		= getProperty("database.useSSLDebug");
    	Boolean result
			= Boolean.valueOf(property);
    	return result; 	
    }
    
    public static String getTrustStore() {
    	String property
			= getProperty("database.sslTrustStore");
    	return property; 	
    }
    
    public static String getTrustStorePassword() {
    	String property
    		= getProperty("database.sslTrustStorePassword");
    	return property; 	    }
    
    public static String getODBCDataSourceName() {
    	String property
			= getProperty("odbcDataSourceName");
    	return property;    	
    }
    
    
    /**
     * Gets the message.
     *
     * @param key the key
     * @return the message
     */
    public static String getProperty(
    	final String key) {

    	if (resourceBundle != null) {
            return (resourceBundle.getString(key));
        }
        return null;
    }

    public static ResourceBundle getResourceBundle() {
    	
        return resourceBundle;
    }

    // ==========================================
    // Section Mutators
    // ==========================================

	
	/**
     * Sets the collator.
     *
     * @param _collator the new collator
     */
    static public void setCollator(
    	final Collator _collator) {

    	collator = _collator;
	}

    // ==========================================
    // Section Validation
    // ==========================================

    // ==========================================
    // Section Errors
    // ==========================================

    // ==========================================
    // Section Interfaces
    // ==========================================

    // ==========================================
    // Section Overload
    // ==========================================

}
