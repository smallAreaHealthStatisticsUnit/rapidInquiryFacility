package org.sahsu.rif.services.graphics;

import java.sql.Connection;
import java.sql.SQLException;

import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.generic.system.RIFServiceException;
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

	// --Commented out by Inspection (10/12/2018 11:35):private static final int SLEEP_TIME = 200;

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	// --Commented out by Inspection (10/12/2018 11:34):private static String lineSeparator = System.getProperty("line.separator");
	private static RIFServiceStartupOptions rifServiceStartupOptions = null;
	private static BaseSQLManager baseSQLManager = null;
	private static String username = null;
	private static boolean doStop = false;
	private static int sleepInteval=1;
	
	public RIFTilesGenerator() { // Dummy constructor
	}
	
	/** 
	 * Initialize function; called before run
	 *
	 * @param username String
	 * @param rifServiceStartupOptions RIFServiceStartupOptions for the extract directory
	 *
	 * @throws ClassNotFoundException Unable to load JFBC driver
     */		
	public void initialise( 
		final String username, 
		final RIFServiceStartupOptions rifServiceStartupOptions)
		throws ClassNotFoundException {
			
		this.rifServiceStartupOptions=rifServiceStartupOptions;
		this.username = username;
		baseSQLManager = new BaseSQLManager(rifServiceStartupOptions);
		sleepInteval=2;

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
			do {
				password=baseSQLManager.getUserPassword(user);
				if (password == null ) {
					if (sleepInteval <= 30) {
						sleepInteval=sleepInteval+2;
					}
					rifLogger.info(getClass(), 
						"RIF Middleware Tile Generator cannot be run yet, tileGeneratorUsername: " +
						username + " has not yet logged on to the RIF; sleeping for: " + sleepInteval + " seconds");
					try {
						for (int i=0; i<sleepInteval; i++) {
							Thread.sleep(1000); // 1 sec
							if (!keepRunning()) {
								break;
							}
						}
					}
					catch (InterruptedException interruptedException) {
						rifLogger.info(this.getClass(), "Tile generator run() INTERRUPTEDEXCEPTION: " + interruptedException.getMessage());
					}	
				}
			} while (password == null && keepRunning());

			/* isFirstConnectionForUser */
			/* isReadOnly */
			Connection connection = baseSQLManager.createConnection(username, password,
					false    /* isFirstConnectionForUser */,
					true    /* isReadOnly */);

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

	/** 
	 * RIFService shutdown thread
	 */
    public synchronized void doStop() {
		rifLogger.info(this.getClass(), "Tile generator doStop() called from RIFService");
        doStop = true;
    }

	/** 
	 * Should the thread keep running?
	 */
    private synchronized boolean keepRunning() {
        return doStop == false;
    }
}