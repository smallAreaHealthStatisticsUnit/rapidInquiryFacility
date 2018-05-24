package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.generic.datastorage.DatabaseType;

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

public final class RIFDataLoaderToolStartupProperties {

    // ==========================================
    // Section Constants
    // ==========================================
	/** The Constant resourceBundle. */
    public static final ResourceBundle resourceBundle;

    // ==========================================
    // Section Properties
    // ==========================================


    // ==========================================
    // Section Construction
    // ==========================================

    static {
    	
        resourceBundle = ResourceBundle.getBundle("RIFDataLoaderToolStartupProperties");
    }

    // ==========================================
    // Section Accessors
    // ==========================================

    public static String getTestUserID() {
    	return getProperty("testUser.id");
    }
    
    public static String getTestUserPassword() {
    	return getProperty("testUser.password");    	
    }
    
    public static String getDatabasePasswordFilePath() {
    	return getProperty("databasePasswordFile");    	
    }

    public static DatabaseConnectionsConfiguration createStartupDBConfiguration() {
    	
    	DatabaseConnectionsConfiguration dbConfiguration
    		= DatabaseConnectionsConfiguration.newInstance();
    	
    	dbConfiguration.setDatabasePasswordFilePath(getProperty("databasePasswordFile"));
    	
    	String databaseType = getProperty("databaseType");
    	if (databaseType.equals("pg")) {
    		dbConfiguration.setDatabaseType(DatabaseType.POSTGRESQL);  		
    		dbConfiguration.setDatabaseDriverClassName(getProperty("pg.driverClassName"));
    		dbConfiguration.setDatabaseDriverPrefix(getProperty("pg.jdbcDriverPrefix"));
    		dbConfiguration.setHostName(getProperty("pg.host"));
    		dbConfiguration.setPortName(getProperty("pg.port"));
    		dbConfiguration.setDatabaseName(getProperty("pg.databaseName")); 		
    	}
    	else {
    		//assume it is 'ms' for SQL Server
    		dbConfiguration.setDatabaseType(DatabaseType.SQL_SERVER);  		
    		dbConfiguration.setDatabaseDriverClassName(getProperty("ms.driverClassName"));
    		dbConfiguration.setDatabaseDriverPrefix(getProperty("ms.jdbcDriverPrefix"));
    		dbConfiguration.setHostName(getProperty("ms.host"));
    		dbConfiguration.setPortName(getProperty("ms.port"));
    		dbConfiguration.setDatabaseName(getProperty("ms.databaseName"));    		
    	}
    	
    	return dbConfiguration;
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
