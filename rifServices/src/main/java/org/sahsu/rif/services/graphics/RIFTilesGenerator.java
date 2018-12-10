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
import org.sahsu.rif.generic.concepts.User;

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
	private static BaseSQLManager baseSQLManager = null;
	private static String username = null;
	
	public RIFTilesGenerator() { // Dummy constructor
	}
	
	/** 
	 * Initialize function; called before run
	 *
	 * @param String username
	 * @param RIFServiceStartupOptions for the extract directory
	 *
	 * @throws RIFServiceException RIF error
	 * @throws SQLException Unable to connect to database
	 * @throws ClassNotFoundException Unable to load JFBC driver
     */		
	public void initialise( 
		final String username, 
		final RIFServiceStartupOptions rifServiceStartupOptions)
		throws RIFServiceException, ClassNotFoundException {
			
		this.rifServiceStartupOptions=rifServiceStartupOptions;
		this.username = username;
		this.baseSQLManager = new BaseSQLManager(rifServiceStartupOptions);

		Class.forName(rifServiceStartupOptions.getDatabaseDriverClassName()); // Load JDBC driver
	}

	/** 
	 * Required Thread run function
	 */
	public void run() {
		try {			
			RIFTiles rifTiles = new RIFTiles(rifServiceStartupOptions);

			User user = User.newInstance(username, "::1" /* IP address */);
			String password=null;
			int sleepInteval=1;
			do {
				password=baseSQLManager.getUserPassword(user);
				if (password == null ) {
					if (sleepInteval <= 256) {
						sleepInteval=sleepInteval*2;
					}
					rifLogger.info(getClass(), 
						"RIF Middleware Tile Generator cannot be run yet, tileGeneratorUsername: " +
						username + " has not yet logged on to the RIF; sleeping for: " + sleepInteval + " seconds");
					Thread.sleep(sleepInteval*1000);
				}
			} while (password == null);

			this.connection=baseSQLManager.createConnection(username, password, 
				false 	/* isFirstConnectionForUser */,
				true 	/* isReadOnly */);

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
		catch (InterruptedException interruptedException) {
			rifLogger.info(this.getClass(), "Tile generator run() UNHANDLED INTERRUPTEDEXCEPTION: " + interruptedException.getMessage());
		}	
	}
	
}