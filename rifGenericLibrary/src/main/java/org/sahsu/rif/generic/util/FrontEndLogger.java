package org.sahsu.rif.generic.util;

/**
 * This is a class used for error logging and provides a facade to the underlying
 * SL4J logging facilities.  The class has been developed as part of a temporary
 * work-around for transitivity problems we've encountered with classes in the 
 * SL4J jars.  By using this class, we've reduced the number of times that SL4J
 * is explicitly called in the code base.  For now we're commenting out references
 * to SL4J until the transitive dependencies problems can be addressed.
 * 
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public final class FrontEndLogger {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//TOUR_CONCURRENCY
	/*
	 * We use eager instantiation to create a single instance of the class
	 * in a thread-safe way.  Here, FrontEndLogger is created and assigned a value once
	 * when the class is loaded by the class loader.  It ensures that multiple threads
	 * will not each try to initialise the object.
	 * 
	 * It is more common in discussion of the Singleton pattern to use a construction that
	 * supports lazy instantiation, or instantiating FrontEndLogger when it is needed.  There are 
	 * two reasons I don't do this.
	 * 
	 * First, we are guaranteed to need FrontEndLogger when the RIF middleware is running.  It does not
	 * make much sense to use lazy instantiation for when you are guaranteed to need it soon after
	 * the middleware starts operating.  Second, this solution for ensuring thread safety
	 * is much simpler than the variety of solutions which are used to make lazy instantiation 
	 * thread-safe.
	 * 
	 * This solution is not perfect, and may be altered later.  Its biggest weakness is that
	 * it won't report exceptions should FrontEndLogger fail to instantiate.
	 */
	private static final FrontEndLogger FrontEndLogger = new FrontEndLogger();
	private static Logger log;
	private static String lineSeparator = System.getProperty("line.separator");
	private static Properties prop = null;
		
	private enum MessageType {
				ERROR,
				WARNING,
				SUCCESS,	// For compatibility with ngNotificationsBar; mapped to INFO in middleware
				INFO,
				DEBUG};	
				
	// ==========================================
	// Section Construction: log4j
	// ==========================================
	
	private FrontEndLogger() {
//		System.out.println("FrontEndLogger() INIT");
		/* Get actual class name to be printed on */
		String message = "Created FrontEndLogger: " + FrontEndLogger.class.getName();
		boolean setManager=false;
		
		String logManagerName=System.getProperty("java.util.logging.manager");
		if (logManagerName == null || !logManagerName.equals("java.util.logging.manager")) {
			System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			setManager=true;
		}
		
		try {
			log=LogManager.getLogger(FrontEndLogger.class.getName());	
															// Get logger called: "FrontEndLogger" 	
		}
		catch(Exception e) {
			System.out.println("FrontEndLogger() LogManager.getLogger: Caught exception: " + e.getMessage());
		}
		finally {
			if (log != null) {
				log.info("[FrontEndLogger]: " + message);
				if (setManager) {
					log.info("[FrontEndLogger]: Set java.util.logging.manager=" +
						System.getProperty("java.util.logging.manager"));
				}
			}
			else {	
				System.out.println("INFO(no FrontEndLogger) " + message);
			}			
		}
	}
	
	public void printLoggers() {
		if (log == null) {	
			System.out.println("INFO(no FrontEndLogger) [printLoggers]");
			return;
		}	
	}
	
	public static FrontEndLogger getLogger() { // Return this static object
		return FrontEndLogger;
	}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	// 
	
	/**
	 * error() print error message and stack trace. Contains message, root cause message, throwable count,
	 * All root cause stack traces.
	 * <p>
	 * May NOT be used in statically defined functions (because of this.getClass()). 
	 *
	 * @param  callingClass  Calling class (object); usually this.getClass()
	 * @param  errorHeading  Textual error heading
	 * @param  throwableItem Exception caught
	 * @return Nothing
	 */
	public void frontEndMessage(
			final String userID,
			final String browserType,
			final String ipAddress,
			final String stringMessageType,
			final String message, 
			final String errorMessage,
			final String errorStack,
			final String actualTime,
			final String relativeTime) {
			
		StringBuilder messageString = new StringBuilder();

		MessageType messageType=null;
		for (MessageType mType : MessageType.values()) {
			if (mType.toString().toUpperCase().equals(stringMessageType.toUpperCase())) {
				messageType=mType;
			}				
		}
		if (messageType == null) {
			System.out.println("ERROR(FrontEndLogger invalid messageType: " + stringMessageType + 
				"): " + messageString.toString());
		}
		
		messageString.append(lineSeparator);
		if (userID != null) {
			messageString.append("userID:       " + userID + lineSeparator);
		}
		if (browserType != null) {
			messageString.append("browser type: " + browserType + lineSeparator);
		}
		if (ipAddress != null) {
			messageString.append("iP address:   " + ipAddress + lineSeparator);
		}
		if (message != null) {
			messageString.append("message:      " + message + lineSeparator);
		}
		if (errorMessage != null && !errorMessage.equals("Dummy")) {
			messageString.append("error message: " + errorMessage + lineSeparator);
		}
		if (errorStack != null) {
			messageString.append("error stack>>>" + lineSeparator + errorStack + "<<<" + lineSeparator);
		}
		if (actualTime != null) {
			messageString.append("actual time:  " + actualTime + lineSeparator);
		}
		if (relativeTime != null) {
			messageString.append("relative:     +" + relativeTime + lineSeparator);
		}
			
		if (log != null) {
			if (messageType == MessageType.ERROR) {
				log.error(messageString.toString());
			}
			else if (messageType == MessageType.WARNING) {
				log.warn(messageString.toString());
			}
			else if (messageType == MessageType.DEBUG) {
				log.debug(messageString.toString());
			}
			else {
				log.info(messageString.toString());
			}
		}
		else {	
			System.out.println("ERROR(no FrontEndLogger): " + messageString.toString());
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
