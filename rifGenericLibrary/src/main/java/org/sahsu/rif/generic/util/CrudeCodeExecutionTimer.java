package org.sahsu.rif.generic.util;



import java.util.HashMap;
import java.util.ArrayList;

/**
 * <p>
 * This class is intended to provide a very primitive diagnostic tool to give an
 * approximate idea of how long it takes parts of the code to execute.  This is nothing
 * more than something that monitors calls to System.currentTimeMillis() values.
 * </p>
 * 
 * <p>
 * The class is designed purely to give a very rough idea of where time is being
 * spent in method calls.  It is not thread-safe and eventually we will be looking at
 * profiling tools instead.
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

public final class CrudeCodeExecutionTimer {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private static final CrudeCodeExecutionTimer timer 
		= new CrudeCodeExecutionTimer();
	
	private HashMap<String, Long> timeFromIthStoppingPoint;
	private ArrayList<String> stoppingPointLabels;
	private long startTime;
	private long stopTime;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private CrudeCodeExecutionTimer() {
		timeFromIthStoppingPoint = new HashMap<String, Long>();
		stoppingPointLabels = new ArrayList<String>();		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public static CrudeCodeExecutionTimer getTimer() {
		return timer;
	}
	
	public void clearStoppingPoints() {		
		timeFromIthStoppingPoint.clear();
	}

	public void start() {
		startTime = System.currentTimeMillis();		
	}
	
	public void addStoppingPoint(
		final String stoppingPointLabel) {
		
		timeFromIthStoppingPoint.put(
			stoppingPointLabel,
			System.currentTimeMillis());
		stoppingPointLabels.add(stoppingPointLabel);
	}
	
	public void stop() {
		stopTime = System.currentTimeMillis();
	}
	
	
	public void printReport() {
		long totalTime = stopTime - startTime;
		if (totalTime == 0) {
			System.out.println("You forgot to set either the start time, the stop time or both.");
			return;
		}
		
		System.out.println("Total time spent:" + totalTime);
		
		long lastTime = startTime;		
		for (String stoppingPointLabel : stoppingPointLabels) {
			long stepNumberTime
				= timeFromIthStoppingPoint.get(stoppingPointLabel);
			long timeSinceLastStoppingPoint
				= stepNumberTime - lastTime;
			lastTime = stepNumberTime;
			double percentageTime
				= (timeSinceLastStoppingPoint * 100) / totalTime;
			StringBuilder report = new StringBuilder();
			report.append("At Step \'");
			report.append(stoppingPointLabel);
			report.append("\'.");
			report.append("Time stamp:");
			report.append(stepNumberTime);
			report.append("  Time spent:");
			report.append(timeSinceLastStoppingPoint);
			report.append("  Percentage:");
			report.append(percentageTime);
			System.out.println(report.toString());
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
