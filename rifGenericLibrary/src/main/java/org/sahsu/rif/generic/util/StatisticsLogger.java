package org.sahsu.rif.generic.util;

/**
 * This is a class used for error logging and provides a facade to the underlying
 * SLF4J logging facilities.  The class has been developed as part of a temporary
 * work-around for transitivity problems we've encountered with classes in the 
 * SLF4J jars.  By using this class, we've reduced the number of times that SL4J
 * is explicitly called in the code base.  For now we're commenting out references
 * to SLF4J until the transitive dependencies problems can be addressed.
 * 
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sahsu.rif.generic.util.RIFLogger;

public final class StatisticsLogger extends RIFLogger {

	//TOUR_CONCURRENCY
	/*
	 * We use eager instantiation to create a single instance of the class
	 * in a thread-safe way.  Here, StatisticsLogger is created and assigned a value once
	 * when the class is loaded by the class loader.  It ensures that multiple threads
	 * will not each try to initialise the object.
	 * 
	 * It is more common in discussion of the Singleton pattern to use a construction that
	 * supports lazy instantiation, or instantiating StatisticsLogger when it is needed.  There are 
	 * two reasons I don't do this.
	 * 
	 * First, we are guaranteed to need StatisticsLogger when the RIF middleware is running.  It does not
	 * make much sense to use lazy instantiation for when you are guaranteed to need it soon after
	 * the middleware starts operating.  Second, this solution for ensuring thread safety
	 * is much simpler than the variety of solutions which are used to make lazy instantiation 
	 * thread-safe.
	 * 
	 * This solution is not perfect, and may be altered later.  Its biggest weakness is that
	 * it won't report exceptions should StatisticsLogger fail to instantiate.
	 */
	private static final StatisticsLogger StatisticsLogger = new StatisticsLogger();
	private static String lineSeparator = System.getProperty("line.separator");
				
	private StatisticsLogger() {

		/* Get actual class name to be printed on */
		String message = "Created StatisticsLogger: " + StatisticsLogger.class.getName();
		boolean setManager=false;
		
		String logManagerName=System.getProperty("java.util.logging.manager");
		if (logManagerName == null || !logManagerName.equals("java.util.logging.manager")) {
			System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			setManager=true;
		}
		
		Logger log = null;
		try {	
			log = LogManager.getLogger(StatisticsLogger.class.getName());
			setLog(log);	
		} catch(Exception e) {
			System.out.println("StatisticsLogger() LogManager.getLogger: Caught exception: "
			                   + e.getMessage());
		} finally {
			if (log != null) {
				log.info("[StatisticsLogger]: " + message);
				if (setManager) {
					log.info("[StatisticsLogger]: Set java.util.logging.manager=" +
						System.getProperty("java.util.logging.manager"));
				}
			} else {
				System.out.println("INFO(no StatisticsLogger) " + message);
			}			
		}
	}

	public static StatisticsLogger getLogger() { // Return this static object
		return StatisticsLogger;
	}	
}
