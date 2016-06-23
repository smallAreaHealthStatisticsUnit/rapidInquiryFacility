package rifServices.system;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.dataStorageLayer.DatabaseType;

import java.text.Collator;
import java.util.Locale;
import java.util.ResourceBundle;


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
 * Copyright 2014 Imperial College London, developed by the Small Area
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
    	
        resourceBundle = ResourceBundle.getBundle("RIFServiceStartupProperties");
    }

    // ==========================================
    // Section Accessors
    // ==========================================

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
