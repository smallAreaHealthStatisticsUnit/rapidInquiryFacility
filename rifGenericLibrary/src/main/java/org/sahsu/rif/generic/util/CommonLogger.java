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
 * Peter Hambly
 * @author phambly
 *
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
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Properties;

abstract class CommonLogger {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//TOUR_CONCURRENCY
	/*
	 * We use eager instantiation to create a single instance of the class
	 * in a thread-safe way.  Here, CommonLogger is created and assigned a value once
	 * when the class is loaded by the class loader.  It ensures that multiple threads
	 * will not each try to initialise the object.
	 * 
	 * It is more common in discussion of the Singleton pattern to use a construction that
	 * supports lazy instantiation, or instantiating CommonLogger when it is needed.  There are 
	 * two reasons I don't do this.
	 * 
	 * First, we are guaranteed to need CommonLogger when the RIF middleware is running.  It does not
	 * make much sense to use lazy instantiation for when you are guaranteed to need it soon after
	 * the middleware starts operating.  Second, this solution for ensuring thread safety
	 * is much simpler than the variety of solutions which are used to make lazy instantiation 
	 * thread-safe.
	 * 
	 * This solution is not perfect, and may be altered later.  Its biggest weakness is that
	 * it won't report exceptions should CommonLogger fail to instantiate.
	 */
	private static String lineSeparator = System.getProperty("line.separator");
	private Logger log;
	
	public final void CreateLogger(final String className) {
		String message = "Created CommonLogger for: " + className;
		boolean setManager=false;
		
		String logManagerName=System.getProperty("java.util.logging.manager");
		if (logManagerName == null || !logManagerName.equals("java.util.logging.manager")) {
			System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			setManager=true;
		}
		
		try {
			log=LogManager.getLogger(className);	
															// Get logger called: "CommonLogger" 	
		}
		catch(Exception e) {
			System.out.println("CommonLogger() LogManager.getLogger: Caught exception: " + e.getMessage());
		}
		finally {
			if (log != null) {
				log.info("[CommonLogger A]: " + message); // Should both appear in the same log stream
				info(this.getClass(), "[CommonLogger B]: " + message);
				if (setManager) {
					log.info("[CommonLogger]: Set java.util.logging.manager=" +
						System.getProperty("java.util.logging.manager"));
				}
			}
			else {	
				System.out.println("INFO(no CommonLogger) " + message);
			}			
		}
	}

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
	public final void error(
		final Class callingClass,
		final String errorHeading,
		final Throwable throwableItem) {
			
		StringBuilder errorString = new StringBuilder();
		
		errorString.append("getMessage:          " + ExceptionUtils.getMessage(throwableItem) + lineSeparator);
        errorString.append("getRootCauseMessage: " + ExceptionUtils.getRootCauseMessage(throwableItem) + lineSeparator);
        errorString.append("getThrowableCount:   " + ExceptionUtils.getThrowableCount(throwableItem) + lineSeparator);
		errorString.append("getRootCauseStackTrace >>>" + lineSeparator);
		for (String cause : ExceptionUtils.getRootCauseStackTrace(throwableItem)) {
			errorString.append(cause + lineSeparator);
		}
		errorString.append("<<< End getRootCauseStackTrace.");
		
		if (log != null) {
			log.error("["+callingClass.getName()+"]:" + lineSeparator + 
				errorHeading + lineSeparator + errorString);
		}
		else {	
			System.out.println("ERROR(no CommonLogger):" + lineSeparator + "["+callingClass.getName()+"]" + 
				errorHeading + lineSeparator + throwableItem.getStackTrace());
		}
	}
	
	public void error( // For use in Kev's endless statically defined functions
		final String callingClassName,
		final String errorHeading,
		final Throwable throwableItem) {
			
		StringBuilder errorString = new StringBuilder();
		
		errorString.append("getMessage:          " + ExceptionUtils.getMessage(throwableItem) + lineSeparator);
        errorString.append("getRootCauseMessage: " + ExceptionUtils.getRootCauseMessage(throwableItem) + lineSeparator);
        errorString.append("getThrowableCount:   " + ExceptionUtils.getThrowableCount(throwableItem) + lineSeparator);
		errorString.append("getRootCauseStackTrace >>>" + lineSeparator);
		for (String cause : ExceptionUtils.getRootCauseStackTrace(throwableItem)) {
			errorString.append(cause + lineSeparator);
		}
		errorString.append("<<< End getRootCauseStackTrace.");
		
		if (log != null) {
			log.error("["+callingClassName+"]:" + lineSeparator + 
				errorHeading + lineSeparator + errorString + " [NO STACK]");
		}
		else {	
			System.out.println("ERROR(no CommonLogger):" + lineSeparator + "["+callingClassName+"]" + 
				errorHeading + lineSeparator + throwableItem.getStackTrace());
		}
	}
	
	// error() do not print stack trace
	public final void error(
		final Class callingClass,
		final String errorHeading) {		
		
		if (log != null) {
			log.error("["+callingClass.getName()+"]:" + lineSeparator + 
				errorHeading);
		}
		else {	
			System.out.println("ERROR(no CommonLogger):" + lineSeparator + "["+callingClass.getName()+"]" + 
				errorHeading);
		}
	}

