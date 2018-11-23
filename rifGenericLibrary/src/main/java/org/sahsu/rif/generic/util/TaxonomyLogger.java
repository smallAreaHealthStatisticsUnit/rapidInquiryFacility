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
 * Peter Hambly
 * @author phambly
 *
 * Kevin Garwood
 * @author kgarwood
 */

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sahsu.rif.generic.util.CommonLogger;

public final class TaxonomyLogger extends CommonLogger {

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
				
	private TaxonomyLogger() {
		try {	
			CreateLogger(TaxonomyLogger.class.getName());	
			info(this.getClass(), "Created TaxonomyLogger.");
		} catch(Exception e) {
			System.out.println("TaxonomyLogger() LogManager.getLogger: Caught exception: "
			                   + e.getMessage());
		} 
	}

	public static TaxonomyLogger getLogger() { // Return this static object
		return taxonomyLogger;
	}	
}
