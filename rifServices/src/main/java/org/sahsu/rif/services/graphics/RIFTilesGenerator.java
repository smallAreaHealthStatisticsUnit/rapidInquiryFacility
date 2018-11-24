package org.sahsu.rif.services.graphics;

import java.sql.Connection;
import java.sql.SQLException;

import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.graphics.RIFTiles;
import org.sahsu.rif.services.datastorage.common.BaseSQLManager;

public class RIFTilesGenerator implements Runnable {

	private static final int SLEEP_TIME = 200;

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static RIFServiceStartupOptions rifServiceStartupOptions = null;
	private static Connection connection = null;
	
	public RIFTilesGenerator() { // Dummy constructor
	}
	
	public void initialise( // Initialize function
		final String username, 
		final String password,
		final RIFServiceStartupOptions rifServiceStartupOptions)
		throws RIFServiceException, SQLException, ClassNotFoundException {
			
		this.rifServiceStartupOptions=rifServiceStartupOptions;
		Class.forName(rifServiceStartupOptions.getDatabaseDriverClassName()); // Load JDBC driver
		BaseSQLManager baseSQLManager = new BaseSQLManager(rifServiceStartupOptions);
		this.connection=baseSQLManager.createConnection(username, password, 
			false /* isFirstConnectionForUser */ ,
			true /* isReadOnly */);
	}
			
	public void run() { // Required Thread run function
		try {			
			RIFTiles rifTiles = new RIFTiles(rifServiceStartupOptions);
			rifTiles.generateTiles(connection);
			rifLogger.info(this.getClass(), "Tile generator run() Finished OK!");
		}
/*		catch(InterruptedException interruptedException) {
			rifLogger.info(this.getClass(), "Tile generator run() run() FAILED: " + interruptedException.getMessage());
			rifLogger.error(this.getClass(), getClass().getSimpleName() + " ERROR",
			                interruptedException);

		} */
		catch(RIFServiceException rifServiceException) {
			rifLogger.info(this.getClass(), "Tile generator run() run() FAILED: " + rifServiceException.getMessage());
			rifServiceException.printErrors();
		}
	}
	
}