	public final void warning( // For use in Kev's endless statically defined functions
		final String callingClassName,
		final String errorHeading,
		final Throwable throwableItem) {
			
		StringBuilder errorString = new StringBuilder();
		
		errorString.append("getMessage:          " + ExceptionUtils.getMessage(throwableItem) + lineSeparator);
        errorString.append("getRootCauseMessage: " + ExceptionUtils.getRootCauseMessage(throwableItem) + lineSeparator);
        errorString.append("getThrowableCount:   " + ExceptionUtils.getThrowableCount(throwableItem) + lineSeparator);
		errorString.append("getRootCauseStackTrace >>>" + lineSeparator);
		for (String cause : ExceptionUtils.getRootCauseStackTrace(throwableItem)) {
			errorString.append(cause + lineSeparator);
		}
		errorString.append("<<< End getRootCauseStackTrace.");
		
		if (log != null) {
			log.warn("["+callingClassName+"]:" + lineSeparator + 
				errorHeading + lineSeparator + errorString);
		}
		else {	
			System.out.println("WARNING(no CommonLogger):" + lineSeparator + "["+callingClassName+"]" + 
				errorHeading + lineSeparator + throwableItem.getStackTrace());
		}
	}
			
	// warning() print stack tracr
	public final void warning(
		final Class callingClass,
		final String errorHeading,
		final Throwable throwableItem) {
			
		StringBuilder errorString = new StringBuilder();
		
		errorString.append("getMessage:          " + ExceptionUtils.getMessage(throwableItem) + lineSeparator);
        errorString.append("getRootCauseMessage: " + ExceptionUtils.getRootCauseMessage(throwableItem) + lineSeparator);
        errorString.append("getThrowableCount:   " + ExceptionUtils.getThrowableCount(throwableItem) + lineSeparator);
		errorString.append("getRootCauseStackTrace >>>" + lineSeparator);
		for (String cause : ExceptionUtils.getRootCauseStackTrace(throwableItem)) {
			errorString.append(cause + lineSeparator);
		}
		errorString.append("<<< End getRootCauseStackTrace.");
		
		if (log != null) {
			log.warn("["+callingClass.getName()+"]:" + lineSeparator + 
				errorHeading + lineSeparator + errorString);
		}
		else {	
			System.out.println("WARNING(no CommonLogger):" + lineSeparator + "["+callingClass.getName()+"]" + 
				errorHeading + lineSeparator + throwableItem.getStackTrace());
		}
	}
		
	public final void warning( // For use in Kev's endless statically defined functions
		final String callingClassName,			
		final String message) {
		
		if (log != null) {
			log.warn("["+callingClassName+"]:" + lineSeparator + message);
		}
		else {	
			System.out.println("WARNING(no CommonLogger):" + lineSeparator + "["+callingClassName+"]" + message);
		}
	}	
	
	public final void warning(
		final Class callingClass,			
		final String message) {
		
		if (log != null) {
			log.warn("["+callingClass.getName()+"]:" + lineSeparator + message);
		}
		else {	
			System.out.println("WARNING(no CommonLogger):" + lineSeparator + "["+callingClass.getName()+"]" + message);
		}
	}

	public final void info( // For use in Kev's endless statically defined functions
		final String callingClassName,			
		final String message) {
		
		if (log != null) {
			log.info("["+callingClassName+"]:" + lineSeparator + message);

		} else {
			System.out.println("INFO(no CommonLogger):" + lineSeparator + "["+callingClassName+"]" + message);
		}
	}
	
	public final void info(
		final Class callingClass,			
		final String message) {
		
		info(callingClass.getName(), message);
	}

	public final void debug(
		final Class callingClass,			
		final String message) {

		if (log != null) {		
			log.debug("["+callingClass.getName()+"]:" + lineSeparator + message);
		}
		else {	
			System.out.println("DEBUG(no CommonLogger) " + "["+callingClass.getName()+"]:" + lineSeparator + message);
		}
	}

	public final void debugQuery(
			final Object caller,
			final String debugLabel,
			final String message) {

		/*
		StringBuilder buffer = new StringBuilder();
		buffer.append("DEBUG: Class:");
		buffer.append(caller.getClass());
		
		if (debugLabel != null) {
			buffer.append(" DebugLabel: ");
			buffer.append(debugLabel);
		}
		
		System.out.println(buffer.toString());
		*/
		if (log != null) {
			log.debug("DEBUG " + debugLabel + ": " + message);
		}
		else {	
			System.out.println("DEBUG(no CommonLogger) " + debugLabel + ": " + message);
		}
			
	}
	
	public final void debug(
		final String debugLabel,
		final String message) {
				
		if (log != null) {
			log.debug("DEBUG " + debugLabel + ": " + message);
		}
		else {	
			System.out.println("DEBUG(no CommonLogger) " + debugLabel + ": " + message);
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
