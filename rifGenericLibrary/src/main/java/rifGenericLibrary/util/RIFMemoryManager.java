package rifGenericLibrary.util;

/**
 * This is a class used for memory management. For the present it only has one method: printThreadMemory()
 * It has been crrated statically as it is intended that this will run as a thread in the background 
 * checking that the thread memory is adequate.
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
import java.lang.management.ManagementFactory;
import java.lang.Runtime;
import com.sun.management.ThreadMXBean;
import java.lang.management.ThreadInfo; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

import rifGenericLibrary.util.RIFLogger;

public final class RIFMemoryManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");	
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	//TOUR_CONCURRENCY
	/*
	 * We use eager instantiation to create a single instance of the class
	 * in a thread-safe way.  Here, RIFMemoryManager is created and assigned a value once
	 * when the class is loaded by the class loader.  It ensures that multiple threads
	 * will not each try to initialise the object.
	 * 
	 * It is more common in discussion of the Singleton pattern to use a construction that
	 * supports lazy instantiation, or instantiating RIFMemoryManager when it is needed.  There are 
	 * two reasons I don't do this.
	 * 
	 * First, we are guaranteed to need RIFMemoryManager when the RIF middleware is running.  It does not
	 * make much sense to use lazy instantiation for when you are guaranteed to need it soon after
	 * the middleware starts operating.  Second, this solution for ensuring thread safety
	 * is much simpler than the variety of solutions which are used to make lazy instantiation 
	 * thread-safe.
	 * 
	 * This solution is not perfect, and may be altered later.  Its biggest weakness is that
	 * it won't report exceptions should rifLogger fail to instantiate.
	 *
	 * Inspiration https://stackoverflow.com/questions/25033458/memory-consumed-by-a-thread
	 */
	private static ThreadMXBean threadMxBean = (ThreadMXBean) ManagementFactory.getThreadMXBean(); 
			// Returns the managed bean for the thread system of the Java virtual machine.
	public static Runtime runtime = Runtime.getRuntime();
	private static final RIFMemoryManager rifMemoryManager = new RIFMemoryManager();
			// the Runtime object associated with the current Java application.
	private static final long warnAtAvailableMemory=(500*1024*1024); // 500M
	
	// ==========================================
	// Section Construction: 
	// ==========================================
	
	private RIFMemoryManager() {
		threadMxBean.setThreadAllocatedMemoryEnabled(true);
			// Enables or disables thread memory allocation measurement

		rifLogger.info(this.getClass(), "Thread monitoring enabled");

	}


	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public static RIFMemoryManager getMemoryManager() { // Return this static object
		return rifMemoryManager;
	}	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void printThreadMemory() {
		long[] allThreadIds = threadMxBean.getAllThreadIds();
		ThreadInfo[] allThreads = threadMxBean.getThreadInfo(allThreadIds);
		StringBuilder threadInfo = new StringBuilder();
		int threadCount = threadMxBean.getThreadCount();
		long totalBytes = 0;
		long thisThread = Thread.currentThread().getId();
		long availableMemory=0;
		
		threadInfo.append("Thread list, " + threadCount + " threads >>>" + lineSeparator);
		for ( ThreadInfo thread : allThreads) {
			totalBytes+=threadMxBean.getThreadAllocatedBytes(thread.getThreadId());
			if (thread.getThreadId() == thisThread) {		
				threadInfo.append("CURRENT thread: "+ thread.getThreadName() + "; ID : " + thread.getThreadId());
			}
			else {			
				threadInfo.append("Thread: "+ thread.getThreadName() + "; ID : " + thread.getThreadId());
			}
			threadInfo.append("; state: " + printThreadState(thread) +
				"; memory: " + humanReadableByteCount(threadMxBean.getThreadAllocatedBytes(thread.getThreadId())) + lineSeparator);
		}
		threadInfo.append("<<<" + lineSeparator + "Total thread memory: " + humanReadableByteCount(totalBytes) + lineSeparator);
		availableMemory=runtime.maxMemory() - runtime.totalMemory();
		if (availableMemory < warnAtAvailableMemory) { // 500M		
			rifLogger.warning(this.getClass(), "Available memory is low: " + availableMemory + 
				"; less than warning limit: " + warnAtAvailableMemory);
		}
		threadInfo.append("Memory" +
			": max: " + humanReadableByteCount(runtime.maxMemory()) +
			", total: " + humanReadableByteCount(runtime.totalMemory()) +
			", free: " + humanReadableByteCount(runtime.freeMemory()) +
			", available: " + humanReadableByteCount(availableMemory) +
			"; processors: " + runtime.availableProcessors());
		
		rifLogger.info(this.getClass(), threadInfo.toString()); 
	}
	
	private static String humanReadableByteCount(long bytes) {
		
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("KMGTPE").charAt(exp-1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	private static String printThreadState(ThreadInfo thread) {
		
		String threadState;
		switch (thread.getThreadState()) {
			case NEW: 	
					threadState="NEW"; // A thread that has not yet started is in this state
					break;
			case RUNNABLE: 	
					threadState="RUNNABLE"; // A thread executing in the Java virtual machine is in this state
					break;
			case BLOCKED: 	
					threadState="BLOCKED"; // A thread that is blocked waiting for a monitor lock is in this state
					break;
			case WAITING: 	
					threadState="WAITING"; // A thread that is waiting indefinitely for another thread to perform a particular action is in this state
					break;
			case TIMED_WAITING: 	
					threadState="TIMED_WAITING"; // A thread that is waiting for another thread to perform an action for up to a specified waiting time is in this state
					break;
			case TERMINATED: 	
					threadState="TERMINATED"; // A thread that has exited is in this state";
					break;
			default:
					threadState="ERROR! UNOWN STATE: " + thread.getThreadState();
					break;
		}
		
		return threadState;
	}
	 
	// ==========================================
	// Section Override
	// ==========================================
}
