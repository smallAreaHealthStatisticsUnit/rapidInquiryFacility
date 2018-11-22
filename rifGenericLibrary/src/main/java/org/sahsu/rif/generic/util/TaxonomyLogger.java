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

public final class TaxonomyLogger {

	//TOUR_CONCURRENCY
	/*
	 * We use eager instantiation to create a single instance of the class
	 * in a thread-safe way.  Here, TaxonomyLogger is created and assigned a value once
	 * when the class is loaded by the class loader.  It ensures that multiple threads
	 * will not each try to initialise the object.
	 * 
	 * It is more common in discussion of the Singleton pattern to use a construction that
	 * supports lazy instantiation, or instantiating TaxonomyLogger when it is needed.  There are 
	 * two reasons I don't do this.
	 * 
	 * First, we are guaranteed to need TaxonomyLogger when the RIF middleware is running.  It does not
	 * make much sense to use lazy instantiation for when you are guaranteed to need it soon after
	 * the middleware starts operating.  Second, this solution for ensuring thread safety
	 * is much simpler than the variety of solutions which are used to make lazy instantiation 
	 * thread-safe.
	 * 
	 * This solution is not perfect, and may be altered later.  Its biggest weakness is that
	 * it won't report exceptions should TaxonomyLogger fail to instantiate.
	 */
	private static final TaxonomyLogger taxonomyLogger = new TaxonomyLogger();
	private static Logger log;
	private static String lineSeparator = System.getProperty("line.separator");
				
	private TaxonomyLogger() {

		/* Get actual class name to be printed on */
		String message = "Created TaxonomyLogger: " + TaxonomyLogger.class.getName();
		boolean setManager=false;
		
		String logManagerName=System.getProperty("java.util.logging.manager");
		if (logManagerName == null || !logManagerName.equals("java.util.logging.manager")) {
			System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			setManager=true;
		}
		
		try {
			log=LogManager.getLogger(TaxonomyLogger.class.getName());	
		} catch(Exception e) {
			System.out.println("TaxonomyLogger() LogManager.getLogger: Caught exception: "
			                   + e.getMessage());
		} finally {
			if (log != null) {
				log.info("[TaxonomyLogger]: " + message);
				if (setManager) {
					log.info("[TaxonomyLogger]: Set java.util.logging.manager=" +
						System.getProperty("java.util.logging.manager"));
				}
			} else {
				System.out.println("INFO(no TaxonomyLogger) " + message);
			}			
		}
	}

	public static TaxonomyLogger getLogger() { // Return this static object
		return taxonomyLogger;
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
	 */
	public void error(
		final Class callingClass,
		final String errorHeading,
		final Throwable throwableItem) {
			
		StringBuilder errorString = new StringBuilder();
		
		errorString.append("getMessage:          ").append(ExceptionUtils.getMessage(
				throwableItem)).append(lineSeparator);
        errorString.append("getRootCauseMessage: ")
		        .append(ExceptionUtils.getRootCauseMessage(throwableItem)).append(lineSeparator);
        errorString.append("getThrowableCount:   ")
		        .append(ExceptionUtils.getThrowableCount(throwableItem)).append(lineSeparator);
		errorString.append("getRootCauseStackTrace >>>").append(lineSeparator);
		for (String cause : ExceptionUtils.getRootCauseStackTrace(throwableItem)) {
			errorString.append(cause).append(lineSeparator);
		}
		errorString.append("<<< End getRootCauseStackTrace.");
		
		if (log != null) {
			log.error("["+callingClass.getName()+"]:" + lineSeparator + 
				errorHeading + lineSeparator + errorString);
		} else {
			System.out.println("ERROR(no RIFLogger):" + lineSeparator + "["
			                   + callingClass.getName() + "]" + errorHeading + lineSeparator
			                   + Arrays.toString(throwableItem.getStackTrace()));
		}
	}
	
	// error() do not print stack trace
	public void error(
		final Class callingClass,
		final String errorHeading) {		
		
		if (log != null) {
			log.error("["+callingClass.getName()+"]:" + lineSeparator + 
				errorHeading);
		}
		else {	
			System.out.println("ERROR(no TaxonomyLogger):" + lineSeparator + "["+callingClass.getName()+"]" + 
				errorHeading);
		}
	}

		// warning() print stack tracr
	public void warning(
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
			System.out.println("WARNING(no TaxonomyLogger):" + lineSeparator + "["+callingClass.getName()+"]" + 
				errorHeading + lineSeparator + throwableItem.getStackTrace());
		}
	}
		
	public void warning(
		final Class callingClass,			
		final String message) {
		
		if (log != null) {
			log.warn("["+callingClass.getName()+"]:" + lineSeparator + message);
		}
		else {	
			System.out.println("WARNING(no TaxonomyLogger):" + lineSeparator + "["+callingClass.getName()+"]" + message);
		}
	}
	
	public void info(
		final Class callingClass,			
		final String message) {
		
		log.info("["+callingClass.getName()+"]:" + lineSeparator + message);
	}

	public void debug(final Class callingClass, final String message) {

		log.debug("[" + callingClass.getName() + "]:" + lineSeparator + message);
	}
}
