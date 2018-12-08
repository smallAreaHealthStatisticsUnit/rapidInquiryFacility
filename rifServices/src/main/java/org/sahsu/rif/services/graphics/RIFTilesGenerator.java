package org.sahsu.rif.services.graphics;

import java.sql.Connection;
import java.sql.SQLException;

import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.datastorage.common.RIFTiles;
import org.sahsu.rif.services.datastorage.common.BaseSQLManager;

/**
 * Create PNG tiles for geolevels with more than 5000 areas.
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class RIFTilesGenerator implements Runnable {

	private static final int SLEEP_TIME = 200;

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static RIFServiceStartupOptions rifServiceStartupOptions = null;
	private static Connection connection = null;
	
	public RIFTilesGenerator() { // Dummy constructor
	}
	
	/** 
	 * Initialize function; called before run
	 *
	 * @param String username
	 * @param String password
	 * @param RIFServiceStartupOptions for the extract directory
	 *
	 * @throws RIFServiceException RIF error
	 * @throws SQLException Unable to connect to database
	 * @throws ClassNotFoundException Unable to load JFBC driver
     */		
	public void initialise( 
		final String username, 
		final String password,
		final RIFServiceStartupOptions rifServiceStartupOptions)
		throws RIFServiceException, SQLException, ClassNotFoundException {
			
		this.rifServiceStartupOptions=rifServiceStartupOptions;
		Class.forName(rifServiceStartupOptions.getDatabaseDriverClassName()); // Load JDBC driver
		BaseSQLManager baseSQLManager = new BaseSQLManager(rifServiceStartupOptions);
		this.connection=baseSQLManager.createConnection(username, password, 
			false 	/* isFirstConnectionForUser */,
			true 	/* isReadOnly */);
	}

	/** 
	 * Required Thread run function
	 */
	public void run() {
		try {			
			RIFTiles rifTiles = new RIFTiles(rifServiceStartupOptions);
			rifTiles.generateTiles(connection); // Generated 913 tiles for: 3 geolevels in 00:14:07.567 (EWS2011)
			rifLogger.info(this.getClass(), "Tile generator run() Finished OK!");
		}		
		catch(SQLException sqlException) { // Finally {} has failed closing cursors or releasing resultsets: 
										   // usually a fatal database error
			rifLogger.info(this.getClass(), "Tile generator run() UNHANDLED SQL EXCEPTION: " + sqlException.getMessage());
		}		
		catch(RIFServiceException rifServiceException) { // Also catch RIFSQLException super class
			rifLogger.info(this.getClass(), "Tile generator run() FAILED: " + rifServiceException.getMessage());
			rifServiceException.printErrors();
		}		
	}
	
}